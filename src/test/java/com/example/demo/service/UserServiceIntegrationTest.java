package com.example.demo.service;

import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.domain.dto.UserCreateDTO;
import com.example.demo.domain.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-integration.properties")
@DirtiesContext
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PersonService personService;

    @Test
    void testCrudOperations() {
        // Create a person first
        PersonDTO personDTO = new PersonDTO(null, "Test", "User", "test.user@example.com", "555-123-4567", "123 Test St");
        PersonDTO createdPerson = personService.createPerson(personDTO);
        
        // Create a user
        UserCreateDTO userCreateDTO = new UserCreateDTO(null, "testuser", "password123", createdPerson, true, "USER");
        UserDTO createdUser = userService.createUser(userCreateDTO);
        
        assertNotNull(createdUser.getId());
        assertEquals("testuser", createdUser.getUsername());
        assertNotNull(createdUser.getPerson());
        assertEquals(createdPerson.getId(), createdUser.getPerson().getId());
        
        // Read
        Optional<UserDTO> retrievedUserOpt = userService.getUserById(createdUser.getId());
        assertTrue(retrievedUserOpt.isPresent());
        UserDTO retrievedUser = retrievedUserOpt.get();
        assertEquals(createdUser.getId(), retrievedUser.getId());
        assertEquals(createdUser.getUsername(), retrievedUser.getUsername());
        
        // Get by username
        Optional<UserDTO> userByUsernameOpt = userService.getUserByUsername("testuser");
        assertTrue(userByUsernameOpt.isPresent());
        assertEquals(createdUser.getId(), userByUsernameOpt.get().getId());
        
        // Update
        UserCreateDTO updateDTO = new UserCreateDTO(
            retrievedUser.getId(),
            "updateduser",
            "newpassword123",
            retrievedUser.getPerson(),
            true,
            "USER"
        );
        
        // Apply update
        UserDTO updatedUser = userService.updateUser(retrievedUser.getId(), updateDTO);
        assertEquals("updateduser", updatedUser.getUsername());
        
        // Verify update
        Optional<UserDTO> afterUpdateOpt = userService.getUserById(updatedUser.getId());
        assertTrue(afterUpdateOpt.isPresent());
        assertEquals("updateduser", afterUpdateOpt.get().getUsername());
        
        // Update person info via User update
        PersonDTO updatedPersonDTO = new PersonDTO(
            updatedUser.getPerson().getId(),
            "Updated",
            updatedUser.getPerson().getLastName(),
            updatedUser.getPerson().getEmail(),
            updatedUser.getPerson().getPhoneNumber(),
            updatedUser.getPerson().getAddress()
        );
        
        UserCreateDTO userWithUpdatedPerson = new UserCreateDTO(
            updatedUser.getId(),
            updatedUser.getUsername(),
            "newpassword123",
            updatedPersonDTO,
            updatedUser.isActive(),
            updatedUser.getRoles()
        );
        
        UserDTO userWithUpdatedPersonResult = userService.updateUser(updatedUser.getId(), userWithUpdatedPerson);
        assertEquals("Updated", userWithUpdatedPersonResult.getPerson().getFirstName());
        
        // Delete
        userService.deleteUser(updatedUser.getId());
        
        // Verify deletion
        Optional<UserDTO> afterDeleteOpt = userService.getUserById(updatedUser.getId());
        assertFalse(afterDeleteOpt.isPresent());
        
        // Verify delete non-existent throws exception
        assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteUser(9999L);
        });
    }

    @Test
    void testUsernameConstraints() {
        // Create initial test data
        PersonDTO person1 = personService.createPerson(new PersonDTO(null, "Test", "One", "test.one@example.com", "123-456-7890", null));
        PersonDTO person2 = personService.createPerson(new PersonDTO(null, "Test", "Two", "test.two@example.com", "987-654-3210", null));
        
        // Create first user
        UserCreateDTO user1 = new UserCreateDTO(null, "uniqueusername", "password123", person1, true, "USER");
        UserDTO createdUser1 = userService.createUser(user1);
        
        assertNotNull(createdUser1.getId());
        assertEquals("uniqueusername", createdUser1.getUsername());
        
        // Try to create second user with same username - should throw exception
        UserCreateDTO user2 = new UserCreateDTO(null, "uniqueusername", "password456", person2, true, "USER");
        
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user2);
        });
        
        // Check that username exists
        assertTrue(userService.existsByUsername("uniqueusername"));
        assertFalse(userService.existsByUsername("nonexistentuser"));
        
        // Get all users
        List<UserDTO> allUsers = userService.getAllUsers();
        assertTrue(allUsers.size() >= 1);
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("uniqueusername")));
    }
} 