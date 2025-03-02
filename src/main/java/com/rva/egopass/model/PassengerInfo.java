package com.rva.egopass.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PassengerInfo {

    private String firstName;
    private String lastName;
    private String nationality;
    private String passportNumber;
    private LocalDate passportIssueDate;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

