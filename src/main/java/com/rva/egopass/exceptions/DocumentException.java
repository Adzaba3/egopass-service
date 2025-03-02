package com.rva.egopass.exceptions;

import lombok.Getter;

@Getter
public class DocumentException extends RuntimeException {
    private final String errorCode;

    public DocumentException(String message) {
        super(message);
        this.errorCode = "DOCUMENT_ERROR";
    }

    public DocumentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DOCUMENT_ERROR";
    }

}
