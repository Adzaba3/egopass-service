package com.rva.egopass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EGoPassInitiationResponse {
    private Long reservationId;
    private String message;
    private long expiresIn; // in seconds
    private String transactionReference;
    private String redirectUrl;
}
