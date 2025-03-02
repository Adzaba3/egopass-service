package com.rva.egopass.service;

import com.rva.egopass.dto.CardDetails;
import com.rva.egopass.dto.PaymentCallbackRequest;
import com.rva.egopass.dto.PaymentInitiationResponse;
import com.rva.egopass.model.Payment;

public interface PaymentGatewayService {
    PaymentInitiationResponse initiateMobileMoneyPayment(Payment payment);
    PaymentInitiationResponse initiateCreditCardPayment(Payment payment, CardDetails cardDetails);
    PaymentInitiationResponse initiatePayPalPayment(Payment payment);
    boolean verifyPayment(PaymentCallbackRequest callback);
}
