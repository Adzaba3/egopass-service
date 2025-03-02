package com.rva.egopass.mapper;


import com.rva.egopass.dto.UserDTO;
import com.rva.egopass.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setNationality(user.getNationality());
        dto.setPassportNumber(user.getPassportNumber());
        dto.setRole(user.getRole());
        return dto;
    }
}
