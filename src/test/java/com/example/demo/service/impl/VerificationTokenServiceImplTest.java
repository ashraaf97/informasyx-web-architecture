package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.VerificationToken;
import com.example.demo.domain.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceImplTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private VerificationTokenServiceImpl verificationTokenService;

    private User testUser;
    private VerificationToken testToken;

    @BeforeEach
    void setUp() {
        Person testPerson = new Person();
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

        testToken = new VerificationToken();
        testToken.setId(1L);
        testToken.setToken("test-token-123");
        testToken.setUser(testUser);
        testToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        testToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        testToken.setUsed(false);
        testToken.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void generateEmailVerificationToken_ShouldCreateAndSaveToken() {
        // Arrange
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String token = verificationTokenService.generateEmailVerificationToken(testUser);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        verify(verificationTokenRepository).deleteByUserAndTokenType(testUser, VerificationToken.TokenType.EMAIL_VERIFICATION);
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void generatePasswordResetToken_ShouldCreateAndSaveToken() {
        // Arrange
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String token = verificationTokenService.generatePasswordResetToken(testUser);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        verify(verificationTokenRepository).deleteByUserAndTokenType(testUser, VerificationToken.TokenType.PASSWORD_RESET);
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void verifyEmailToken_ValidUnusedToken_ShouldReturnTrueAndUpdateUser() {
        // Arrange
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        boolean result = verificationTokenService.verifyEmailToken("test-token-123");

        // Assert
        assertTrue(result);
        assertTrue(testUser.isEmailVerified());
        assertTrue(testToken.isUsed());
        verify(verificationTokenRepository).save(testToken);
    }

    @Test
    void verifyEmailToken_TokenNotFound_ShouldReturnFalse() {
        // Arrange
        when(verificationTokenRepository.findByToken("non-existent-token"))
                .thenReturn(Optional.empty());

        // Act
        boolean result = verificationTokenService.verifyEmailToken("non-existent-token");

        // Assert
        assertFalse(result);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void verifyEmailToken_ExpiredToken_ShouldReturnFalse() {
        // Arrange
        testToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        boolean result = verificationTokenService.verifyEmailToken("test-token-123");

        // Assert
        assertFalse(result);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void verifyEmailToken_AlreadyUsedToken_ShouldReturnFalse() {
        // Arrange
        testToken.setUsed(true);
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        boolean result = verificationTokenService.verifyEmailToken("test-token-123");

        // Assert
        assertFalse(result);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void verifyEmailToken_WrongTokenType_ShouldReturnFalse() {
        // Arrange
        testToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        boolean result = verificationTokenService.verifyEmailToken("test-token-123");

        // Assert
        assertFalse(result);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void verifyPasswordResetToken_ValidUnusedToken_ShouldReturnTrue() {
        // Arrange
        testToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        boolean result = verificationTokenService.verifyPasswordResetToken("test-token-123");

        // Assert
        assertTrue(result);
        verify(verificationTokenRepository, never()).save(any()); // Should not save on verification
    }

    @Test
    void verifyPasswordResetToken_TokenNotFound_ShouldReturnFalse() {
        // Arrange
        when(verificationTokenRepository.findByToken("non-existent-token"))
                .thenReturn(Optional.empty());

        // Act
        boolean result = verificationTokenService.verifyPasswordResetToken("non-existent-token");

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPasswordResetToken_ExpiredToken_ShouldReturnFalse() {
        // Arrange
        testToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        testToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        boolean result = verificationTokenService.verifyPasswordResetToken("test-token-123");

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPasswordResetToken_AlreadyUsedToken_ShouldReturnFalse() {
        // Arrange
        testToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        testToken.setUsed(true);
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        boolean result = verificationTokenService.verifyPasswordResetToken("test-token-123");

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPasswordResetToken_WrongTokenType_ShouldReturnFalse() {
        // Arrange
        testToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        boolean result = verificationTokenService.verifyPasswordResetToken("test-token-123");

        // Assert
        assertFalse(result);
    }

    @Test
    void findByToken_ExistingToken_ShouldReturnToken() {
        // Arrange
        when(verificationTokenRepository.findByToken("test-token-123"))
                .thenReturn(Optional.of(testToken));

        // Act
        VerificationToken result = verificationTokenService.findByToken("test-token-123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testToken);
    }

    @Test
    void findByToken_NonExistentToken_ShouldReturnNull() {
        // Arrange
        when(verificationTokenRepository.findByToken("non-existent-token"))
                .thenReturn(Optional.empty());

        // Act
        VerificationToken result = verificationTokenService.findByToken("non-existent-token");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void markTokenAsUsed_ShouldUpdateTokenAndSave() {
        // Act
        verificationTokenService.markTokenAsUsed(testToken);

        // Assert
        assertTrue(testToken.isUsed());
        verify(verificationTokenRepository).save(testToken);
    }

    @Test
    void cleanupExpiredTokens_ShouldDeleteExpiredUsedTokens() {
        // Act
        verificationTokenService.cleanupExpiredTokens();

        // Assert
        verify(verificationTokenRepository).deleteByExpiryDateBeforeAndUsedTrue(any(LocalDateTime.class));
    }

    @Test
    void generateEmailVerificationToken_ShouldDeleteExistingTokensFirst() {
        // Arrange
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        verificationTokenService.generateEmailVerificationToken(testUser);

        // Assert
        verify(verificationTokenRepository).deleteByUserAndTokenType(
                eq(testUser), eq(VerificationToken.TokenType.EMAIL_VERIFICATION));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void generatePasswordResetToken_ShouldDeleteExistingTokensFirst() {
        // Arrange
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        verificationTokenService.generatePasswordResetToken(testUser);

        // Assert
        verify(verificationTokenRepository).deleteByUserAndTokenType(
                eq(testUser), eq(VerificationToken.TokenType.PASSWORD_RESET));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void generateEmailVerificationToken_ShouldSetCorrectExpiryTime() {
        // Arrange
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> {
                    VerificationToken token = invocation.getArgument(0);
                    // Verify the token has correct properties
                    assertThat(token.getTokenType()).isEqualTo(VerificationToken.TokenType.EMAIL_VERIFICATION);
                    assertThat(token.getUser()).isEqualTo(testUser);
                    assertThat(token.isUsed()).isFalse();
                    assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now().plusHours(23));
                    assertThat(token.getExpiryDate()).isBefore(LocalDateTime.now().plusHours(25));
                    return token;
                });

        // Act
        verificationTokenService.generateEmailVerificationToken(testUser);

        // Assert
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void generatePasswordResetToken_ShouldSetCorrectExpiryTime() {
        // Arrange
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> {
                    VerificationToken token = invocation.getArgument(0);
                    // Verify the token has correct properties
                    assertThat(token.getTokenType()).isEqualTo(VerificationToken.TokenType.PASSWORD_RESET);
                    assertThat(token.getUser()).isEqualTo(testUser);
                    assertThat(token.isUsed()).isFalse();
                    assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now().plusMinutes(59));
                    assertThat(token.getExpiryDate()).isBefore(LocalDateTime.now().plusMinutes(61));
                    return token;
                });

        // Act
        verificationTokenService.generatePasswordResetToken(testUser);

        // Assert
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void generateTokens_ShouldGenerateUniqueTokens() {
        // Arrange
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String token1 = verificationTokenService.generateEmailVerificationToken(testUser);
        String token2 = verificationTokenService.generateEmailVerificationToken(testUser);

        // Assert
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1).isNotEmpty();
        assertThat(token2).isNotEmpty();
    }

    @Test
    void verifyEmailToken_NullToken_ShouldReturnFalse() {
        // Act
        boolean result = verificationTokenService.verifyEmailToken(null);

        // Assert
        assertFalse(result);
        verify(verificationTokenRepository, never()).findByToken(any());
    }

    @Test
    void verifyPasswordResetToken_EmptyToken_ShouldReturnFalse() {
        // Act
        boolean result = verificationTokenService.verifyPasswordResetToken("");

        // Assert
        assertFalse(result);
        verify(verificationTokenRepository, never()).findByToken(any());
    }

    @Test
    void markTokenAsUsed_NullToken_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            verificationTokenService.markTokenAsUsed(null);
        });
        
        verify(verificationTokenRepository, never()).save(any());
    }
}