package com.rva.egopass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EGoPassDTO {
    private String passNumber;
    private String passengerName;
    private String nationality;
    private String flightType;
    private String flightCompany;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime issueDate;
    private String qrCodeUrl;
    private String downloadUrl;
}
