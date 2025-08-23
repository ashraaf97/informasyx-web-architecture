package com.example.demo.config;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(userRepository, personRepository, passwordEncoder);
    }

    @Test
    void run_SuperAdminNotExists_CreatesSuperAdmin() throws Exception {
        // Arrange
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("superadmin123")).thenReturn("encoded_password");
        when(personRepository.save(any())).thenAnswer(invocation -> {
            Object person = invocation.getArgument(0);
            // Simulate setting ID after save
            return person;
        });
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataInitializer.run();

        // Assert
        verify(userRepository).findByUsername("superadmin");
        verify(personRepository).save(any());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("superadmin123");
    }

    @Test
    void run_SuperAdminAlreadyExists_DoesNotCreateSuperAdmin() throws Exception {
        // Arrange
        User existingSuperAdmin = new User();
        existingSuperAdmin.setUsername("superadmin");
        existingSuperAdmin.setRole(Role.SUPER_ADMIN);
        
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.of(existingSuperAdmin));

        // Act
        dataInitializer.run();

        // Assert
        verify(userRepository).findByUsername("superadmin");
        verify(personRepository, never()).save(any());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void run_ExceptionDuringCreation_HandledGracefully() throws Exception {
        // Arrange
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        lenient().when(passwordEncoder.encode("superadmin123")).thenReturn("encoded_password");
        when(personRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - Should not throw exception
        dataInitializer.run();

        // Verify that the method attempted to create super admin but handled the exception
        verify(userRepository).findByUsername("superadmin");
        verify(personRepository).save(any());
        // userRepository.save should not be called due to exception in personRepository.save
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createSuperAdmin_CreatesCorrectUserAndPerson() throws Exception {
        // Arrange
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("superadmin123")).thenReturn("encoded_password");
        
        // Capture the Person that gets saved
        when(personRepository.save(any())).thenAnswer(invocation -> {
            com.example.demo.domain.Person person = invocation.getArgument(0);
            // Verify person properties
            assert "Super".equals(person.getFirstName());
            assert "Admin".equals(person.getLastName());
            assert "superadmin@example.com".equals(person.getEmail());
            assert "0000000000".equals(person.getPhoneNumber());
            assert "System Administrator".equals(person.getAddress());
            return person;
        });
        
        // Capture the User that gets saved
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Verify user properties
            assert "superadmin".equals(user.getUsername());
            assert "encoded_password".equals(user.getPassword());
            assert user.isActive();
            assert user.isEmailVerified();
            assert Role.SUPER_ADMIN.equals(user.getRole());
            assert user.getPerson() != null;
            return user;
        });

        // Act
        dataInitializer.run();

        // Assert
        verify(userRepository).findByUsername("superadmin");
        verify(personRepository).save(any());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("superadmin123");
    }

    @Test
    void run_WithEmptyArgs_WorksCorrectly() throws Exception {
        // Arrange
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("superadmin123")).thenReturn("encoded_password");
        when(personRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataInitializer.run(new String[]{});

        // Assert
        verify(userRepository).findByUsername("superadmin");
        verify(personRepository).save(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void run_WithNullArgs_WorksCorrectly() throws Exception {
        // Arrange
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("superadmin123")).thenReturn("encoded_password");
        when(personRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataInitializer.run((String[]) null);

        // Assert
        verify(userRepository).findByUsername("superadmin");
        verify(personRepository).save(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void run_PasswordEncoderException_HandledGracefully() throws Exception {
        // Arrange
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("superadmin123")).thenThrow(new RuntimeException("Encoding error"));

        // Act & Assert - Should not throw exception
        dataInitializer.run();

        // Verify that the method attempted to create super admin but handled the exception
        verify(userRepository).findByUsername("superadmin");
        verify(passwordEncoder).encode("superadmin123");
        // Person should be saved before password encoding, but User should not be saved due to encoding exception
        verify(personRepository).save(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_UserRepositorySaveException_HandledGracefully() throws Exception {
        // Arrange
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("superadmin123")).thenReturn("encoded_password");
        when(personRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("User save error"));

        // Act & Assert - Should not throw exception
        dataInitializer.run();

        // Verify that the method attempted to create super admin but handled the exception
        verify(userRepository).findByUsername("superadmin");
        verify(personRepository).save(any());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("superadmin123");
    }

    @Test
    void createSuperAdmin_VerifyDefaultValues() throws Exception {
        // This test verifies that the super admin is created with correct default values
        // Arrange
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("superadmin123")).thenReturn("encoded_password");
        when(personRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            
            // Verify all default values
            assertEquals("superadmin", user.getUsername());
            assertEquals("encoded_password", user.getPassword());
            assertTrue(user.isActive(), "Super admin should be active by default");
            assertTrue(user.isEmailVerified(), "Super admin should be email verified by default");
            assertEquals(Role.SUPER_ADMIN, user.getRole());
            assertNotNull(user.getPerson(), "Super admin should have associated person");
            
            return user;
        });

        // Act
        dataInitializer.run();

        // Assert through the mock verification in when() clause above
        verify(userRepository).save(any(User.class));
    }

    private void assertEquals(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("Expected: " + expected + ", but was: " + actual);
        }
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new AssertionError(message);
        }
    }
}