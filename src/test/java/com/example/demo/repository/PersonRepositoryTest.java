package com.example.demo.repository;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class PersonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    private Person testPerson1;
    private Person testPerson2;
    private Person testPerson3;

    @BeforeEach
    void setUp() {
        // Create test persons
        testPerson1 = new Person();
        testPerson1.setFirstName("John");
        testPerson1.setLastName("Doe");
        testPerson1.setEmail("john.doe@example.com");
        testPerson1.setPhoneNumber("1234567890");
        testPerson1.setAddress("123 Main St");

        testPerson2 = new Person();
        testPerson2.setFirstName("Jane");
        testPerson2.setLastName("Doe");
        testPerson2.setEmail("jane.doe@example.com");
        testPerson2.setPhoneNumber("0987654321");
        testPerson2.setAddress("456 Oak Ave");

        testPerson3 = new Person();
        testPerson3.setFirstName("Bob");
        testPerson3.setLastName("Smith");
        testPerson3.setEmail("bob.smith@company.com");
        testPerson3.setPhoneNumber("5555551234");
        testPerson3.setAddress("789 Pine St");

        // For @DataJpaTest, we'll test Person in isolation without User relationships
    }

    @Test
    void save_ShouldPersistPerson() {
        // Act
        Person savedPerson = personRepository.save(testPerson1);

        // Assert
        assertThat(savedPerson.getId()).isNotNull();
        assertThat(savedPerson.getFirstName()).isEqualTo("John");
        assertThat(savedPerson.getLastName()).isEqualTo("Doe");
        assertThat(savedPerson.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findById_ExistingPerson_ShouldReturnPerson() {
        // Arrange
        Person savedPerson = entityManager.persistAndFlush(testPerson1);
        entityManager.clear();

        // Act
        Optional<Person> foundPerson = personRepository.findById(savedPerson.getId());

        // Assert
        assertThat(foundPerson).isPresent();
        assertThat(foundPerson.get().getFirstName()).isEqualTo("John");
        assertThat(foundPerson.get().getLastName()).isEqualTo("Doe");
        assertThat(foundPerson.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findById_NonExistentPerson_ShouldReturnEmpty() {
        // Act
        Optional<Person> foundPerson = personRepository.findById(999L);

        // Assert
        assertThat(foundPerson).isEmpty();
    }

    @Test
    void findByLastName_ExistingLastName_ShouldReturnMatchingPersons() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);
        entityManager.persistAndFlush(testPerson2);
        entityManager.persistAndFlush(testPerson3);

        // Act
        List<Person> persons = personRepository.findByLastName("Doe");

        // Assert
        assertThat(persons).hasSize(2);
        assertThat(persons).extracting(Person::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane");
        assertThat(persons).allMatch(person -> person.getLastName().equals("Doe"));
    }

    @Test
    void findByLastName_NonExistentLastName_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act
        List<Person> persons = personRepository.findByLastName("NonExistent");

        // Assert
        assertThat(persons).isEmpty();
    }

    @Test
    void findByLastName_CaseSensitive_ShouldNotMatchDifferentCase() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act
        List<Person> persons = personRepository.findByLastName("doe"); // lowercase

        // Assert
        assertThat(persons).isEmpty();
    }

    @Test
    void findByFirstNameAndLastName_ExistingNames_ShouldReturnMatchingPersons() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);
        entityManager.persistAndFlush(testPerson2);
        entityManager.persistAndFlush(testPerson3);

        // Act
        List<Person> persons = personRepository.findByFirstNameAndLastName("John", "Doe");

        // Assert
        assertThat(persons).hasSize(1);
        assertThat(persons.get(0).getFirstName()).isEqualTo("John");
        assertThat(persons.get(0).getLastName()).isEqualTo("Doe");
        assertThat(persons.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findByFirstNameAndLastName_NonExistentNames_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act
        List<Person> persons = personRepository.findByFirstNameAndLastName("NonExistent", "Person");

        // Assert
        assertThat(persons).isEmpty();
    }

    @Test
    void findByFirstNameAndLastName_PartialMatch_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act - Only first name matches
        List<Person> persons = personRepository.findByFirstNameAndLastName("John", "Smith");

        // Assert
        assertThat(persons).isEmpty();
    }

    @Test
    void findByEmailContaining_ExistingEmailPart_ShouldReturnMatchingPersons() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);
        entityManager.persistAndFlush(testPerson2);
        entityManager.persistAndFlush(testPerson3);

        // Act
        List<Person> persons = personRepository.findByEmailContaining("doe");

        // Assert
        assertThat(persons).hasSize(2);
        assertThat(persons).extracting(Person::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "jane.doe@example.com");
    }

    @Test
    void findByEmailContaining_NonExistentEmailPart_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act
        List<Person> persons = personRepository.findByEmailContaining("nonexistent");

        // Assert
        assertThat(persons).isEmpty();
    }

    @Test
    void findByEmailContaining_DomainSearch_ShouldReturnMatchingPersons() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);
        entityManager.persistAndFlush(testPerson2);
        entityManager.persistAndFlush(testPerson3);

        // Act
        List<Person> persons = personRepository.findByEmailContaining("example.com");

        // Assert
        assertThat(persons).hasSize(2);
        assertThat(persons).extracting(Person::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void findByEmailContaining_CaseInsensitive_ShouldMatchDifferentCase() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act - This tests if the query is case-insensitive
        List<Person> persons = personRepository.findByEmailContaining("DOE");

        // Assert - This might fail if the database/JPA implementation is case-sensitive
        // The actual behavior depends on the database collation settings
        // For H2, it's typically case-insensitive
        assertThat(persons).hasSize(1);
    }

    @Test
    void findByEmail_ExistingEmail_ShouldReturnPerson() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act
        Optional<Person> person = personRepository.findByEmail("john.doe@example.com");

        // Assert
        assertThat(person).isPresent();
        assertThat(person.get().getFirstName()).isEqualTo("John");
        assertThat(person.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void findByEmail_NonExistentEmail_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act
        Optional<Person> person = personRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(person).isEmpty();
    }

    @Test
    void findByEmail_CaseSensitive_ShouldNotMatchDifferentCase() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);

        // Act
        Optional<Person> person = personRepository.findByEmail("JOHN.DOE@EXAMPLE.COM");

        // Assert - This might pass if database is case-insensitive
        // For most databases, email comparison should be case-insensitive
        assertThat(person).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllPersons() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);
        entityManager.persistAndFlush(testPerson2);
        entityManager.persistAndFlush(testPerson3);

        // Act
        List<Person> persons = personRepository.findAll();

        // Assert
        assertThat(persons).hasSize(3);
        assertThat(persons).extracting(Person::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane", "Bob");
    }

    @Test
    void delete_ShouldRemovePerson() {
        // Arrange
        Person savedPerson = entityManager.persistAndFlush(testPerson3); // Person without user
        entityManager.clear();

        // Act
        personRepository.deleteById(savedPerson.getId());
        entityManager.flush();

        // Assert
        Optional<Person> deletedPerson = personRepository.findById(savedPerson.getId());
        assertThat(deletedPerson).isEmpty();
    }

    @Test
    void update_ShouldModifyPerson() {
        // Arrange
        Person savedPerson = entityManager.persistAndFlush(testPerson3);
        entityManager.clear();

        // Act
        Optional<Person> personToUpdate = personRepository.findById(savedPerson.getId());
        assertThat(personToUpdate).isPresent();
        
        personToUpdate.get().setFirstName("Robert");
        personToUpdate.get().setEmail("robert.smith@company.com");
        personToUpdate.get().setPhoneNumber("5555555555");
        
        Person updatedPerson = personRepository.save(personToUpdate.get());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Person> verifyUpdated = personRepository.findById(savedPerson.getId());
        assertThat(verifyUpdated).isPresent();
        assertThat(verifyUpdated.get().getFirstName()).isEqualTo("Robert");
        assertThat(verifyUpdated.get().getEmail()).isEqualTo("robert.smith@company.com");
        assertThat(verifyUpdated.get().getPhoneNumber()).isEqualTo("5555555555");
        assertThat(verifyUpdated.get().getLastName()).isEqualTo("Smith"); // Unchanged
    }

    @Test
    void personWithoutUser_ShouldBePersisted() {
        // Act - Save person without user
        Person savedPerson = personRepository.save(testPerson3);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Person> foundPerson = personRepository.findById(savedPerson.getId());
        assertThat(foundPerson).isPresent();
        assertThat(foundPerson.get().getUser()).isNull();
        assertThat(foundPerson.get().getFirstName()).isEqualTo("Bob");
    }

    @Test
    void queryMethods_ShouldHandleEmptyDatabase() {
        // Act & Assert - All query methods should handle empty database gracefully
        assertThat(personRepository.findByLastName("AnyName")).isEmpty();
        assertThat(personRepository.findByFirstNameAndLastName("Any", "Name")).isEmpty();
        assertThat(personRepository.findByEmailContaining("any")).isEmpty();
        assertThat(personRepository.findByEmail("any@email.com")).isEmpty();
        assertThat(personRepository.findAll()).isEmpty();
    }

    @Test
    void complexQuery_MultipleFilters_ShouldWork() {
        // Arrange
        entityManager.persistAndFlush(testPerson1);
        entityManager.persistAndFlush(testPerson2);
        entityManager.persistAndFlush(testPerson3);

        // Act - Find all Doe family members
        List<Person> doeFamily = personRepository.findByLastName("Doe");
        
        // Then filter by email containing "jane" (simulating more complex filtering)
        List<Person> janeResults = personRepository.findByEmailContaining("jane");

        // Assert
        assertThat(doeFamily).hasSize(2);
        assertThat(janeResults).hasSize(1);
        assertThat(janeResults.get(0).getFirstName()).isEqualTo("Jane");
    }
}