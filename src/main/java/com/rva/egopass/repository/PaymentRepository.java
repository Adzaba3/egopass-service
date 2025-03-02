package com.rva.egopass.repository;

import com.rva.egopass.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionReference(String transactionReference);
//    Optional<Payment> findByReservationReservationId(String reservationId);
}

