package com.rva.egopass.exceptions;

import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {
    private final String field;

    public InvalidRequestException(String field, String message) {
        super(message);
        this.field = field;
    }
}
