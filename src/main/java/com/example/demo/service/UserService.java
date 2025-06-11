package com.example.demo.service;

import com.example.demo.domain.dto.UserCreateDTO;
import com.example.demo.domain.dto.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDTO> getAllUsers();
    Optional<UserDTO> getUserById(Long id);
    Optional<UserDTO> getUserByUsername(String username);
    UserDTO createUser(UserCreateDTO userCreateDTO);
    UserDTO updateUser(Long id, UserCreateDTO userCreateDTO);
    void deleteUser(Long id);
    boolean existsByUsername(String username);
}
