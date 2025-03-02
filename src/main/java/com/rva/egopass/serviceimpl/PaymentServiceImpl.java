package com.rva.egopass.serviceimpl;

import com.rva.egopass.dto.PaymentCallbackRequest;
import com.rva.egopass.dto.PaymentInitiationRequest;
import com.rva.egopass.dto.PaymentInitiationResponse;
import com.rva.egopass.dto.PaymentStatusResponse;
import com.rva.egopass.enums.PaymentStatus;
import com.rva.egopass.exceptions.PaymentException;
import com.rva.egopass.exceptions.ReservationNotFoundException;
import com.rva.egopass.model.Payment;
import com.rva.egopass.model.Reservation;
import com.rva.egopass.repository.PaymentRepository;
import com.rva.egopass.repository.ReservationRepository;
import com.rva.egopass.service.PaymentGatewayService;
import com.rva.egopass.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implémentation du service de paiement qui gère les transactions pour les réservations d'eGoPass.
 * Ce service coordonne l'initiation, la vérification et le suivi des paiements à travers
 * différentes passerelles de paiement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGatewayService paymentGatewayService;

    /**
     * Initie un processus de paiement pour une réservation.
     *
     * @param request Les détails de l'initiation du paiement incluant l'ID de réservation et la méthode de paiement
     * @return Une réponse contenant les informations nécessaires pour compléter le paiement
     * @throws ReservationNotFoundException Si la réservation associée n'existe pas
     * @throws PaymentException Si une erreur survient pendant l'initiation du paiement
     */
    @Transactional
    public PaymentInitiationResponse initiatePayment(PaymentInitiationRequest request) {
        log.info("Début d'initiation de paiement pour la réservation ID: {}", request.getReservationId());

        // Récupérer la réservation
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> {
                    log.error("Réservation non trouvée avec l'ID: {}", request.getReservationId());
                    return new ReservationNotFoundException("Réservation non trouvée: " + request.getReservationId());
                });
        log.debug("Réservation trouvée: {}", reservation.getId());

        // Calculer le montant du paiement
        BigDecimal amount = calculateAmount(reservation);
        log.debug("Montant calculé pour le paiement: {}", amount);

        // Créer l'enregistrement de paiement
        log.debug("Création d'un nouvel enregistrement de paiement");
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setReservation(reservation);
        payment.setCreatedAt(LocalDateTime.now());

        // Enregistrer le paiement
        log.debug("Sauvegarde du paiement initial en base de données");
        paymentRepository.save(payment);

        // Initier le paiement avec la passerelle de paiement appropriée
        PaymentInitiationResponse gatewayResponse;
        log.info("Initiation du paiement avec la méthode: {}", request.getPaymentMethod());

        try {
            switch (request.getPaymentMethod()) {
                case MOBILE_MONEY:
                    log.debug("Traitement d'un paiement par Mobile Money");
                    gatewayResponse = paymentGatewayService.initiateMobileMoneyPayment(payment);
                    break;
                case CREDIT_CARD:
                    log.debug("Traitement d'un paiement par Carte de Crédit");
                    gatewayResponse = paymentGatewayService.initiateCreditCardPayment(payment, request.getCardDetails());
                    break;
                case PAYPAL:
                    log.debug("Traitement d'un paiement par PayPal");
                    gatewayResponse = paymentGatewayService.initiatePayPalPayment(payment);
                    break;
                default:
                    log.error("Méthode de paiement non supportée: {}", request.getPaymentMethod());
                    throw new PaymentException("Méthode de paiement non supportée");
            }
        } catch (Exception e) {
            // En cas d'erreur, marquer le paiement comme échoué
            log.error("Erreur lors de l'initiation du paiement: {}", e.getMessage(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            paymentRepository.save(payment);

            throw new PaymentException("Erreur lors de l'initiation du paiement: " + e.getMessage(), e);
        }

        // Mettre à jour les informations du paiement avec la réponse de la passerelle
        log.debug("Mise à jour du paiement avec la référence de transaction: {}", gatewayResponse.getTransactionReference());
        payment.setTransactionReference(gatewayResponse.getTransactionReference());
        paymentRepository.save(payment);

        log.info("Paiement initié avec succès, ID: {}, Référence: {}",
                payment.getId(), gatewayResponse.getTransactionReference());

        return new PaymentInitiationResponse(
                payment.getId(),
                gatewayResponse.getTransactionReference(),
                gatewayResponse.getRedirectUrl(),
                gatewayResponse.getPaymentInstructions()
        );
    }

    /**
     * Vérifie et confirme un paiement après réception d'un callback de la passerelle de paiement.
     *
     * @param callback Les informations reçues du service de paiement
     * @return true si le paiement est valide et confirmé, false sinon
     * @throws PaymentException Si le paiement associé n'est pas trouvé
     */
    @Transactional
    public boolean verifyPayment(PaymentCallbackRequest callback) {
        log.info("Vérification du paiement avec référence de transaction: {}", callback.getTransactionReference());

        // Récupérer le paiement
        Payment payment = paymentRepository.findByTransactionReference(callback.getTransactionReference())
                .orElseThrow(() -> {
                    log.error("Paiement non trouvé pour la référence de transaction: {}", callback.getTransactionReference());
                    return new PaymentException("Paiement non trouvé pour la référence: " + callback.getTransactionReference());
                });
        log.debug("Paiement trouvé avec ID: {}", payment.getId());

        // Vérifier le paiement avec la passerelle
        log.debug("Vérification du paiement auprès de la passerelle de paiement");
        boolean isValid = paymentGatewayService.verifyPayment(callback);

        if (isValid) {
            // Mettre à jour le statut du paiement
            log.info("Paiement validé avec succès, mise à jour du statut à COMPLETED");
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
        } else {
            log.warn("Échec de la vérification du paiement, mise à jour du statut à FAILED");
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Échec de vérification du paiement");
        }

        log.debug("Sauvegarde du paiement mis à jour en base de données");
        paymentRepository.save(payment);

        return isValid;
    }

    /**
     * Récupère le statut actuel d'un paiement.
     *
     * @param paymentId L'identifiant du paiement
     * @return Un objet contenant les informations de statut du paiement
     * @throws PaymentException Si le paiement n'est pas trouvé
     */
    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        log.info("Récupération du statut du paiement ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Paiement non trouvé avec ID: {}", paymentId);
                    return new PaymentException("Paiement non trouvé: " + paymentId);
                });
        log.debug("Paiement trouvé, statut actuel: {}", payment.getStatus());

        PaymentStatusResponse response = new PaymentStatusResponse(
                payment.getId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCreatedAt(),
                payment.getCompletedAt(),
                payment.getErrorMessage()
        );

        log.debug("Réponse de statut de paiement générée");
        return response;
    }

    /**
     * Génère un identifiant unique pour un paiement.
     *
     * @return L'identifiant de paiement généré
     */
    private String generatePaymentId() {
        String paymentId = "PMT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.debug("ID de paiement généré: {}", paymentId);
        return paymentId;
    }

    /**
     * Calcule le montant à payer en fonction des détails de la réservation.
     *
     * @param reservation La réservation pour laquelle calculer le montant
     * @return Le montant à payer
     */
    private BigDecimal calculateAmount(Reservation reservation) {
        log.debug("Calcul du montant pour la réservation ID: {}, type de vol: {}",
                reservation.getId(), reservation.getFlightInfo().getFlightType());

        // Logique pour calculer le montant basé sur le type de vol et d'autres paramètres
        BigDecimal amount;
        if (reservation.getFlightInfo().getFlightType().equals("INTERNATIONAL")) {
            amount = BigDecimal.valueOf(50.0); // $50 pour les vols internationaux
            log.debug("Vol international détecté, montant fixé à: {}", amount);
        } else {
            amount = BigDecimal.valueOf(25.0); // $25 pour les vols locaux
            log.debug("Vol local détecté, montant fixé à: {}", amount);
        }

        return amount;
    }
}