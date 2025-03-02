package com.rva.egopass.service;

import com.rva.egopass.dto.PaymentCallbackRequest;
import com.rva.egopass.dto.PaymentInitiationRequest;
import com.rva.egopass.dto.PaymentInitiationResponse;

public interface PaymentService {
    boolean verifyPayment(PaymentCallbackRequest callback);
    PaymentInitiationResponse initiatePayment(PaymentInitiationRequest request);
}
