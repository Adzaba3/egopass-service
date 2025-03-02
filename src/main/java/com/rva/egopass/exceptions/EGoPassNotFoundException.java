package com.rva.egopass.exceptions;

import lombok.Getter;

@Getter
public class EGoPassNotFoundException extends RuntimeException {
    private final String errorCode;

    public EGoPassNotFoundException(String message) {
        super(message);
        this.errorCode = "EGOPASS_NOT_FOUND";
    }

    public EGoPassNotFoundException(Long id) {
        super("EGoPass not found with id: " + id);
        this.errorCode = "EGOPASS_NOT_FOUND";
    }

}
