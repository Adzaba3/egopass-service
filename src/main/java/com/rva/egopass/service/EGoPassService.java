package com.rva.egopass.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rva.egopass.dto.EGoPassDTO;
import com.rva.egopass.dto.EGoPassRequest;
import com.rva.egopass.model.EGoPass;
import jakarta.annotation.Resource;
import org.springframework.core.io.ByteArrayResource;

public interface EGoPassService {
    Long createReservation(EGoPassRequest request, Long userId);
    void generateEGoPassFromReservation(Long reservationId) throws JsonProcessingException;
    ByteArrayResource generatePDF(Long eGoPassId) throws Exception;
    EGoPassDTO getEGoPass(Long id);
}
