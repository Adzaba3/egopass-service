package com.rva.egopass.service;

import com.rva.egopass.dto.LoginRequest;
import com.rva.egopass.dto.LoginResponse;
import com.rva.egopass.dto.RegisterRequest;
import com.rva.egopass.enums.Role;
import com.rva.egopass.model.User;
import com.rva.egopass.repository.UserRepository;
import com.rva.egopass.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse register(RegisterRequest request) {
        log.info("Enregistrement d'un nouvel utilisateur: {}", request.getUsername());

        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new IllegalArgumentException("Le nom d'utilisateur ou l'email est déjà pris.");
        }

        // Création de l'utilisateur
        User user = User.builder()
                .username(request.getUsername())
               .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .nationality(request.getNationality())
                .passportNumber(request.getPassportNumber())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword())) // Hachage du mot de passe
                .role(request.getRole() != null ? request.getRole() : Role.ROLE_USER) // Role par défaut : USER
                .build();

        // Sauvegarde en base
        userRepository.save(user);

        // Génération du JWT
        String jwt = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .roles(user.getAuthorities())
                .build();
    }

    public LoginResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String jwt = jwtService.generateToken(userDetails);

            return LoginResponse.builder()
                    .token(jwt)
                    .username(userDetails.getUsername())
                    .roles(userDetails.getAuthorities())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Erreur d'authentification: {}", e.getMessage());
            throw new AuthenticationException("Nom d'utilisateur ou mot de passe incorrect") {};
        }
    }
}

