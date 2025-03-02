package com.rva.egopass.dto;


import com.rva.egopass.enums.Role;
import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String nationality;
    private String passportNumber;
    private String email;
    private String phone;
    private Role role;
}
