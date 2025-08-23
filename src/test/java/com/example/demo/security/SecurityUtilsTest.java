package com.example.demo.security;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SecurityUtils securityUtils;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Role.USER);
    }

    @Test
    void getCurrentUser_ValidToken_ReturnsUser() {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;
        String username = "testuser";

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn(username);
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Optional<User> result = securityUtils.getCurrentUser();

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testUser, result.get());
            assertEquals("testuser", result.get().getUsername());
            assertEquals(Role.USER, result.get().getRole());
        }
    }

    @Test
    void getCurrentUser_NoRequestContext_ReturnsEmpty() {
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            // Act
            Optional<User> result = securityUtils.getCurrentUser();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getCurrentUser_NoAuthorizationHeader_ReturnsEmpty() {
        // Arrange
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(null);

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Optional<User> result = securityUtils.getCurrentUser();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getCurrentUser_InvalidAuthorizationHeader_ReturnsEmpty() {
        // Arrange
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn("Invalid header format");

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Optional<User> result = securityUtils.getCurrentUser();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getCurrentUser_InvalidToken_ReturnsEmpty() {
        // Arrange
        String token = "invalid_token";
        String authHeader = "Bearer " + token;

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn(null);
        lenient().when(authService.isValidToken(token)).thenReturn(false);

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Optional<User> result = securityUtils.getCurrentUser();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getCurrentUser_UserNotFound_ReturnsEmpty() {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;
        String username = "nonexistent";

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn(username);
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Optional<User> result = securityUtils.getCurrentUser();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getTokenFromRequest_ValidAuthorizationHeader_ReturnsToken() {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            String result = securityUtils.getTokenFromRequest();

            // Assert
            assertEquals(token, result);
        }
    }

    @Test
    void getTokenFromRequest_NoRequestContext_ReturnsNull() {
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            // Act
            String result = securityUtils.getTokenFromRequest();

            // Assert
            assertNull(result);
        }
    }

    @Test
    void getTokenFromRequest_NoAuthorizationHeader_ReturnsNull() {
        // Arrange
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(null);

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            String result = securityUtils.getTokenFromRequest();

            // Assert
            assertNull(result);
        }
    }

    @Test
    void getTokenFromRequest_InvalidAuthorizationHeader_ReturnsNull() {
        // Arrange
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn("Invalid header format");

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            String result = securityUtils.getTokenFromRequest();

            // Assert
            assertNull(result);
        }
    }

    @Test
    void getTokenFromRequest_EmptyBearerToken_ReturnsEmptyString() {
        // Arrange
        String authHeader = "Bearer ";

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            String result = securityUtils.getTokenFromRequest();

            // Assert
            assertEquals("", result);
        }
    }

    @Test
    void getCurrentUser_AdminUser_ReturnsAdminUser() {
        // Arrange
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        String token = "admin_token";
        String authHeader = "Bearer " + token;
        String username = "admin";

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn(username);
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Optional<User> result = securityUtils.getCurrentUser();

            // Assert
            assertTrue(result.isPresent());
            assertEquals(adminUser, result.get());
            assertEquals("admin", result.get().getUsername());
            assertEquals(Role.ADMIN, result.get().getRole());
        }
    }

    @Test
    void getCurrentUser_SuperAdminUser_ReturnsSuperAdminUser() {
        // Arrange
        User superAdminUser = new User();
        superAdminUser.setId(3L);
        superAdminUser.setUsername("superadmin");
        superAdminUser.setRole(Role.SUPER_ADMIN);

        String token = "superadmin_token";
        String authHeader = "Bearer " + token;
        String username = "superadmin";

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn(username);
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(superAdminUser));

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Optional<User> result = securityUtils.getCurrentUser();

            // Assert
            assertTrue(result.isPresent());
            assertEquals(superAdminUser, result.get());
            assertEquals("superadmin", result.get().getUsername());
            assertEquals(Role.SUPER_ADMIN, result.get().getRole());
        }
    }
}