package com.rva.egopass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "egopasses")
public class EGoPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String passNumber;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Embedded
    private PassengerInfo passengerInfo;

    @Embedded
    private FlightInfo flightInfo;

    @Lob
    @Column(name = "qr_code_image")
    private byte[] qrCodeImage;

    @Lob
    @Column(name = "pdf_document")
    private byte[] pdfDocument;

    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;

    private boolean validated = false;
    private LocalDateTime validationDate;

    @ManyToOne
    @JoinColumn(name = "validation_user_id")
    private User user;
}
