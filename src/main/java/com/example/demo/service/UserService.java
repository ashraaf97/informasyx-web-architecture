package com.example.demo.service;

import com.example.demo.domain.dto.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDTO> getAllUsers();
    Optional<UserDTO> getUserById(Long id);
    Optional<UserDTO> getUserByUsername(String username);
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    boolean existsByUsername(String username);
}
