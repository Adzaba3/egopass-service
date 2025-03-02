package com.rva.egopass.serviceimpl;

import com.rva.egopass.dto.UserDTO;
import com.rva.egopass.exceptions.UserNotFoundException;
import com.rva.egopass.model.User;
import com.rva.egopass.repository.UserRepository;
import com.rva.egopass.mapper.UserMapper;
import com.rva.egopass.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service utilisateur qui gère les opérations CRUD sur les utilisateurs.
 * Ce service permet de récupérer, mettre à jour et supprimer des utilisateurs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Récupère tous les utilisateurs du système.
     *
     * @return Liste des utilisateurs convertis en DTO
     */
    public List<UserDTO> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");

        List<User> users = userRepository.findAll();
        log.debug("Nombre d'utilisateurs trouvés: {}", users.size());

        List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        log.debug("Conversion en DTO terminée pour {} utilisateurs", userDTOs.size());
        return userDTOs;
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id L'identifiant de l'utilisateur
     * @return Les données DTO de l'utilisateur
     * @throws UserNotFoundException Si l'utilisateur n'est pas trouvé
     */
    public UserDTO getUserById(Long id) {
        log.info("Récupération de l'utilisateur avec ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec ID: {}", id);
                    return new UserNotFoundException("Utilisateur non trouvé avec ID : " + id);
                });

        log.debug("Utilisateur trouvé: {}", user.getUsername());
        UserDTO userDTO = userMapper.toDTO(user);
        log.debug("Conversion en DTO terminée pour l'utilisateur ID: {}", id);

        return userDTO;
    }

    /**
     * Met à jour les informations d'un utilisateur existant.
     *
     * @param id L'identifiant de l'utilisateur à mettre à jour
     * @param userDTO Les nouvelles données de l'utilisateur
     * @return Les données DTO de l'utilisateur mis à jour
     * @throws UserNotFoundException Si l'utilisateur n'est pas trouvé
     */
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Mise à jour de l'utilisateur avec ID: {}", id);
        log.debug("Nouvelles données: username={}, email={}", userDTO.getUsername(), userDTO.getEmail());

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec ID: {}", id);
                    return new UserNotFoundException("Utilisateur non trouvé avec ID : " + id);
                });

        log.debug("Utilisateur trouvé: {}, mise à jour des champs", user.getUsername());

        // Sauvegarde des anciennes valeurs pour le logging
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();

        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setNationality(userDTO.getNationality());
        user.setPassportNumber(userDTO.getPassportNumber());

        User updatedUser = userRepository.save(user);
        log.debug("Utilisateur mis à jour avec succès: {} -> {}, email: {} -> {}",
                oldUsername, updatedUser.getUsername(),
                oldEmail, updatedUser.getEmail());

        UserDTO updatedUserDTO = userMapper.toDTO(updatedUser);
        log.info("Mise à jour terminée pour l'utilisateur ID: {}", id);

        return updatedUserDTO;
    }

    /**
     * Supprime un utilisateur du système.
     *
     * @param id L'identifiant de l'utilisateur à supprimer
     * @throws UserNotFoundException Si l'utilisateur n'est pas trouvé
     */
    public void deleteUser(Long id) {
        log.info("Tentative de suppression de l'utilisateur avec ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.error("Suppression impossible: utilisateur non trouvé avec ID: {}", id);
            throw new UserNotFoundException("Utilisateur non trouvé avec ID : " + id);
        }

        log.debug("Utilisateur trouvé, suppression en cours");
        userRepository.deleteById(id);
        log.info("Utilisateur avec ID: {} supprimé avec succès", id);
    }
}