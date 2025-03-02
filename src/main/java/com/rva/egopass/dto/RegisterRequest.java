package com.rva.egopass.dto;


import com.rva.egopass.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String nationality;

    @NotBlank
    private String passportNumber;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 10, max = 15)
    private String phone;

    @NotBlank
    @Size(min = 6)
    private String password;

    private Role role;
}

