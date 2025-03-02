package com.rva.egopass.serviceimpl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.rva.egopass.dto.EGoPassDTO;
import com.rva.egopass.dto.EGoPassRequest;
import com.rva.egopass.enums.ReservationStatus;
import com.rva.egopass.exceptions.EGoPassNotFoundException;
import com.rva.egopass.exceptions.ReservationNotFoundException;
import com.rva.egopass.exceptions.UserNotFoundException;
import com.rva.egopass.mapper.EGoPassMapper;
import com.rva.egopass.model.EGoPass;
import com.rva.egopass.model.Reservation;
import com.rva.egopass.model.User;
import com.rva.egopass.repository.EGoPassRepository;
import com.rva.egopass.repository.ReservationRepository;
import com.rva.egopass.repository.UserRepository;
import com.rva.egopass.service.PDFGeneratorService;
import com.rva.egopass.service.QRCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EGoPassServiceImplTest {

    @Mock
    private EGoPassRepository eGoPassRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QRCodeService qrCodeService;

    @Mock
    private PDFGeneratorService pdfGeneratorService;

    @Mock
    private EGoPassMapper eGoPassMapper;

    @InjectMocks
    private EGoPassServiceImpl eGoPassService;

    private User user;
    private Reservation reservation;
    private EGoPass eGoPass;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        eGoPass = new EGoPass();
        eGoPass.setId(1L);
        eGoPass.setUser(user);
        eGoPass.setIssueDate(LocalDateTime.now());
    }

    @Test
    void createReservation_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        EGoPassRequest request = new EGoPassRequest();
        assertThrows(UserNotFoundException.class, () -> eGoPassService.createReservation(request, 1L));
    }

    @Test
    void generateEGoPassFromReservation_shouldThrowException_whenReservationNotFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ReservationNotFoundException.class, () -> eGoPassService.generateEGoPassFromReservation(1L));
    }

    @Test
    void getEGoPass_shouldReturnDTO_whenEGoPassExists() {
        when(eGoPassRepository.findById(1L)).thenReturn(Optional.of(eGoPass));
        EGoPassDTO dto = new EGoPassDTO();
        when(eGoPassMapper.toDto(eGoPass)).thenReturn(dto);

        EGoPassDTO result = eGoPassService.getEGoPass(1L);

        assertNotNull(result);
        verify(eGoPassRepository, times(1)).findById(1L);
    }

    @Test
    void getEGoPass_shouldThrowException_whenEGoPassNotFound() {
        when(eGoPassRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EGoPassNotFoundException.class, () -> eGoPassService.getEGoPass(1L));
    }

    @Test
    void generatePDF_shouldReturnPDF_whenEGoPassExists() throws Exception {
        byte[] pdfData = new byte[]{1, 2, 3};
        eGoPass.setPdfDocument(pdfData);

        when(eGoPassRepository.findById(1L)).thenReturn(Optional.of(eGoPass));

        ByteArrayResource result = eGoPassService.generatePDF(1L);

        assertNotNull(result);
        assertArrayEquals(pdfData, result.getByteArray());
    }
}