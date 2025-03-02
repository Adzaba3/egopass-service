package com.rva.egopass.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rva.egopass.dto.EGoPassDTO;
import com.rva.egopass.dto.EGoPassRequest;
import com.rva.egopass.dto.QRCodeData;
import com.rva.egopass.enums.ReservationStatus;
import com.rva.egopass.exceptions.*;
import com.rva.egopass.mapper.EGoPassMapper;
import com.rva.egopass.model.EGoPass;
import com.rva.egopass.model.Reservation;
import com.rva.egopass.model.User;
import com.rva.egopass.repository.EGoPassRepository;
import com.rva.egopass.repository.ReservationRepository;
import com.rva.egopass.repository.UserRepository;
import com.rva.egopass.service.EGoPassService;
import com.rva.egopass.service.PDFGeneratorService;
import com.rva.egopass.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implémentation du service EGoPass qui gère la création, génération et récupération des eGoPasses.
 * Ce service coordonne les réservations, la génération de QR codes et de PDFs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EGoPassServiceImpl implements EGoPassService {

    private final EGoPassRepository eGoPassRepository;
    private final ReservationRepository reservationRepository;
    private final QRCodeService qrCodeService;
    private final PDFGeneratorService pdfGeneratorService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EGoPassMapper eGoPassMapper;
    private final UserRepository userRepository;


    /**
     * Crée une réservation temporaire à partir des informations fournies par l'utilisateur.
     *
     * @param request Les données de la demande d'eGoPass
     * @param id L'identifiant de l'utilisateur effectuant la réservation
     * @return L'identifiant de la réservation créée
     */
    @Transactional
    public Long createReservation(EGoPassRequest request, Long id) {
        log.info("Début de création d'une réservation pour l'utilisateur ID: {}", id);

        // Recherche de l'utilisateur dans la base de données
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec l'ID: {}", id);
                    return new UserNotFoundException("Utilisateur non trouvé avec l'ID : " + id);
                });
        log.debug("Utilisateur trouvé: {}", user.getId());

        // Valider les informations fournies
        log.debug("Validation des informations passager pour la demande");
        validatePassengerInfo(request);
        log.debug("Validation des informations de vol pour la demande");
        validateFlightInfo(request);

        // Créer une réservation temporaire
        log.debug("Création d'une nouvelle réservation");
        Reservation reservation = new Reservation();
        reservation.setPassengerInfo(eGoPassMapper.mapToPassengerInfo(request));
        reservation.setFlightInfo(eGoPassMapper.mapToFlightInfo(request));
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        reservation.setUser(user);

        // Sauvegarder la réservation
        log.debug("Sauvegarde de la réservation en base de données");
        reservationRepository.save(reservation);

        log.info("Réservation créée avec succès, ID: {}", reservation.getId());
        return reservation.getId();
    }

    /**
     * Génère un eGoPass à partir d'une réservation existante après paiement.
     * Crée le QR code et met à jour le statut de la réservation.
     *
     * @param reservationId L'identifiant de la réservation
     * @throws JsonProcessingException Si une erreur survient lors de la génération du contenu JSON
     */
    @Transactional
    public void generateEGoPassFromReservation(Long reservationId) throws JsonProcessingException {
        log.info("Début de génération d'eGoPass pour la réservation ID: {}", reservationId);

        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("Réservation non trouvée avec l'ID: {}", reservationId);
                    return new ReservationNotFoundException(reservationId);
                });
        log.debug("Réservation trouvée: {}", reservation.getId());

        // Vérifier que la réservation est en attente de paiement
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            log.error("État invalide de la réservation ID: {}, statut actuel: {}",
                    reservationId, reservation.getStatus());
            throw new InvalidReservationStateException(reservationId.toString());
        }
        log.debug("Validation du statut de la réservation: OK");

        // Générer un identifiant unique pour l'eGoPass
        String eGoPassNumber = generateEGoPassNumber();
        log.debug("Numéro d'eGoPass généré: {}", eGoPassNumber);

        // Générer le QR code qui contiendra les informations essentielles
        log.debug("Génération du contenu du QR code");
        String qrCodeContent = generateQRCodeContent(reservation, eGoPassNumber);
        log.debug("Génération de l'image QR code");
        byte[] qrCodeImage = qrCodeService.generateQRCode(qrCodeContent);

        // Créer l'eGoPass
        log.debug("Création d'un nouvel objet eGoPass");
        EGoPass eGoPass = new EGoPass();
        eGoPass.setPassNumber(eGoPassNumber);
        eGoPass.setQrCodeImage(qrCodeImage);
        eGoPass.setIssueDate(LocalDateTime.now());
        eGoPass.setUser(reservation.getUser());
        eGoPass.setFlightInfo(reservation.getFlightInfo());
        eGoPass.setPassengerInfo(reservation.getPassengerInfo());
        eGoPass.setReservation(reservation);

        // Sauvegarder l'eGoPass
        log.debug("Sauvegarde de l'eGoPass en base de données");
        eGoPassRepository.save(eGoPass);

        // Mettre à jour le statut de la réservation
        log.debug("Mise à jour du statut de la réservation à COMPLETED");
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.setEGoPass(eGoPass);
        reservationRepository.save(reservation);

        // Générer le PDF de l'eGoPass (asynchrone)
        log.debug("Programmation de la génération du PDF pour l'eGoPass ID: {}", eGoPass.getId());
        pdfGeneratorService.scheduleEGoPassPDFGeneration(eGoPass.getId());

        log.info("eGoPass généré avec succès, ID: {}", eGoPass.getId());
    }

    /**
     * Génère ou récupère le document PDF associé à un eGoPass.
     *
     * @param eGoPassId L'identifiant de l'eGoPass
     * @return Une ressource contenant le document PDF
     * @throws Exception Si une erreur survient lors de la génération du PDF
     */
    public ByteArrayResource generatePDF(Long eGoPassId) throws Exception {
        log.info("Demande de génération/récupération de PDF pour l'eGoPass ID: {}", eGoPassId);

        // Récupérer l'eGoPass
        EGoPass eGoPass = eGoPassRepository.findById(eGoPassId)
                .orElseThrow(() -> {
                    log.error("eGoPass non trouvé avec l'ID: {}", eGoPassId);
                    return new EGoPassNotFoundException(eGoPassId);
                });
        log.debug("eGoPass trouvé: {}", eGoPass.getId());

        // Vérifier si le PDF existe déjà
        if (eGoPass.getPdfDocument() != null) {
            log.debug("PDF déjà généré, retour du document existant");
            return new ByteArrayResource(eGoPass.getPdfDocument());
        }

        // Sinon, générer le PDF
        log.debug("Génération d'un nouveau document PDF");
        byte[] pdfDocument = pdfGeneratorService.generateEGoPassPDF(eGoPass);

        // Sauvegarder le PDF généré
        log.debug("Sauvegarde du document PDF généré");
        eGoPass.setPdfDocument(pdfDocument);
        eGoPassRepository.save(eGoPass);

        log.info("PDF généré avec succès pour l'eGoPass ID: {}", eGoPassId);
        return new ByteArrayResource(pdfDocument);
    }

    /**
     * Récupère les informations d'un eGoPass par son identifiant.
     *
     * @param id L'identifiant de l'eGoPass
     * @return Les données DTO de l'eGoPass
     */
    @Transactional(readOnly = true)
    public EGoPassDTO getEGoPass(Long id) {
        log.info("Récupération de l'eGoPass avec ID: {}", id);
        EGoPass egopass = eGoPassRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("EGoPass non trouvé avec ID: {}", id);
                    return new EGoPassNotFoundException(id);
                });
        log.debug("EGoPass trouvé, conversion en DTO");

        EGoPassDTO dto = EGoPassMapper.INSTANCE.toDto(egopass);
        log.debug("DTO créé avec succès");
        return dto;
    }

    // Méthodes utilitaires

    /**
     * Génère un identifiant unique pour une réservation.
     *
     * @return L'identifiant généré
     */
    private String generateReservationId() {
        String id = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.debug("ID de réservation généré: {}", id);
        return id;
    }

    /**
     * Génère un numéro unique pour un eGoPass.
     *
     * @return Le numéro généré
     */
    private String generateEGoPassNumber() {
        String number = "EGP-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        log.debug("Numéro d'eGoPass généré: {}", number);
        return number;
    }

    /**
     * Génère le contenu JSON qui sera encodé dans le QR code de l'eGoPass.
     *
     * @param reservation La réservation associée
     * @param eGoPassNumber Le numéro d'eGoPass
     * @return Le contenu JSON sous forme de chaîne
     * @throws JsonProcessingException Si une erreur survient lors de la conversion en JSON
     */
    private String generateQRCodeContent(Reservation reservation, String eGoPassNumber) throws JsonProcessingException {
        log.debug("Génération du contenu QR code pour eGoPass: {}", eGoPassNumber);

        // Créer un objet JSON contenant toutes les informations essentielles
        QRCodeData data = new QRCodeData();
        data.setPassNumber(eGoPassNumber);
        data.setPassengerName(reservation.getPassengerInfo().getFullName());
        data.setNationality(reservation.getPassengerInfo().getNationality());
        data.setFlightNumber(reservation.getFlightInfo().getFlightNumber());
        data.setFlightCompany(reservation.getFlightInfo().getFlightCompany());
        data.setOrigin(reservation.getFlightInfo().getOrigin());
        data.setDestination(reservation.getFlightInfo().getDestination());
        data.setIssueDate(LocalDateTime.now().toString());

        String jsonContent = objectMapper.writeValueAsString(data);
        log.debug("Contenu QR code généré: {}", jsonContent);
        return jsonContent;
    }

    /**
     * Valide les informations du passager fournies dans la demande.
     * Vérifie que tous les champs obligatoires sont présents et valides.
     *
     * @param request La demande d'eGoPass
     * @throws InvalidRequestException Si des informations sont manquantes ou invalides
     */
    private void validatePassengerInfo(EGoPassRequest request) {
        log.debug("Validation des informations passager");

        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            log.warn("Validation échouée: prénom manquant");
            throw new InvalidRequestException("firstName", "Le prénom du passager est requis.");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            log.warn("Validation échouée: nom manquant");
            throw new InvalidRequestException("lastName", "Le nom du passager est requis.");
        }
        if (request.getNationality() == null || request.getNationality().trim().isEmpty()) {
            log.warn("Validation échouée: nationalité manquante");
            throw new InvalidRequestException("nationality", "La nationalité du passager est requise.");
        }
        if (request.getPassportNumber() == null || request.getPassportNumber().trim().isEmpty()) {
            log.warn("Validation échouée: numéro de passeport manquant");
            throw new InvalidRequestException("passportNumber", "Le numéro de passeport est requis.");
        }
        if (request.getPassportIssueDate() == null || request.getPassportIssueDate().isAfter(LocalDate.now())) {
            log.warn("Validation échouée: date de délivrance du passeport invalide");
            throw new InvalidRequestException("passportIssueDate", "La date de délivrance du passeport est invalide.");
        }
        if (request.getEmail() == null || !request.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            log.warn("Validation échouée: email invalide: {}", request.getEmail());
            throw new InvalidRequestException("email", "L'email du passager est invalide.");
        }
        if (request.getPhone() == null || !request.getPhone().matches("^\\+?[0-9]{7,15}$")) {
            log.warn("Validation échouée: numéro de téléphone invalide: {}", request.getPhone());
            throw new InvalidRequestException("phone", "Le numéro de téléphone est invalide.");
        }

        log.debug("Validation des informations passager réussie");
    }

    /**
     * Valide les informations de vol fournies dans la demande.
     * Vérifie que tous les champs obligatoires sont présents et valides.
     *
     * @param request La demande d'eGoPass
     * @throws InvalidRequestException Si des informations sont manquantes ou invalides
     */
    private void validateFlightInfo(EGoPassRequest request) {
        log.debug("Validation des informations de vol");

        if (request.getFlightType() == null || (!request.getFlightType().equals("LOCAL") && !request.getFlightType().equals("INTERNATIONAL"))) {
            log.warn("Validation échouée: type de vol invalide: {}", request.getFlightType());
            throw new InvalidRequestException("flightType", "Le type de vol doit être 'LOCAL' ou 'INTERNATIONAL'.");
        }
        if (request.getFlightCompany() == null || request.getFlightCompany().trim().isEmpty()) {
            log.warn("Validation échouée: compagnie aérienne manquante");
            throw new InvalidRequestException("airline", "Le nom de la compagnie aérienne est requis.");
        }
        if (request.getFlightNumber() == null || request.getFlightNumber().trim().isEmpty()) {
            log.warn("Validation échouée: numéro de vol manquant");
            throw new InvalidRequestException("flightNumber", "Le numéro de vol est requis.");
        }
        if (request.getOrigin() == null || request.getOrigin().trim().isEmpty()) {
            log.warn("Validation échouée: origine manquante");
            throw new InvalidRequestException("origin", "L'aéroport d'origine est requis.");
        }
        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            log.warn("Validation échouée: destination manquante");
            throw new InvalidRequestException("destination", "L'aéroport de destination est requis.");
        }
        if (request.getOrigin().equalsIgnoreCase(request.getDestination())) {
            log.warn("Validation échouée: origine et destination identiques: {}", request.getOrigin());
            throw new InvalidRequestException("destination", "L'origine et la destination ne peuvent pas être identiques.");
        }

        log.debug("Validation des informations de vol réussie");
    }
}