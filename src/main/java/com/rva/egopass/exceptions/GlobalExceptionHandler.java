package com.rva.egopass.exceptions;


import com.rva.egopass.common.APIResponse;
import com.rva.egopass.common.StatusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);



    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<APIResponse<?>> handleUserNotFoundException(UserNotFoundException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getMessage(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DocumentException.class)
    public ResponseEntity<APIResponse<?>> handleDocumentException(DocumentException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EGoPassNotFoundException.class)
    public ResponseEntity<APIResponse<?>> handleEGoPassNotFoundException(EGoPassNotFoundException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidReservationStateException.class)
    public ResponseEntity<APIResponse<?>> handleInvalidReservationStateException(InvalidReservationStateException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<APIResponse<?>> handlePaymentException(PaymentException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PDFGenerationException.class)
    public ResponseEntity<APIResponse<?>> handlePDFGenerationException(PDFGenerationException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(QRCodeGenerationException.class)
    public ResponseEntity<APIResponse<?>> handleQRCodeGenerationException(QRCodeGenerationException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<APIResponse<?>> handleReservationNotFoundException(ReservationNotFoundException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<APIResponse<?>> handleInvalidRequestException(InvalidRequestException ex) {
        logger.error("Product error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                ex.getMessage(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.badRequest().body(response);
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<?>> handleGlobalException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        APIResponse<?> response = new APIResponse<>(
                StatusConstants.REQUEST_FAILURE_STATUS,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
