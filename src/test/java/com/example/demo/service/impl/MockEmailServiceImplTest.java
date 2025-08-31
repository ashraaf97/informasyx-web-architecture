package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MockEmailServiceImplTest {

    @InjectMocks
    private MockEmailServiceImpl mockEmailService;

    private User testUser;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        // Set up test data
        testPerson = new Person();
        testPerson.setId(1L);
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("1234567890");
        testPerson.setAddress("123 Main St");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setPassword("password123");
        testUser.setPerson(testPerson);
        testUser.setActive(true);
        testUser.setEmailVerified(false);
        testUser.setRole(Role.USER);

        // Set the base URL for testing
        ReflectionTestUtils.setField(mockEmailService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void sendEmailVerification_ShouldNotThrowException() {
        // Arrange
        String token = "verification-token-123";

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> {
            mockEmailService.sendEmailVerification(testUser, token);
        });

        // Additional verification could be done by capturing log output
        // But for a mock service, the main test is that it doesn't fail
    }

    @Test
    void sendPasswordResetEmail_ShouldNotThrowException() {
        // Arrange
        String token = "reset-token-123";

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> {
            mockEmailService.sendPasswordResetEmail(testUser, token);
        });
    }

    @Test
    void sendWelcomeEmail_ShouldNotThrowException() {
        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> {
            mockEmailService.sendWelcomeEmail(testUser);
        });
    }

    @Test
    void sendEmailVerification_WithNullToken_ShouldNotThrowException() {
        // Act & Assert - Mock service should be resilient
        assertDoesNotThrow(() -> {
            mockEmailService.sendEmailVerification(testUser, null);
        });
    }

    @Test
    void sendPasswordResetEmail_WithEmptyToken_ShouldNotThrowException() {
        // Act & Assert - Mock service should be resilient
        assertDoesNotThrow(() -> {
            mockEmailService.sendPasswordResetEmail(testUser, "");
        });
    }

    @Test
    void sendEmailVerification_WithUserWithoutPerson_ShouldThrowNullPointerException() {
        // Arrange
        User userWithoutPerson = new User();
        userWithoutPerson.setId(2L);
        userWithoutPerson.setUsername("noperson");
        userWithoutPerson.setPerson(null);

        // Act & Assert - Should throw NPE when trying to access person
        assertThrows(NullPointerException.class, () -> {
            mockEmailService.sendEmailVerification(userWithoutPerson, "token");
        });
    }

    @Test
    void sendPasswordResetEmail_WithUserWithoutPerson_ShouldThrowNullPointerException() {
        // Arrange
        User userWithoutPerson = new User();
        userWithoutPerson.setId(2L);
        userWithoutPerson.setUsername("noperson");
        userWithoutPerson.setPerson(null);

        // Act & Assert - Should throw NPE when trying to access person
        assertThrows(NullPointerException.class, () -> {
            mockEmailService.sendPasswordResetEmail(userWithoutPerson, "token");
        });
    }

    @Test
    void sendWelcomeEmail_WithUserWithoutPerson_ShouldThrowNullPointerException() {
        // Arrange
        User userWithoutPerson = new User();
        userWithoutPerson.setId(2L);
        userWithoutPerson.setUsername("noperson");
        userWithoutPerson.setPerson(null);

        // Act & Assert - Should throw NPE when trying to access person
        assertThrows(NullPointerException.class, () -> {
            mockEmailService.sendWelcomeEmail(userWithoutPerson);
        });
    }

    @Test
    void sendEmailVerification_WithPersonWithoutEmail_ShouldNotThrowException() {
        // Arrange
        testPerson.setEmail(null);

        // Act & Assert - Mock service should handle this gracefully
        assertDoesNotThrow(() -> {
            mockEmailService.sendEmailVerification(testUser, "token");
        });
    }

    @Test
    void sendEmailVerification_WithPersonWithoutFirstName_ShouldNotThrowException() {
        // Arrange
        testPerson.setFirstName(null);

        // Act & Assert - Mock service should handle this gracefully
        assertDoesNotThrow(() -> {
            mockEmailService.sendEmailVerification(testUser, "token");
        });
    }

    @Test
    void sendPasswordResetEmail_WithPersonWithoutEmail_ShouldNotThrowException() {
        // Arrange
        testPerson.setEmail(null);

        // Act & Assert - Mock service should handle this gracefully
        assertDoesNotThrow(() -> {
            mockEmailService.sendPasswordResetEmail(testUser, "token");
        });
    }

    @Test
    void sendWelcomeEmail_WithPersonWithoutFirstName_ShouldNotThrowException() {
        // Arrange
        testPerson.setFirstName(null);

        // Act & Assert - Mock service should handle this gracefully
        assertDoesNotThrow(() -> {
            mockEmailService.sendWelcomeEmail(testUser);
        });
    }

    @Test
    void mockEmailService_WithDifferentBaseUrl_ShouldNotThrowException() {
        // Arrange
        ReflectionTestUtils.setField(mockEmailService, "baseUrl", "https://different-domain.com");

        // Act & Assert - Should work with any base URL
        assertDoesNotThrow(() -> {
            mockEmailService.sendEmailVerification(testUser, "token");
            mockEmailService.sendPasswordResetEmail(testUser, "token");
            mockEmailService.sendWelcomeEmail(testUser);
        });
    }

    @Test
    void mockEmailService_WithNullBaseUrl_ShouldNotThrowException() {
        // Arrange
        ReflectionTestUtils.setField(mockEmailService, "baseUrl", null);

        // Act & Assert - Should handle null base URL gracefully
        assertDoesNotThrow(() -> {
            mockEmailService.sendEmailVerification(testUser, "token");
            mockEmailService.sendPasswordResetEmail(testUser, "token");
            mockEmailService.sendWelcomeEmail(testUser);
        });
    }

    @Test
    void sendEmailVerification_WithSpecialCharactersInToken_ShouldNotThrowException() {
        // Arrange
        String specialToken = "token-with-special-chars!@#$%^&*()_+={}[]|\\:;\"'<>,.?/~`";

        // Act & Assert - Should handle special characters in token
        assertDoesNotThrow(() -> {
            mockEmailService.sendEmailVerification(testUser, specialToken);
        });
    }

    @Test
    void sendPasswordResetEmail_WithVeryLongToken_ShouldNotThrowException() {
        // Arrange
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longToken.append("a");
        }

        // Act & Assert - Should handle very long tokens
        assertDoesNotThrow(() -> {
            mockEmailService.sendPasswordResetEmail(testUser, longToken.toString());
        });
    }

    @Test
    void allEmailMethods_WithCompleteUserData_ShouldExecuteSuccessfully() {
        // Arrange - User with all complete data
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("1234567890");
        testPerson.setAddress("123 Main Street");

        // Act & Assert - All methods should work without exceptions
        assertDoesNotThrow(() -> {
            mockEmailService.sendEmailVerification(testUser, "verification-token");
            mockEmailService.sendPasswordResetEmail(testUser, "reset-token");
            mockEmailService.sendWelcomeEmail(testUser);
        });
    }
}