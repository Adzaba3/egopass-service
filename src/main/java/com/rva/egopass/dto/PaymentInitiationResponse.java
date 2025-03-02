package com.rva.egopass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationResponse {
    private Long paymentId;
    private String transactionReference;
    private String redirectUrl;
    private String paymentInstructions;
}

