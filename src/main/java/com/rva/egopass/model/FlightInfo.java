package com.rva.egopass.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FlightInfo {

    private String flightType; // LOCAL or INTERNATIONAL
    private String flightNumber;
    private String origin;
    private String destination;
    private String flightCompany;
}

