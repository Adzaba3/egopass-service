package com.rva.egopass.exceptions;

import lombok.Getter;

@Getter
public class ReservationNotFoundException extends RuntimeException {
    private final String errorCode;

    public ReservationNotFoundException(String message) {
        super(message);
        this.errorCode = "RESERVATION_NOT_FOUND";
    }

    public ReservationNotFoundException(Long id) {
        super("Reservation not found with id: " + id);
        this.errorCode = "RESERVATION_NOT_FOUND";
    }


}