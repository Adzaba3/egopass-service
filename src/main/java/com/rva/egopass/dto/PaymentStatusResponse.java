package com.rva.egopass.dto;

import com.rva.egopass.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private Long paymentId;
    private PaymentStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
}