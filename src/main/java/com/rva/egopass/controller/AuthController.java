package com.rva.egopass.controller;

import com.rva.egopass.common.StatusConstants;
import com.rva.egopass.dto.LoginRequest;
import com.rva.egopass.dto.LoginResponse;
import com.rva.egopass.dto.RegisterRequest;
import com.rva.egopass.service.AuthService;
import com.rva.egopass.common.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour la gestion de l'authentification.
 * Fournit des endpoints REST pour les opérations liées à l'authentification des utilisateurs.
 */
@Tag(name = "Authentification", description = "Endpoints pour l'authentification des utilisateurs")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    @Operation(summary = "Inscription d'un utilisateur", description = "Permet d'inscrire un nouvel utilisateur et retourne un JWT.")
    @ApiResponse(responseCode = "200", description = "Utilisateur inscrit avec succès")
    @ApiResponse(responseCode = "400", description = "Requête invalide ou utilisateur déjà existant")
    @PostMapping("/register")
    public ResponseEntity<APIResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Demande d'inscription reçue pour: {}", request.getUsername());

        LoginResponse loginResponse = authService.register(request);

        APIResponse<LoginResponse> response = new APIResponse<>(
                StatusConstants.REQUEST_SUCCESS_STATUS,
                "Inscription réussie",
                "Utilisateur enregistré avec succès",
                loginResponse,
                null
        );

        return ResponseEntity.ok(response);
    }



    /**
     * Endpoint pour l'authentification des utilisateurs.
     *
     * @param loginRequest Les informations d'identification de l'utilisateur.
     * @return Une réponse contenant un jeton d'authentification et des informations sur l'utilisateur.
     */
    @Operation(
            summary = "Authentifier un utilisateur",
            description = "Vérifie les identifiants et retourne un token JWT avec les informations de l'utilisateur."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification réussie",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Requête invalide (ex: données manquantes)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "401", description = "Identifiants incorrects",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("Tentative de connexion pour l'utilisateur: {}", loginRequest.getUsername());

        // Vérification des identifiants
        LoginResponse loginResponse = authService.authenticate(loginRequest);

        // Liens vers d'autres ressources après authentification
        Map<String, String> links = new HashMap<>();
        links.put("products", "/api/v1/passes");

        // Construction de la réponse
        APIResponse<LoginResponse> response = new APIResponse<>(
                StatusConstants.REQUEST_SUCCESS_STATUS,
                StatusConstants.AUTH_SUCCESS,
                StatusConstants.AUTH_WIN,
                loginResponse,
                links
        );

        return ResponseEntity.ok(response);
    }
}

