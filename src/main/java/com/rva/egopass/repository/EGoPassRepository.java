package com.rva.egopass.repository;


import com.rva.egopass.model.EGoPass;
import com.rva.egopass.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EGoPassRepository extends JpaRepository<EGoPass, Long> {
    List<EGoPass> findByReservationUser(User user);

    @Query("SELECT e FROM EGoPass e WHERE e.reservation.user = ?1 ORDER BY e.issueDate DESC")
    Page<EGoPass> findByUserOrderByIssueDateDesc(User user, Pageable pageable);

    List<EGoPass> findByValidatedFalseAndExpiryDateBefore(LocalDateTime dateTime);

    @Query("SELECT e FROM EGoPass e WHERE " +
            "LOWER(e.reservation.passengerInfo.firstName) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
            "LOWER(e.reservation.passengerInfo.lastName) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
            "LOWER(e.reservation.flightInfo.flightNumber) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<EGoPass> searchEGoPasses(String searchTerm);
}
