package com.rva.egopass.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.rva.egopass.exceptions.QRCodeGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsable de la génération de QR Codes.
 */
@Service
@Slf4j
public class QRCodeService {

    private static final int QR_CODE_WIDTH = 300;  // Largeur du QR Code en pixels
    private static final int QR_CODE_HEIGHT = 300; // Hauteur du QR Code en pixels
    private static final String QR_CODE_FORMAT = "PNG"; // Format du QR Code

    /**
     * Génère un QR Code sous forme d'un tableau de bytes (format PNG).
     *
     * @param content Le contenu à encoder dans le QR Code.
     * @return Un tableau de bytes représentant l'image du QR Code.
     * @throws QRCodeGenerationException En cas d'erreur lors de la génération.
     */
    public byte[] generateQRCode(String content) {
        log.info("Début de la génération du QR Code pour le contenu : {}", content);

        try {
            // Définition des paramètres du QR Code
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Correction d'erreur élevée
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // Encodage UTF-8

            // Création du QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT, hints);

            // Conversion en image PNG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, QR_CODE_FORMAT, outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            log.info("QR Code généré avec succès pour le contenu : {}", content);
            return qrCodeBytes;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du QR Code pour le contenu : {}", content, e);
            throw new QRCodeGenerationException("Erreur lors de la génération du QR Code", e);
        }
    }
}
