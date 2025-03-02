package com.rva.egopass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeData {
    private String passNumber;
    private String passengerName;
    private String nationality;
    private String flightNumber;
    private String flightCompany;
    private String origin;
    private String destination;
    private String issueDate;
}
