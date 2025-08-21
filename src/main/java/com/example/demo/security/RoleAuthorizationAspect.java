package com.example.demo.security;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleAuthorizationAspect {

    private final AuthServiceImpl authService;
    private final UserRepository userRepository;

    @Around("@annotation(requiredRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiredRole requiredRole) throws Throwable {
        ServletRequestAttributes requestAttributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (requestAttributes == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        HttpServletRequest request = requestAttributes.getRequest();
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Authorization header missing or invalid");
        }

        String token = authHeader.substring(7);
        String username = authService.getUsernameFromToken(token);

        if (username == null || !authService.isValidToken(token)) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOpt.get();
        Role userRole = user.getRole();

        boolean hasRequiredRole = Arrays.stream(requiredRole.value())
            .anyMatch(role -> role == userRole);

        if (!hasRequiredRole) {
            log.warn("User {} with role {} attempted to access endpoint requiring roles: {}", 
                username, userRole, Arrays.toString(requiredRole.value()));
            return ResponseEntity.status(403).body("Insufficient permissions");
        }

        return joinPoint.proceed();
    }
}