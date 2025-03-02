package com.rva.egopass.serviceimpl;

import com.rva.egopass.dto.*;
import com.rva.egopass.enums.PaymentMethod;
import com.rva.egopass.enums.PaymentStatus;
import com.rva.egopass.exceptions.PaymentException;
import com.rva.egopass.exceptions.ReservationNotFoundException;
import com.rva.egopass.model.Payment;
import com.rva.egopass.model.Reservation;
import com.rva.egopass.repository.PaymentRepository;
import com.rva.egopass.repository.ReservationRepository;
import com.rva.egopass.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Reservation reservation;
    private Payment payment;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        reservation.setId(1L);

        payment = new Payment();
        payment.setId(1L);
        payment.setReservation(reservation);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(BigDecimal.valueOf(25.0));
        payment.setCreatedAt(LocalDateTime.now());
        payment.setTransactionReference(UUID.randomUUID().toString());
    }

    @Test
    void testInitiatePayment_Success() {
        PaymentInitiationRequest request = new PaymentInitiationRequest(1L, PaymentMethod.MOBILE_MONEY, null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentGatewayService.initiateMobileMoneyPayment(any(Payment.class)))
                .thenReturn(new PaymentInitiationResponse(1L, "TXN123", "https://payment.com", "instructions"));

        PaymentInitiationResponse response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertEquals("TXN123", response.getTransactionReference());
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testInitiatePayment_ReservationNotFound() {
        PaymentInitiationRequest request = new PaymentInitiationRequest(2L, PaymentMethod.MOBILE_MONEY, null);
        when(reservationRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> paymentService.initiatePayment(request));
    }

    @Test
    void testVerifyPayment_Success() {
        PaymentCallbackRequest callback = new PaymentCallbackRequest(payment.getTransactionReference(), 1L, "COMPLETED", null);
        when(paymentRepository.findByTransactionReference(callback.getTransactionReference())).thenReturn(Optional.of(payment));
        when(paymentGatewayService.verifyPayment(callback)).thenReturn(true);

        boolean isValid = paymentService.verifyPayment(callback);

        assertTrue(isValid);
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void testVerifyPayment_Failed() {
        PaymentCallbackRequest callback = new PaymentCallbackRequest(payment.getTransactionReference(), 1L, "FAILED", null);
        when(paymentRepository.findByTransactionReference(callback.getTransactionReference())).thenReturn(Optional.of(payment));
        when(paymentGatewayService.verifyPayment(callback)).thenReturn(false);

        boolean isValid = paymentService.verifyPayment(callback);

        assertFalse(isValid);
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void testGetPaymentStatus_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentStatusResponse response = paymentService.getPaymentStatus(1L);

        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING, response.getStatus());
    }

    @Test
    void testGetPaymentStatus_PaymentNotFound() {
        when(paymentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(PaymentException.class, () -> paymentService.getPaymentStatus(2L));
    }
}
