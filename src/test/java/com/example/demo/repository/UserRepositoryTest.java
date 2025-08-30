package com.example.demo.repository;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
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
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    private User testUser1;
    private User testUser2;
    private Person testPerson1;
    private Person testPerson2;

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
        testPerson2.setLastName("Smith");
        testPerson2.setEmail("jane.smith@example.com");
        testPerson2.setPhoneNumber("0987654321");
        testPerson2.setAddress("456 Oak Ave");

        // Create test users
        testUser1 = new User();
        testUser1.setUsername("johndoe");
        testUser1.setPassword("password123");
        testUser1.setPerson(testPerson1);
        testUser1.setActive(true);
        testUser1.setEmailVerified(true);
        testUser1.setRole(Role.USER);

        testUser2 = new User();
        testUser2.setUsername("janesmith");
        testUser2.setPassword("password456");
        testUser2.setPerson(testPerson2);
        testUser2.setActive(true);
        testUser2.setEmailVerified(false);
        testUser2.setRole(Role.ADMIN);

        // Set bidirectional relationships
        testPerson1.setUser(testUser1);
        testPerson2.setUser(testUser2);
    }

    @Test
    void save_ShouldPersistUser() {
        // Act
        User savedUser = userRepository.save(testUser1);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("johndoe");
        assertThat(savedUser.getPerson().getFirstName()).isEqualTo("John");
    }

    @Test
    void findById_ExistingUser_ShouldReturnUser() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // Act
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("johndoe");
        assertThat(foundUser.get().getPerson().getFirstName()).isEqualTo("John");
    }

    @Test
    void findById_NonExistentUser_ShouldReturnEmpty() {
        // Act
        Optional<User> foundUser = userRepository.findById(999L);

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByUsername_ExistingUsername_ShouldReturnUser() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // Act
        Optional<User> foundUser = userRepository.findByUsername("johndoe");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("johndoe");
        assertThat(foundUser.get().getPerson().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findByUsername_NonExistentUsername_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByUsername_CaseSensitive_ShouldNotMatchDifferentCase() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("JOHNDOE");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByUsername_ExistingUsername_ShouldReturnTrue() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        boolean exists = userRepository.existsByUsername("johndoe");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_NonExistentUsername_ShouldReturnFalse() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_CaseSensitive_ShouldNotMatchDifferentCase() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        boolean exists = userRepository.existsByUsername("JOHNDOE");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
                .containsExactlyInAnyOrder("johndoe", "janesmith");
    }

    @Test
    void delete_ShouldRemoveUser() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // Act
        userRepository.deleteById(savedUser.getId());
        entityManager.flush();

        // Assert
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void update_ShouldModifyUser() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // Act
        Optional<User> userToUpdate = userRepository.findById(savedUser.getId());
        assertThat(userToUpdate).isPresent();
        
        userToUpdate.get().setActive(false);
        userToUpdate.get().setEmailVerified(false);
        userToUpdate.get().setRole(Role.ADMIN);
        
        User updatedUser = userRepository.save(userToUpdate.get());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<User> verifyUpdated = userRepository.findById(savedUser.getId());
        assertThat(verifyUpdated).isPresent();
        assertThat(verifyUpdated.get().isActive()).isFalse();
        assertThat(verifyUpdated.get().isEmailVerified()).isFalse();
        assertThat(verifyUpdated.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void cascadeOperations_ShouldWorkWithPerson() {
        // Act - Save user with person
        User savedUser = userRepository.save(testUser1);
        entityManager.flush();
        entityManager.clear();

        // Assert - Person should also be persisted
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPerson()).isNotNull();
        assertThat(foundUser.get().getPerson().getId()).isNotNull();
        assertThat(foundUser.get().getPerson().getFirstName()).isEqualTo("John");
    }

    @Test
    void uniqueConstraint_ShouldPreventDuplicateUsernames() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        User duplicateUsernameUser = new User();
        duplicateUsernameUser.setUsername("johndoe"); // Same username
        duplicateUsernameUser.setPassword("different-password");
        duplicateUsernameUser.setPerson(testPerson2);
        duplicateUsernameUser.setActive(true);
        duplicateUsernameUser.setEmailVerified(true);
        duplicateUsernameUser.setRole(Role.USER);

        // Act & Assert
        try {
            userRepository.save(duplicateUsernameUser);
            entityManager.flush();
            // If we reach here, the test should fail
            assertThat(false).withFailMessage("Expected unique constraint violation").isTrue();
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertThat(e.getMessage()).containsIgnoringCase("unique");
        }
    }

    @Test
    void lazyLoading_ShouldWorkForPersonRelation() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // Act
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertThat(foundUser).isPresent();
        // Person should be lazily loaded but accessible
        assertThat(foundUser.get().getPerson()).isNotNull();
        assertThat(foundUser.get().getPerson().getFirstName()).isEqualTo("John");
    }

    @Test
    void defaultValues_ShouldBeSetCorrectly() {
        // Arrange
        User userWithDefaults = new User();
        userWithDefaults.setUsername("defaults");
        userWithDefaults.setPassword("password");
        userWithDefaults.setPerson(testPerson1);
        // Not explicitly setting active, emailVerified, or role

        // Act
        User savedUser = userRepository.save(userWithDefaults);

        // Assert
        assertThat(savedUser.isActive()).isTrue(); // Default value
        assertThat(savedUser.isEmailVerified()).isFalse(); // Default value
        assertThat(savedUser.getRole()).isEqualTo(Role.USER); // Default value
    }
}