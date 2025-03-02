package com.rva.egopass.dto;

import com.rva.egopass.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EGoPassRequest {
    // Flight Information
    private String flightType; // LOCAL or INTERNATIONAL
    private String flightNumber;
    private String origin;
    private String destination;
    private String flightCompany;

    // Passenger Information
    private String firstName;
    private String lastName;
    private String nationality;
    private String passportNumber;
    private LocalDate passportIssueDate;

    // Contact Information
    private String email;
    private String phone;

    // payment
    private PaymentMethod paymentMethod;
    private CardDetails cardDetails; // Only required for CREDIT_CARD


}

