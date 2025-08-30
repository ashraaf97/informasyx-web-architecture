package com.example.demo.dto;

import com.example.demo.domain.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // LoginRequest Tests
    @Test
    void loginRequest_ValidData_ShouldPassValidation() {
        // Arrange
        LoginRequest request = new LoginRequest("validuser", "validpassword");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void loginRequest_BlankUsername_ShouldFailValidation() {
        // Arrange
        LoginRequest request = new LoginRequest("", "validpassword");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
    }

    @Test
    void loginRequest_NullUsername_ShouldFailValidation() {
        // Arrange
        LoginRequest request = new LoginRequest(null, "validpassword");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
    }

    @Test
    void loginRequest_BlankPassword_ShouldFailValidation() {
        // Arrange
        LoginRequest request = new LoginRequest("validuser", "");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    void loginRequest_NullPassword_ShouldFailValidation() {
        // Arrange
        LoginRequest request = new LoginRequest("validuser", null);

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    void loginRequest_BothFieldsBlank_ShouldFailValidation() {
        // Arrange
        LoginRequest request = new LoginRequest("", "");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(2);
    }

    // SignUpRequest Tests
    @Test
    void signUpRequest_ValidData_ShouldPassValidation() {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setUsername("validuser");
        request.setEmail("valid@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("1234567890");
        request.setAddress("123 Main St");
        request.setPassword("ValidPass123!");
        request.setConfirmPassword("ValidPass123!");

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void signUpRequest_MinimumValidData_ShouldPassValidation() {
        // Arrange - Only required fields
        SignUpRequest request = new SignUpRequest();
        request.setUsername("abc"); // minimum 3 chars
        request.setEmail("a@b.co");
        request.setFirstName("A");
        request.setLastName("B");
        request.setPassword("ValidPass123!");
        request.setConfirmPassword("ValidPass123!");

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void signUpRequest_BlankUsername_ShouldFailValidation() {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setUsername("");

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(2); // NotBlank and Size violations
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void signUpRequest_UsernameTooShort_ShouldFailValidation() {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setUsername("ab"); // Less than 3 characters

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
        assertThat(violations.iterator().next().getMessage()).contains("between 3 and 50 characters");
    }

    @Test
    void signUpRequest_UsernameTooLong_ShouldFailValidation() {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setUsername("a".repeat(51)); // More than 50 characters

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
        assertThat(violations.iterator().next().getMessage()).contains("between 3 and 50 characters");
    }

    @Test
    void signUpRequest_InvalidEmail_ShouldFailValidation() {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setEmail("invalid-email");

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).contains("valid email address");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "plaintext", "123456", "PASSWORD", "Password", "Password123", "password123!"})
    void signUpRequest_InvalidPasswords_ShouldFailValidation(String invalidPassword) {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setPassword(invalidPassword);

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ValidPass123!", "MySecure1@", "Password123$", "Strong1Pass!"})
    void signUpRequest_ValidPasswords_ShouldPassValidation(String validPassword) {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setPassword(validPassword);
        request.setConfirmPassword(validPassword);

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void signUpRequest_FirstNameTooLong_ShouldFailValidation() {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setFirstName("a".repeat(51)); // More than 50 characters

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    @Test
    void signUpRequest_PhoneNumberTooLong_ShouldFailValidation() {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setPhoneNumber("1".repeat(21)); // More than 20 characters

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("phoneNumber");
    }

    @Test
    void signUpRequest_AddressTooLong_ShouldFailValidation() {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setAddress("a".repeat(256)); // More than 255 characters

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("address");
    }

    // ChangePasswordRequest Tests
    @Test
    void changePasswordRequest_ValidData_ShouldPassValidation() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentpass");
        request.setNewPassword("ValidPass123!");
        request.setConfirmPassword("ValidPass123!");

        // Act
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void changePasswordRequest_BlankCurrentPassword_ShouldFailValidation() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("");
        request.setNewPassword("ValidPass123!");
        request.setConfirmPassword("ValidPass123!");

        // Act
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("currentPassword");
    }

    @Test
    void changePasswordRequest_InvalidNewPassword_ShouldFailValidation() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentpass");
        request.setNewPassword("weak");
        request.setConfirmPassword("weak");

        // Act
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("newPassword");
    }

    // PersonDTO Tests
    @Test
    void personDTO_ValidData_ShouldPassValidation() {
        // Arrange
        PersonDTO personDTO = new PersonDTO();
        personDTO.setFirstName("John");
        personDTO.setLastName("Doe");
        personDTO.setEmail("john.doe@example.com");
        personDTO.setPhoneNumber("1234567890");
        personDTO.setAddress("123 Main St");

        // Act
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(personDTO);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void personDTO_BlankFirstName_ShouldFailValidation() {
        // Arrange
        PersonDTO personDTO = new PersonDTO();
        personDTO.setFirstName("");
        personDTO.setLastName("Doe");
        personDTO.setEmail("john.doe@example.com");

        // Act
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(personDTO);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    @Test
    void personDTO_InvalidEmail_ShouldFailValidation() {
        // Arrange
        PersonDTO personDTO = new PersonDTO();
        personDTO.setFirstName("John");
        personDTO.setLastName("Doe");
        personDTO.setEmail("invalid-email");

        // Act
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(personDTO);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void personDTO_NullEmail_ShouldPassValidation() {
        // Arrange - Email is not required, only validated if present
        PersonDTO personDTO = new PersonDTO();
        personDTO.setFirstName("John");
        personDTO.setLastName("Doe");
        personDTO.setEmail(null);

        // Act
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(personDTO);

        // Assert
        assertThat(violations).isEmpty();
    }

    // ForgotPasswordRequest Tests
    @Test
    void forgotPasswordRequest_ValidEmail_ShouldPassValidation() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("valid@example.com");

        // Act
        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    // ResetPasswordRequest Tests
    @Test
    void resetPasswordRequest_ValidData_ShouldPassValidation() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("ValidPass123!");
        request.setConfirmPassword("ValidPass123!");

        // Act
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    // AuthResponse Tests (typically no validation constraints on response DTOs)
    @Test
    void authResponse_AnyData_ShouldPassValidation() {
        // Arrange
        AuthResponse response = AuthResponse.success("user", "token", null);

        // Act
        Set<ConstraintViolation<AuthResponse>> violations = validator.validate(response);

        // Assert
        assertThat(violations).isEmpty();
    }

    // Edge Cases and Boundary Tests
    @Test
    void signUpRequest_ExactlyMaxLengths_ShouldPassValidation() {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setUsername("a".repeat(50)); // Exactly 50 characters
        request.setEmail("valid@example.com");
        request.setFirstName("a".repeat(50)); // Exactly 50 characters
        request.setLastName("a".repeat(50)); // Exactly 50 characters
        request.setPhoneNumber("1".repeat(20)); // Exactly 20 characters
        request.setAddress("a".repeat(255)); // Exactly 255 characters
        request.setPassword("ValidPass123!");
        request.setConfirmPassword("ValidPass123!");

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void signUpRequest_ExactlyMinLength_ShouldPassValidation() {
        // Arrange
        SignUpRequest request = createValidSignUpRequest();
        request.setUsername("abc"); // Exactly 3 characters

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void signUpRequest_WhitespaceOnlyFields_ShouldFailValidation() {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setUsername("   "); // Whitespace only
        request.setEmail("valid@example.com");
        request.setFirstName("   ");
        request.setLastName("   ");
        request.setPassword("ValidPass123!");
        request.setConfirmPassword("ValidPass123!");

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSizeGreaterThan(0); // Should fail NotBlank validations
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user.name@domain.co", "valid+email@test.org", "123@test.com"})
    void personDTO_ValidEmails_ShouldPassValidation(String validEmail) {
        // Arrange
        PersonDTO personDTO = new PersonDTO();
        personDTO.setFirstName("John");
        personDTO.setLastName("Doe");
        personDTO.setEmail(validEmail);

        // Act
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(personDTO);

        // Assert
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "@domain.com", "user@", "user..name@domain.com", "user name@domain.com"})
    void personDTO_InvalidEmails_ShouldFailValidation(String invalidEmail) {
        // Arrange
        PersonDTO personDTO = new PersonDTO();
        personDTO.setFirstName("John");
        personDTO.setLastName("Doe");
        personDTO.setEmail(invalidEmail);

        // Act
        Set<ConstraintViolation<PersonDTO>> violations = validator.validate(personDTO);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    // Helper method to create a valid SignUpRequest
    private SignUpRequest createValidSignUpRequest() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("validuser");
        request.setEmail("valid@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("1234567890");
        request.setAddress("123 Main St");
        request.setPassword("ValidPass123!");
        request.setConfirmPassword("ValidPass123!");
        return request;
    }
}