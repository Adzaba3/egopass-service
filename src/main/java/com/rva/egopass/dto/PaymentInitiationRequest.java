package com.rva.egopass.dto;

import com.rva.egopass.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationRequest {
    private Long reservationId;
    private PaymentMethod paymentMethod;
    private CardDetails cardDetails; // Only required for CREDIT_CARD
}

