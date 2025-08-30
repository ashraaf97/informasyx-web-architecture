package com.example.demo.repository;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.VerificationToken;
import com.example.demo.domain.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class VerificationTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    private User testUser1;
    private User testUser2;
    private VerificationToken emailToken1;
    private VerificationToken passwordToken1;
    private VerificationToken emailToken2;

    @BeforeEach
    void setUp() {
        // Create test persons
        Person testPerson1 = new Person();
        testPerson1.setFirstName("John");
        testPerson1.setLastName("Doe");
        testPerson1.setEmail("john.doe@example.com");
        testPerson1.setPhoneNumber("1234567890");
        testPerson1.setAddress("123 Main St");

        Person testPerson2 = new Person();
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
        testUser1.setEmailVerified(false);
        testUser1.setRole(Role.USER);

        testUser2 = new User();
        testUser2.setUsername("janesmith");
        testUser2.setPassword("password456");
        testUser2.setPerson(testPerson2);
        testUser2.setActive(true);
        testUser2.setEmailVerified(false);
        testUser2.setRole(Role.ADMIN);

        testPerson1.setUser(testUser1);
        testPerson2.setUser(testUser2);

        // Create verification tokens
        emailToken1 = new VerificationToken();
        emailToken1.setToken("email-token-1-" + UUID.randomUUID());
        emailToken1.setUser(testUser1);
        emailToken1.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        emailToken1.setExpiryDate(LocalDateTime.now().plusHours(24));
        emailToken1.setUsed(false);
        emailToken1.setCreatedDate(LocalDateTime.now());

        passwordToken1 = new VerificationToken();
        passwordToken1.setToken("password-token-1-" + UUID.randomUUID());
        passwordToken1.setUser(testUser1);
        passwordToken1.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        passwordToken1.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordToken1.setUsed(false);
        passwordToken1.setCreatedDate(LocalDateTime.now());

        emailToken2 = new VerificationToken();
        emailToken2.setToken("email-token-2-" + UUID.randomUUID());
        emailToken2.setUser(testUser2);
        emailToken2.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        emailToken2.setExpiryDate(LocalDateTime.now().plusHours(24));
        emailToken2.setUsed(false);
        emailToken2.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void save_ShouldPersistVerificationToken() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        
        // Act
        VerificationToken savedToken = verificationTokenRepository.save(emailToken1);

        // Assert
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(emailToken1.getToken());
        assertThat(savedToken.getTokenType()).isEqualTo(VerificationToken.TokenType.EMAIL_VERIFICATION);
        assertThat(savedToken.getUser().getUsername()).isEqualTo("johndoe");
    }

    @Test
    void findByToken_ExistingToken_ShouldReturnToken() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        VerificationToken savedToken = entityManager.persistAndFlush(emailToken1);
        entityManager.clear();

        // Act
        Optional<VerificationToken> foundToken = verificationTokenRepository.findByToken(savedToken.getToken());

        // Assert
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo(savedToken.getToken());
        assertThat(foundToken.get().getTokenType()).isEqualTo(VerificationToken.TokenType.EMAIL_VERIFICATION);
        assertThat(foundToken.get().getUser().getUsername()).isEqualTo("johndoe");
    }

    @Test
    void findByToken_NonExistentToken_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(emailToken1);

        // Act
        Optional<VerificationToken> foundToken = verificationTokenRepository.findByToken("non-existent-token");

        // Assert
        assertThat(foundToken).isEmpty();
    }

    @Test
    void findByUserAndTokenType_ExistingUserAndType_ShouldReturnTokens() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(emailToken1);
        entityManager.persistAndFlush(passwordToken1);

        // Act
        List<VerificationToken> tokens = verificationTokenRepository.findByUserAndTokenType(
                testUser1, VerificationToken.TokenType.EMAIL_VERIFICATION);

        // Assert
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo(emailToken1.getToken());
        assertThat(tokens.get(0).getTokenType()).isEqualTo(VerificationToken.TokenType.EMAIL_VERIFICATION);
    }

    @Test
    void findByUserAndTokenType_MultipleTokensSameType_ShouldReturnAll() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        
        // Create another email verification token for the same user
        VerificationToken anotherEmailToken = new VerificationToken();
        anotherEmailToken.setToken("another-email-token-" + UUID.randomUUID());
        anotherEmailToken.setUser(testUser1);
        anotherEmailToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        anotherEmailToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        anotherEmailToken.setUsed(false);
        anotherEmailToken.setCreatedDate(LocalDateTime.now());

        entityManager.persistAndFlush(emailToken1);
        entityManager.persistAndFlush(anotherEmailToken);
        entityManager.persistAndFlush(passwordToken1);

        // Act
        List<VerificationToken> emailTokens = verificationTokenRepository.findByUserAndTokenType(
                testUser1, VerificationToken.TokenType.EMAIL_VERIFICATION);

        // Assert
        assertThat(emailTokens).hasSize(2);
        assertThat(emailTokens).extracting(VerificationToken::getTokenType)
                .containsOnly(VerificationToken.TokenType.EMAIL_VERIFICATION);
    }

    @Test
    void findByUserAndTokenType_NonExistentType_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(emailToken1); // Only email verification token

        // Act
        List<VerificationToken> tokens = verificationTokenRepository.findByUserAndTokenType(
                testUser1, VerificationToken.TokenType.PASSWORD_RESET);

        // Assert
        assertThat(tokens).isEmpty();
    }

    @Test
    void deleteByExpiryDateBeforeAndUsedTrue_ShouldRemoveExpiredUsedTokens() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        
        // Create expired and used token
        VerificationToken expiredUsedToken = new VerificationToken();
        expiredUsedToken.setToken("expired-used-token-" + UUID.randomUUID());
        expiredUsedToken.setUser(testUser1);
        expiredUsedToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        expiredUsedToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired
        expiredUsedToken.setUsed(true); // Used
        expiredUsedToken.setCreatedDate(LocalDateTime.now().minusHours(2));

        // Create expired but not used token
        VerificationToken expiredUnusedToken = new VerificationToken();
        expiredUnusedToken.setToken("expired-unused-token-" + UUID.randomUUID());
        expiredUnusedToken.setUser(testUser1);
        expiredUnusedToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        expiredUnusedToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired
        expiredUnusedToken.setUsed(false); // Not used
        expiredUnusedToken.setCreatedDate(LocalDateTime.now().minusHours(2));

        entityManager.persistAndFlush(expiredUsedToken);
        entityManager.persistAndFlush(expiredUnusedToken);
        entityManager.persistAndFlush(emailToken1); // Valid token

        long countBefore = verificationTokenRepository.count();
        
        // Act
        verificationTokenRepository.deleteByExpiryDateBeforeAndUsedTrue(LocalDateTime.now());
        entityManager.flush();

        // Assert
        long countAfter = verificationTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - 1); // Only expired used token should be deleted
        
        // Verify the correct tokens remain
        Optional<VerificationToken> remainingValid = verificationTokenRepository.findByToken(emailToken1.getToken());
        Optional<VerificationToken> remainingExpiredUnused = verificationTokenRepository.findByToken(expiredUnusedToken.getToken());
        Optional<VerificationToken> deletedExpiredUsed = verificationTokenRepository.findByToken(expiredUsedToken.getToken());
        
        assertThat(remainingValid).isPresent();
        assertThat(remainingExpiredUnused).isPresent();
        assertThat(deletedExpiredUsed).isEmpty();
    }

    @Test
    void deleteByUserAndTokenType_ShouldRemoveSpecificTokens() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.persistAndFlush(emailToken1);
        entityManager.persistAndFlush(passwordToken1);
        entityManager.persistAndFlush(emailToken2);

        long countBefore = verificationTokenRepository.count();

        // Act - Delete all email verification tokens for testUser1
        verificationTokenRepository.deleteByUserAndTokenType(
                testUser1, VerificationToken.TokenType.EMAIL_VERIFICATION);
        entityManager.flush();

        // Assert
        long countAfter = verificationTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - 1); // Only emailToken1 should be deleted

        // Verify the correct tokens remain
        Optional<VerificationToken> deletedEmailToken1 = verificationTokenRepository.findByToken(emailToken1.getToken());
        Optional<VerificationToken> remainingPasswordToken1 = verificationTokenRepository.findByToken(passwordToken1.getToken());
        Optional<VerificationToken> remainingEmailToken2 = verificationTokenRepository.findByToken(emailToken2.getToken());

        assertThat(deletedEmailToken1).isEmpty();
        assertThat(remainingPasswordToken1).isPresent();
        assertThat(remainingEmailToken2).isPresent();
    }

    @Test
    void tokenValidation_ShouldWorkCorrectly() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Create expired token
        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setToken("expired-token-" + UUID.randomUUID());
        expiredToken.setUser(testUser1);
        expiredToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        expiredToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired
        expiredToken.setUsed(false);
        expiredToken.setCreatedDate(LocalDateTime.now().minusHours(2));

        // Create used token
        VerificationToken usedToken = new VerificationToken();
        usedToken.setToken("used-token-" + UUID.randomUUID());
        usedToken.setUser(testUser1);
        usedToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        usedToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Not expired
        usedToken.setUsed(true); // Used
        usedToken.setCreatedDate(LocalDateTime.now());

        VerificationToken savedValidToken = verificationTokenRepository.save(emailToken1);
        VerificationToken savedExpiredToken = verificationTokenRepository.save(expiredToken);
        VerificationToken savedUsedToken = verificationTokenRepository.save(usedToken);

        // Act & Assert
        assertThat(savedValidToken.isValid()).isTrue();
        assertThat(savedValidToken.isExpired()).isFalse();

        assertThat(savedExpiredToken.isValid()).isFalse();
        assertThat(savedExpiredToken.isExpired()).isTrue();

        assertThat(savedUsedToken.isValid()).isFalse();
        assertThat(savedUsedToken.isExpired()).isFalse();
    }

    @Test
    void uniqueConstraint_ShouldPreventDuplicateTokens() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        VerificationToken savedToken = entityManager.persistAndFlush(emailToken1);

        VerificationToken duplicateToken = new VerificationToken();
        duplicateToken.setToken(savedToken.getToken()); // Same token
        duplicateToken.setUser(testUser1);
        duplicateToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        duplicateToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        duplicateToken.setUsed(false);
        duplicateToken.setCreatedDate(LocalDateTime.now());

        // Act & Assert
        try {
            verificationTokenRepository.save(duplicateToken);
            entityManager.flush();
            // If we reach here, the test should fail
            assertThat(false).withFailMessage("Expected unique constraint violation").isTrue();
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertThat(e.getMessage()).containsIgnoringCase("unique");
        }
    }

    @Test
    void cascadeOperations_ShouldNotDeleteUser() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        VerificationToken savedToken = entityManager.persistAndFlush(emailToken1);
        entityManager.clear();

        // Act - Delete token
        verificationTokenRepository.deleteById(savedToken.getId());
        entityManager.flush();

        // Assert - User should still exist
        Optional<VerificationToken> deletedToken = verificationTokenRepository.findById(savedToken.getId());
        assertThat(deletedToken).isEmpty();

        // User should still exist (no cascade delete)
        Optional<User> user = entityManager.find(User.class, testUser1.getId()) != null ? 
                Optional.of(entityManager.find(User.class, testUser1.getId())) : Optional.empty();
        assertThat(user).isPresent();
    }

    @Test
    void findAll_ShouldReturnAllTokens() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.persistAndFlush(emailToken1);
        entityManager.persistAndFlush(passwordToken1);
        entityManager.persistAndFlush(emailToken2);

        // Act
        List<VerificationToken> tokens = verificationTokenRepository.findAll();

        // Assert
        assertThat(tokens).hasSize(3);
        assertThat(tokens).extracting(VerificationToken::getTokenType)
                .containsExactlyInAnyOrder(
                        VerificationToken.TokenType.EMAIL_VERIFICATION,
                        VerificationToken.TokenType.PASSWORD_RESET,
                        VerificationToken.TokenType.EMAIL_VERIFICATION
                );
    }

    @Test
    void defaultValues_ShouldBeSetCorrectly() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        
        VerificationToken tokenWithDefaults = new VerificationToken();
        tokenWithDefaults.setToken("default-token-" + UUID.randomUUID());
        tokenWithDefaults.setUser(testUser1);
        tokenWithDefaults.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        tokenWithDefaults.setExpiryDate(LocalDateTime.now().plusHours(24));
        // Not explicitly setting used or createdDate

        // Act
        VerificationToken savedToken = verificationTokenRepository.save(tokenWithDefaults);

        // Assert
        assertThat(savedToken.isUsed()).isFalse(); // Default value
        assertThat(savedToken.getCreatedDate()).isNotNull(); // Should be set automatically
        assertThat(savedToken.getCreatedDate()).isBefore(LocalDateTime.now().plusSeconds(1));
    }
}