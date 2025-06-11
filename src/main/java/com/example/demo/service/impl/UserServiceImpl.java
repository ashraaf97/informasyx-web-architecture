package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.domain.dto.UserDTO;
import com.example.demo.domain.mapper.UserMapper;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.PersonService;
import com.example.demo.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PersonService personService;
    private final UserMapper userMapper;

    @Override
    public List<UserDTO> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    @Override
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + userDTO.getUsername());
        }

        // Create the person first if it doesn't exist
        PersonDTO personDTO = null;
        if (userDTO.getPerson() != null) {
            personDTO = personService.createPerson(userDTO.getPerson());
        }

        User user = userMapper.toEntity(userDTO);
        if (personDTO != null) {
            Person person = new Person();
            person.setId(personDTO.getId());
            user.setPerson(person);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        // Check if username already exists (and it's not the current user)
        Optional<User> existingUserByUsername = userRepository.findByUsername(userDTO.getUsername());
        if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(id)) {
            throw new IllegalArgumentException("Username already exists: " + userDTO.getUsername());
        }

        // Update the person if it exists
        if (userDTO.getPerson() != null) {
            User existingUser = userRepository.findById(id).orElseThrow();
            if (existingUser.getPerson() != null) {
                personService.updatePerson(existingUser.getPerson().getId(), userDTO.getPerson());
            } else {
                PersonDTO personDTO = personService.createPerson(userDTO.getPerson());
                userDTO.getPerson().setId(personDTO.getId());
            }
        }

        User user = userMapper.toEntity(userDTO);
        user.setId(id);
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
