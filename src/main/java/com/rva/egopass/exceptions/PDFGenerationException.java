package com.rva.egopass.exceptions;

import lombok.Getter;

@Getter
public class PDFGenerationException extends RuntimeException {
    private final String errorCode;

    public PDFGenerationException(String message) {
        super(message);
        this.errorCode = "PDF_GENERATION_ERROR";
    }

    public PDFGenerationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PDF_GENERATION_ERROR";
    }


}
