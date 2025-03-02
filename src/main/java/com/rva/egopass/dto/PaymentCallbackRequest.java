package com.rva.egopass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackRequest {
    private String transactionReference;
    private Long reservationId;
    private String status;
    private Map<String, String> additionalData;
}
