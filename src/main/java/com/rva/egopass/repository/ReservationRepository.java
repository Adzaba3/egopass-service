package com.rva.egopass.repository;


import com.rva.egopass.enums.ReservationStatus;
import com.rva.egopass.model.Reservation;
import com.rva.egopass.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByUserAndStatus(User user, ReservationStatus status);
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime dateTime);
}

