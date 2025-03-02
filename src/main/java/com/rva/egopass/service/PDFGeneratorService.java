package com.rva.egopass.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.rva.egopass.exceptions.EGoPassNotFoundException;
import com.rva.egopass.model.EGoPass;
import com.rva.egopass.repository.EGoPassRepository;
import com.rva.egopass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service pour la génération des fichiers PDF relatifs aux e-GoPass.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PDFGeneratorService {

    private final UserRepository userRepository;
    private final EGoPassRepository eGoPassRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Génère un PDF contenant les informations du e-GoPass et retourne le fichier sous forme de tableau de bytes.
     *
     * @param eGoPass Le e-GoPass à transformer en PDF.
     * @return Un tableau de bytes contenant le fichier PDF.
     * @throws Exception En cas d'erreur lors de la génération du PDF.
     */
    public byte[] generateEGoPassPDF(EGoPass eGoPass) throws Exception {
        log.info("Début de la génération du PDF pour le e-GoPass: {}", eGoPass.getPassNumber());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        // Ajout du titre principal
        Paragraph title = new Paragraph("E-GOPASS")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Ajout du sous-titre
        Paragraph subtitle = new Paragraph("Reçu Officiel - République du Cameroun")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(subtitle);

        document.add(new Paragraph("\n"));

        // Ajout du QR Code si disponible
        if (eGoPass.getQrCodeImage() != null) {
            log.info("Ajout du QR Code pour le e-GoPass: {}", eGoPass.getPassNumber());
            Image qrCodeImage = new Image(ImageDataFactory.create(eGoPass.getQrCodeImage()));
            qrCodeImage.setWidth(150);
            qrCodeImage.setHeight(150);
            qrCodeImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            document.add(qrCodeImage);
        }

        document.add(new Paragraph("\n"));

        // Ajout des informations du e-GoPass sous forme de tableau
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        addTableRow(infoTable, "Numéro e-GoPass", eGoPass.getPassNumber());
        addTableRow(infoTable, "Date d'émission", eGoPass.getIssueDate().format(DATE_FORMATTER));

        document.add(infoTable);
        document.add(new Paragraph("\n"));

        // Informations du voyageur
        Paragraph passengerTitle = new Paragraph("Informations du Voyageur")
                .setFontSize(16)
                .setBold();
        document.add(passengerTitle);

        Table passengerTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        passengerTable.setWidth(UnitValue.createPercentValue(100));

        addTableRow(passengerTable, "Nom", eGoPass.getPassengerInfo().getLastName());
        addTableRow(passengerTable, "Prénom", eGoPass.getPassengerInfo().getFirstName());
        addTableRow(passengerTable, "Nationalité", eGoPass.getPassengerInfo().getNationality());
        addTableRow(passengerTable, "Passeport", eGoPass.getPassengerInfo().getPassportNumber());

        document.add(passengerTable);
        document.add(new Paragraph("\n"));

        // Informations du vol
        Paragraph flightTitle = new Paragraph("Informations du Vol")
                .setFontSize(16)
                .setBold();
        document.add(flightTitle);

        Table flightTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        flightTable.setWidth(UnitValue.createPercentValue(100));

        addTableRow(flightTable, "Compagnie", eGoPass.getFlightInfo().getFlightCompany());
        addTableRow(flightTable, "Numéro de Vol", eGoPass.getFlightInfo().getFlightNumber());
        addTableRow(flightTable, "Origine", eGoPass.getFlightInfo().getOrigin());
        addTableRow(flightTable, "Destination", eGoPass.getFlightInfo().getDestination());

        document.add(flightTable);
        document.add(new Paragraph("\n"));

        // Ajout d'un message de pied de page
        Paragraph footer = new Paragraph("Ce document est un titre de voyage officiel. Veuillez le présenter aux autorités lors de votre voyage.")
                .setFontSize(10)
                .setItalic();
        document.add(footer);

        document.close();
        log.info("PDF généré avec succès pour le e-GoPass: {}", eGoPass.getPassNumber());

        return outputStream.toByteArray();
    }

    /**
     * Ajoute une ligne à un tableau PDF.
     *
     * @param table Le tableau auquel ajouter une ligne.
     * @param label L'étiquette de la ligne.
     * @param value La valeur correspondante.
     */
    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)).setBold());
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    /**
     * Génère un PDF pour un e-GoPass de manière asynchrone et l'enregistre en base.
     *
     * @param eGoPassId L'identifiant du e-GoPass.
     */
    @Async
    public void scheduleEGoPassPDFGeneration(Long eGoPassId) {
        log.info("Démarrage de la génération asynchrone du PDF pour l'eGoPass ID: {}", eGoPassId);
        try {
            // Récupérer le e-GoPass depuis la base de données
            EGoPass eGoPass = eGoPassRepository.findById(eGoPassId)
                    .orElseThrow(() -> new EGoPassNotFoundException(eGoPassId));

            // Générer le PDF
            byte[] pdfDocument = generateEGoPassPDF(eGoPass);

            // Enregistrer le PDF dans l'entité e-GoPass
            eGoPass.setPdfDocument(pdfDocument);
            eGoPassRepository.save(eGoPass);

            log.info("Génération du PDF terminée avec succès pour l'eGoPass ID: {}", eGoPassId);
        } catch (EGoPassNotFoundException e) {
            log.error("Impossible de trouver l'eGoPass avec l'ID: {}", eGoPassId, e);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour l'eGoPass ID: {}", eGoPassId, e);
        }
    }
}
