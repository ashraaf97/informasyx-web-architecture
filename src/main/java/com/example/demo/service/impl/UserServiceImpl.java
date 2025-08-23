package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.domain.dto.UserCreateDTO;
import com.example.demo.domain.dto.UserDTO;
import com.example.demo.domain.mapper.UserMapper;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.PersonService;
import com.example.demo.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
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
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + userCreateDTO.getUsername());
        }

        // Create the person first if it doesn't have an ID yet
        if (userCreateDTO.getPerson() != null && userCreateDTO.getPerson().getId() == null) {
            PersonDTO savedPersonDTO = personService.createPerson(userCreateDTO.getPerson());
            userCreateDTO.setPerson(savedPersonDTO); // Use the fully populated PersonDTO
        }

        User user = userMapper.toEntity(userCreateDTO);
        
        // If person has an ID, fetch it from database to avoid detached entity issues
        if (userCreateDTO.getPerson() != null && userCreateDTO.getPerson().getId() != null) {
            Optional<Person> existingPerson = personRepository.findById(userCreateDTO.getPerson().getId());
            if (existingPerson.isPresent()) {
                user.setPerson(existingPerson.get());
            }
        }
        
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserCreateDTO userCreateDTO) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        // Check if username already exists (and it's not the current user)
        Optional<User> existingUserByUsername = userRepository.findByUsername(userCreateDTO.getUsername());
        if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(id)) {
            throw new IllegalArgumentException("Username already exists: " + userCreateDTO.getUsername());
        }

        // Get existing user to access its person
        User existingUser = userRepository.findById(id).orElseThrow();
        
        // Update the person if it exists
        if (userCreateDTO.getPerson() != null) {
            if (existingUser.getPerson() != null) {
                // Update existing person
                PersonDTO updatedPersonDTO = personService.updatePerson(
                        existingUser.getPerson().getId(), 
                        userCreateDTO.getPerson()
                );
                userCreateDTO.setPerson(updatedPersonDTO); // Use the fully populated PersonDTO
            } else {
                // Create new person
                PersonDTO savedPersonDTO = personService.createPerson(userCreateDTO.getPerson());
                userCreateDTO.setPerson(savedPersonDTO); // Use the fully populated PersonDTO
            }
        }

        // Ensure ID is set correctly
        userCreateDTO.setId(id);
        
        User user = userMapper.toEntity(userCreateDTO);
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
