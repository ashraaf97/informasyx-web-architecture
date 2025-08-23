package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.domain.dto.UserCreateDTO;
import com.example.demo.domain.dto.UserDTO;
import com.example.demo.domain.mapper.UserMapper;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.PersonService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonService personService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserCreateDTO testUserCreateDTO;
    private Person testPerson;
    private PersonDTO testPersonDTO;
    private List<User> userList;
    private List<UserDTO> userDTOList;

    @BeforeEach
    void setUp() {
        // Set up test data
        testPerson = new Person(1L, "John", "Doe", "john.doe@example.com", "123-456-7890", "123 Main St", null);
        testPersonDTO = new PersonDTO(1L, "John", "Doe", "john.doe@example.com", "123-456-7890", "123 Main St");
        
        testUser = new User(1L, "johndoe", "password123", testPerson, true, false, Role.USER);
        testUserDTO = new UserDTO(1L, "johndoe", testPersonDTO, true, "USER");
        testUserCreateDTO = new UserCreateDTO(1L, "johndoe", "password123", testPersonDTO, true, "USER");
        
        Person person2 = new Person(2L, "Jane", "Smith", "jane.smith@example.com", "987-654-3210", "456 Oak Ave", null);
        PersonDTO personDTO2 = new PersonDTO(2L, "Jane", "Smith", "jane.smith@example.com", "987-654-3210", "456 Oak Ave");
        
        User user2 = new User(2L, "janesmith", "password456", person2, true, false, Role.USER);
        UserDTO userDTO2 = new UserDTO(2L, "janesmith", personDTO2, true, "USER,ADMIN");
        
        userList = Arrays.asList(testUser, user2);
        userDTOList = Arrays.asList(testUserDTO, userDTO2);
    }

    @Test
    void getAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(userList);
        when(userMapper.toDtoList(userList)).thenReturn(userDTOList);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toDtoList(userList);
    }

    @Test
    void getUserById_Found() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        Optional<UserDTO> result = userService.getUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUserDTO.getId(), result.get().getId());
        assertEquals(testUserDTO.getUsername(), result.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toDto(testUser);
    }

    @Test
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.getUserById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99L);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getUserByUsername_Found() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        Optional<UserDTO> result = userService.getUserByUsername("johndoe");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("johndoe", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("johndoe");
        verify(userMapper, times(1)).toDto(testUser);
    }

    @Test
    void getUserByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.getUserByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        // PersonDTO has ID, so createPerson won't be called
        when(userMapper.toEntity(testUserCreateDTO)).thenReturn(testUser);
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.createUser(testUserCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        verify(userRepository, times(1)).existsByUsername("johndoe");
        // PersonDTO has ID, so createPerson should NOT be called
        verify(personService, never()).createPerson(any());
        verify(personRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toEntity(testUserCreateDTO);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(testUser);
    }

    @Test
    void createUser_UsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(testUserCreateDTO);
        });
        verify(userRepository, times(1)).existsByUsername("johndoe");
        verify(personService, never()).createPerson(any());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(personService.updatePerson(eq(1L), any(PersonDTO.class))).thenReturn(testPersonDTO);
        when(userMapper.toEntity(testUserCreateDTO)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.updateUser(1L, testUserCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).findByUsername("johndoe");
        verify(userRepository, times(1)).findById(1L);
        verify(personService, times(1)).updatePerson(eq(1L), any(PersonDTO.class));
        verify(userMapper, times(1)).toEntity(testUserCreateDTO);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(testUser);
    }

    @Test
    void updateUser_UserNotFound() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            userService.updateUser(99L, testUserCreateDTO);
        });
        verify(userRepository, times(1)).existsById(99L);
        verify(userRepository, never()).findByUsername(any());
        verify(personService, never()).updatePerson(anyLong(), any());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_UsernameAlreadyExists() {
        // Arrange
        User otherUser = new User(2L, "johndoe", "password", null, true, false, Role.USER);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(1L, testUserCreateDTO);
        });
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).findByUsername("johndoe");
        verify(personService, never()).updatePerson(anyLong(), any());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteUser(99L);
        });
        verify(userRepository, times(1)).existsById(99L);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void existsByUsername_True() {
        // Arrange
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername("johndoe");

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername("johndoe");
    }

    @Test
    void existsByUsername_False() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean result = userService.existsByUsername("nonexistent");

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).existsByUsername("nonexistent");
    }
} 