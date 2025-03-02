package com.rva.egopass.exceptions;

import lombok.Getter;

@Getter
public class InvalidReservationStateException extends RuntimeException {
    private final String errorCode;

    public InvalidReservationStateException(String message) {
        super(message);
        this.errorCode = "INVALID_RESERVATION_STATE";
    }

    public InvalidReservationStateException(String currentState, String expectedState) {
        super("Invalid reservation state: " + currentState + ", expected: " + expectedState);
        this.errorCode = "INVALID_RESERVATION_STATE";
    }

}