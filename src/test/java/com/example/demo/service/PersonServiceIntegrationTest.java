package com.example.demo.service;

import com.example.demo.domain.dto.PersonDTO;
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
class PersonServiceIntegrationTest {

    @Autowired
    private PersonService personService;

    @Test
    void testCrudOperations() {
        // Create a person
        PersonDTO personDTO = new PersonDTO(null, "Test", "Person", "test.person@example.com", "555-123-4567", "123 Test St");
        PersonDTO createdPerson = personService.createPerson(personDTO);
        
        assertNotNull(createdPerson.getId());
        assertEquals("Test", createdPerson.getFirstName());
        assertEquals("Person", createdPerson.getLastName());
        
        // Read
        Optional<PersonDTO> retrievedPersonOpt = personService.getPersonById(createdPerson.getId());
        assertTrue(retrievedPersonOpt.isPresent());
        PersonDTO retrievedPerson = retrievedPersonOpt.get();
        assertEquals(createdPerson.getId(), retrievedPerson.getId());
        assertEquals(createdPerson.getFirstName(), retrievedPerson.getFirstName());
        
        // Update
        retrievedPerson.setFirstName("Updated");
        retrievedPerson.setEmail("updated.person@example.com");
        PersonDTO updatedPerson = personService.updatePerson(retrievedPerson.getId(), retrievedPerson);
        assertEquals("Updated", updatedPerson.getFirstName());
        assertEquals("updated.person@example.com", updatedPerson.getEmail());
        
        // Verify update
        Optional<PersonDTO> afterUpdateOpt = personService.getPersonById(updatedPerson.getId());
        assertTrue(afterUpdateOpt.isPresent());
        assertEquals("Updated", afterUpdateOpt.get().getFirstName());
        
        // Delete
        personService.deletePerson(updatedPerson.getId());
        
        // Verify deletion
        Optional<PersonDTO> afterDeleteOpt = personService.getPersonById(updatedPerson.getId());
        assertFalse(afterDeleteOpt.isPresent());
        
        // Verify delete non-existent throws exception
        assertThrows(EntityNotFoundException.class, () -> {
            personService.deletePerson(9999L);
        });
    }

    @Test
    void testFindOperations() {
        // Create test data
        PersonDTO person1 = personService.createPerson(new PersonDTO(null, "John", "Smith", "john.smith@example.com", "123-456-7890", "123 Main St"));
        PersonDTO person2 = personService.createPerson(new PersonDTO(null, "Jane", "Smith", "jane.smith@example.com", "987-654-3210", "456 Oak Ave"));
        PersonDTO person3 = personService.createPerson(new PersonDTO(null, "Bob", "Johnson", "bob.johnson@example.com", "555-123-4567", "789 Pine Rd"));
        
        // Test findByLastName
        List<PersonDTO> smiths = personService.findByLastName("Smith");
        assertEquals(2, smiths.size());
        assertTrue(smiths.stream().anyMatch(p -> p.getFirstName().equals("John")));
        assertTrue(smiths.stream().anyMatch(p -> p.getFirstName().equals("Jane")));
        
        // Test findByFirstNameAndLastName
        List<PersonDTO> johnSmiths = personService.findByFirstNameAndLastName("John", "Smith");
        assertEquals(1, johnSmiths.size());
        assertEquals("John", johnSmiths.get(0).getFirstName());
        assertEquals("Smith", johnSmiths.get(0).getLastName());
        
        // Test findByEmailContaining
        List<PersonDTO> exampleEmails = personService.findByEmailContaining("example");
        // Note: There may be additional test data from test-data.sql, so check >= 3
        assertTrue(exampleEmails.size() >= 3);
        
        // Test findByEmailContaining with specific match
        List<PersonDTO> bobEmails = personService.findByEmailContaining("bob");
        assertEquals(1, bobEmails.size());
        assertEquals("Bob", bobEmails.get(0).getFirstName());
    }
} 