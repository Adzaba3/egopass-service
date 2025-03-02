package com.rva.egopass.mapper;

import com.rva.egopass.dto.EGoPassDTO;
import com.rva.egopass.dto.EGoPassRequest;
import com.rva.egopass.model.EGoPass;
import com.rva.egopass.model.FlightInfo;
import com.rva.egopass.model.PassengerInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EGoPassMapper {

    EGoPassMapper INSTANCE = Mappers.getMapper(EGoPassMapper.class);

    @Mapping(target = "flightType", source = "flightType")
    @Mapping(target = "flightNumber", source = "flightNumber")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "destination", source = "destination")
    @Mapping(target = "flightCompany", source = "flightCompany")
    FlightInfo mapToFlightInfo(EGoPassRequest request);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "nationality", source = "nationality")
    @Mapping(target = "passportNumber", source = "passportNumber")
    @Mapping(target = "passportIssueDate", source = "passportIssueDate")
    PassengerInfo mapToPassengerInfo(EGoPassRequest request);

    @Mapping(target = "passNumber", source = "passNumber")
    @Mapping(target = "nationality", source = "passengerInfo.nationality")
    @Mapping(target = "flightType", source = "flightInfo.flightType")
    @Mapping(target = "flightCompany", source = "flightInfo.flightCompany")
    @Mapping(target = "flightNumber", source = "flightInfo.flightNumber")
    @Mapping(target = "origin", source = "flightInfo.origin")
    @Mapping(target = "destination", source = "flightInfo.destination")
    @Mapping(target = "issueDate", source = "issueDate")
    @Mapping(target = "qrCodeUrl", expression = "java(generateQrCodeUrl(egopass.getId()))")
    @Mapping(target = "downloadUrl", expression = "java(generateDownloadUrl(egopass.getId()))")
    EGoPassDTO toDto(EGoPass egopass);

    default String generateQrCodeUrl(Long id) {
        return id != null ? "/api/v1/passes/" + id + "/qr-code" : null;
    }

    default String generateDownloadUrl(Long id) {
        return id != null ? "/api/v1/passes/" + id + "/download" : null;
    }
}
