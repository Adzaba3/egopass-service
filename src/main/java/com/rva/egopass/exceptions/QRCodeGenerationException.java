package com.rva.egopass.exceptions;

import lombok.Getter;

@Getter
public class QRCodeGenerationException extends RuntimeException {
    private final String errorCode;

    public QRCodeGenerationException(String message) {
        super(message);
        this.errorCode = "QR_CODE_GENERATION_ERROR";
    }

    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "QR_CODE_GENERATION_ERROR";
    }


}
