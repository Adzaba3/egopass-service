package com.rva.egopass.serviceimpl;

import com.rva.egopass.dto.CardDetails;
import com.rva.egopass.dto.PaymentCallbackRequest;
import com.rva.egopass.dto.PaymentInitiationResponse;
import com.rva.egopass.model.Payment;
import com.rva.egopass.service.PaymentGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implémentation de simulation du service de passerelle de paiement.
 * Cette classe fournit des réponses simulées pour les différentes méthodes de paiement
 * et est utilisée pour les tests et le développement.
 */
@Service
@Slf4j
public class MockPaymentGatewayService implements PaymentGatewayService {

    /**
     * Simule l'initiation d'un paiement par mobile money.
     *
     * @param payment L'objet de paiement contenant les détails de la transaction
     * @return Une réponse d'initiation de paiement simulée
     */
    @Override
    public PaymentInitiationResponse initiateMobileMoneyPayment(Payment payment) {
        log.info("Initiation d'un paiement Mobile Money pour le paiement ID: {}, montant: {}",
                payment.getId(), payment.getAmount());

        PaymentInitiationResponse response = simulatePaymentResponse(
                payment,
                "Veuillez confirmer le paiement sur votre mobile."
        );

        log.debug("Paiement Mobile Money initié avec la référence: {}", response.getTransactionReference());
        return response;
    }

    /**
     * Simule l'initiation d'un paiement par carte de crédit.
     *
     * @param payment L'objet de paiement contenant les détails de la transaction
     * @param cardDetails Les détails de la carte de crédit
     * @return Une réponse d'initiation de paiement simulée
     */
    @Override
    public PaymentInitiationResponse initiateCreditCardPayment(Payment payment, CardDetails cardDetails) {
        log.info("Initiation d'un paiement par Carte de Crédit pour le paiement ID: {}, montant: {}",
                payment.getId(), payment.getAmount());
        log.debug("Détails de carte reçus pour le numéro se terminant par: {}",
                cardDetails.getCardNumber().substring(Math.max(0, cardDetails.getCardNumber().length() - 4)));

        PaymentInitiationResponse response = simulatePaymentResponse(
                payment,
                "Saisissez vos informations de carte bancaire sur la page de paiement."
        );

        log.debug("Paiement par Carte de Crédit initié avec la référence: {}", response.getTransactionReference());
        return response;
    }

    /**
     * Simule l'initiation d'un paiement par PayPal.
     *
     * @param payment L'objet de paiement contenant les détails de la transaction
     * @return Une réponse d'initiation de paiement simulée
     */
    @Override
    public PaymentInitiationResponse initiatePayPalPayment(Payment payment) {
        log.info("Initiation d'un paiement PayPal pour le paiement ID: {}, montant: {}",
                payment.getId(), payment.getAmount());

        PaymentInitiationResponse response = simulatePaymentResponse(
                payment,
                "Connectez-vous à votre compte PayPal pour finaliser le paiement."
        );

        log.debug("Paiement PayPal initié avec la référence: {}", response.getTransactionReference());
        return response;
    }

    /**
     * Simule la vérification d'un paiement suite à un callback du prestataire de paiement.
     * Dans cette implémentation de simulation, tous les paiements sont considérés comme réussis.
     *
     * @param callback L'objet de requête de callback contenant les détails de vérification
     * @return true (simulation d'une vérification réussie)
     */
    @Override
    public boolean verifyPayment(PaymentCallbackRequest callback) {
        log.info("Vérification du paiement pour la référence de transaction: {}", callback.getTransactionReference());
        log.debug("Réception d'un callback de paiement: {}", callback);

        // Simulation d'une vérification réussie
        log.info("Vérification du paiement réussie (simulation)");
        return true;
    }

    /**
     * Génère une réponse d'initiation de paiement simulée avec une référence de transaction unique.
     *
     * @param payment L'objet de paiement pour lequel générer la réponse
     * @param instructions Les instructions à inclure dans la réponse
     * @return Une réponse d'initiation de paiement complète
     */
    private PaymentInitiationResponse simulatePaymentResponse(Payment payment, String instructions) {
        log.debug("Génération d'une réponse de paiement simulée pour le paiement ID: {}", payment.getId());

        // Génération d'une référence de transaction unique
        String transactionReference = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.debug("Référence de transaction générée: {}", transactionReference);

        // URL de redirection fictive
        String redirectUrl = "https://mock-payment-gateway.com/redirect?tx=" + transactionReference;

        PaymentInitiationResponse response = new PaymentInitiationResponse(
                payment.getId(),
                transactionReference,
                redirectUrl,
                instructions
        );

        log.debug("Réponse de paiement simulée générée: {}", response);
        return response;
    }
}