package com.example.demo.service;

import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.domain.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
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
        UserDTO userDTO = new UserDTO(null, "testuser", "password123", createdPerson, true, "USER");
        UserDTO createdUser = userService.createUser(userDTO);
        
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
        retrievedUser.setUsername("updateduser");
        retrievedUser.getPerson().setFirstName("Updated");
        UserDTO updatedUser = userService.updateUser(retrievedUser.getId(), retrievedUser);
        assertEquals("updateduser", updatedUser.getUsername());
        assertEquals("Updated", updatedUser.getPerson().getFirstName());
        
        // Verify update
        Optional<UserDTO> afterUpdateOpt = userService.getUserById(updatedUser.getId());
        assertTrue(afterUpdateOpt.isPresent());
        assertEquals("updateduser", afterUpdateOpt.get().getUsername());
        assertEquals("Updated", afterUpdateOpt.get().getPerson().getFirstName());
        
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
        UserDTO user1 = new UserDTO(null, "uniqueusername", "password123", person1, true, "USER");
        UserDTO createdUser1 = userService.createUser(user1);
        
        assertNotNull(createdUser1.getId());
        assertEquals("uniqueusername", createdUser1.getUsername());
        
        // Try to create second user with same username - should throw exception
        UserDTO user2 = new UserDTO(null, "uniqueusername", "password456", person2, true, "USER");
        
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