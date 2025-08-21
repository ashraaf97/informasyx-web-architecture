package com.example.demo.security;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.impl.AuthServiceImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleAuthorizationAspectTest {

    @Mock
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private RequiredRole requiredRole;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RoleAuthorizationAspect roleAuthorizationAspect;

    private User superAdminUser;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        superAdminUser = new User();
        superAdminUser.setUsername("superadmin");
        superAdminUser.setRole(Role.SUPER_ADMIN);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setRole(Role.USER);
    }

    @Test
    void checkRole_ValidSuperAdminToken_Success() throws Throwable {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;

        when(requiredRole.value()).thenReturn(new Role[]{Role.SUPER_ADMIN});
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn("superadmin");
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.of(superAdminUser));
        when(joinPoint.proceed()).thenReturn("success");

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertEquals("success", result);
            verify(joinPoint).proceed();
        }
    }

    @Test
    void checkRole_ValidAdminTokenForAdminRole_Success() throws Throwable {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;

        when(requiredRole.value()).thenReturn(new Role[]{Role.ADMIN, Role.SUPER_ADMIN});
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn("admin");
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(joinPoint.proceed()).thenReturn("success");

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertEquals("success", result);
            verify(joinPoint).proceed();
        }
    }

    @Test
    void checkRole_UserTryingAdminEndpoint_Forbidden() throws Throwable {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;

        when(requiredRole.value()).thenReturn(new Role[]{Role.ADMIN, Role.SUPER_ADMIN});
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn("user");
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertTrue(result instanceof ResponseEntity);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertEquals("Insufficient permissions", response.getBody());
            verify(joinPoint, never()).proceed();
        }
    }

    @Test
    void checkRole_NoAuthorizationHeader_Unauthorized() throws Throwable {
        // Arrange
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(null);

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertTrue(result instanceof ResponseEntity);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Authorization header missing or invalid", response.getBody());
            verify(joinPoint, never()).proceed();
        }
    }

    @Test
    void checkRole_InvalidAuthorizationHeader_Unauthorized() throws Throwable {
        // Arrange
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn("Invalid header");

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertTrue(result instanceof ResponseEntity);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Authorization header missing or invalid", response.getBody());
            verify(joinPoint, never()).proceed();
        }
    }

    @Test
    void checkRole_InvalidToken_Unauthorized() throws Throwable {
        // Arrange
        String token = "invalid_token";
        String authHeader = "Bearer " + token;

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn(null);
        when(authService.isValidToken(token)).thenReturn(false);

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertTrue(result instanceof ResponseEntity);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Invalid or expired token", response.getBody());
            verify(joinPoint, never()).proceed();
        }
    }

    @Test
    void checkRole_UserNotFound_Unauthorized() throws Throwable {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn("nonexistent");
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertTrue(result instanceof ResponseEntity);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("User not found", response.getBody());
            verify(joinPoint, never()).proceed();
        }
    }

    @Test
    void checkRole_NoRequestContext_Unauthorized() throws Throwable {
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertTrue(result instanceof ResponseEntity);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Unauthorized", response.getBody());
            verify(joinPoint, never()).proceed();
        }
    }

    @Test
    void checkRole_MultipleRolesAllowed_UserHasOne_Success() throws Throwable {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;

        when(requiredRole.value()).thenReturn(new Role[]{Role.USER, Role.ADMIN, Role.SUPER_ADMIN});
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenReturn("user");
        when(authService.isValidToken(token)).thenReturn(true);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(joinPoint.proceed()).thenReturn("success");

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act
            Object result = roleAuthorizationAspect.checkRole(joinPoint, requiredRole);

            // Assert
            assertEquals("success", result);
            verify(joinPoint).proceed();
        }
    }

    @Test
    void checkRole_ExceptionDuringTokenValidation_Unauthorized() throws Throwable {
        // Arrange
        String token = "valid_token";
        String authHeader = "Bearer " + token;

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(authService.getUsernameFromToken(token)).thenThrow(new RuntimeException("Token validation error"));

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                roleAuthorizationAspect.checkRole(joinPoint, requiredRole);
            });
            
            verify(joinPoint, never()).proceed();
        }
    }
}