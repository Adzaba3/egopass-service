package com.rva.egopass.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rva.egopass.dto.*;
import com.rva.egopass.service.EGoPassService;
import com.rva.egopass.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/passes")
@RequiredArgsConstructor
@Tag(name = "eGoPass", description = "API pour la gestion des eGoPass")
public class EGoPassController {
    private final EGoPassService eGoPassService;
    private final PaymentService paymentService;

    @Operation(
            summary = "Initier un eGoPass",
            description = "Crée une réservation pour un eGoPass et initie le processus de paiement"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Initiation réussie",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EGoPassInitiationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "ID non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @PostMapping("/initiate/{id}")
    public ResponseEntity<EGoPassInitiationResponse> initiateEGoPass(
            @RequestBody @Valid EGoPassRequest request, @Parameter(description = "ID de l'utilisateur") @PathVariable Long id) {
        // Étape 1 : Créer la réservation
        Long reservationId = eGoPassService.createReservation(request, id);
        // Étape 2 : Initier le paiement pour cette réservation
        PaymentInitiationRequest paymentRequest = PaymentInitiationRequest.builder()
                .reservationId(reservationId)
                .paymentMethod(request.getPaymentMethod())
                .cardDetails(request.getCardDetails())
                .build();
        PaymentInitiationResponse paymentResponse = paymentService.initiatePayment(paymentRequest);
        // Étape 3 : Retourner la réponse avec les infos de paiement
        return ResponseEntity.ok(
                EGoPassInitiationResponse.builder()
                        .reservationId(reservationId)
                        .message("Réservation créée avec succès. Veuillez procéder au paiement.")
                        .expiresIn(3600) // 1 heure avant expiration
                        .transactionReference(paymentResponse.getTransactionReference())
                        .redirectUrl(paymentResponse.getRedirectUrl())
                        .build()
        );
    }

    @Operation(
            summary = "Callback de paiement",
            description = "Endpoint pour recevoir les notifications de la passerelle de paiement"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback traité avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @PostMapping("/payment/callback")
    public ResponseEntity<Void> paymentCallback(
            @RequestBody PaymentCallbackRequest callback) throws JsonProcessingException {
        // Traiter le retour de la passerelle de paiement
        if (paymentService.verifyPayment(callback)) {
            // Si le paiement est réussi, générer l'eGoPass
            eGoPassService.generateEGoPassFromReservation(callback.getReservationId());
        }
        return ResponseEntity.ok().build();
    }



    @Operation(
            summary = "Récupérer un eGoPass",
            description = "Récupère les informations d'un eGoPass par son identifiant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "eGoPass trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EGoPassDTO.class))),
            @ApiResponse(responseCode = "404", description = "eGoPass non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EGoPassDTO> getEGoPass(@Parameter(description = "ID du eGoPass") @PathVariable Long id) {
        return ResponseEntity.ok(eGoPassService.getEGoPass(id));
    }

    @Operation(
            summary = "Télécharger un eGoPass",
            description = "Génère et télécharge le PDF d'un eGoPass"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF généré avec succès",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "eGoPass non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la génération du PDF")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadEGoPass(@Parameter(description = "ID du eGoPass") @PathVariable Long id) throws Exception {
        // Récupérer le PDF généré
        ByteArrayResource pdfResource = eGoPassService.generatePDF(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"egopass-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfResource);
    }
}
