package com.example.demo.controller;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.AdminCreateUserRequest;
import com.example.demo.domain.dto.ChangeRoleRequest;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.impl.AuthServiceImpl;
import com.example.demo.service.EventPublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "spring.kafka.bootstrap-servers=",
    "app.kafka.enabled=false"
})
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureWebMvc
@DirtiesContext
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventPublisherService eventPublisherService;

    private MockMvc mockMvc;
    private String superAdminToken;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test users
        createTestUsers();
        
        // Get authentication tokens
        superAdminToken = "Bearer " + generateToken("testsuperadmin");
        adminToken = "Bearer " + generateToken("testadmin");
        userToken = "Bearer " + generateToken("testuser");
    }

    private void createTestUsers() {
        // Create Super Admin
        Person superAdminPerson = new Person();
        superAdminPerson.setFirstName("Test");
        superAdminPerson.setLastName("SuperAdmin");
        superAdminPerson.setEmail("testsuperadmin@example.com");
        superAdminPerson = personRepository.save(superAdminPerson);

        User superAdmin = new User();
        superAdmin.setUsername("testsuperadmin");
        superAdmin.setPassword(passwordEncoder.encode("password123"));
        superAdmin.setPerson(superAdminPerson);
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setActive(true);
        superAdmin.setEmailVerified(true);
        userRepository.save(superAdmin);

        // Create Admin
        Person adminPerson = new Person();
        adminPerson.setFirstName("Test");
        adminPerson.setLastName("Admin");
        adminPerson.setEmail("testadmin@example.com");
        adminPerson = personRepository.save(adminPerson);

        User admin = new User();
        admin.setUsername("testadmin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setPerson(adminPerson);
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setEmailVerified(true);
        userRepository.save(admin);

        // Create Regular User
        Person userPerson = new Person();
        userPerson.setFirstName("Test");
        userPerson.setLastName("User");
        userPerson.setEmail("testuser@example.com");
        userPerson = personRepository.save(userPerson);

        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setPerson(userPerson);
        user.setRole(Role.USER);
        user.setActive(true);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    private String generateToken(String username) {
        // Generate a token in the same format as AuthServiceImpl
        String token = "TOKEN_" + username + "_" + System.currentTimeMillis();
        
        // Add the token to the auth service's tokenStore using reflection
        try {
            java.lang.reflect.Field tokenStoreField = authService.getClass().getDeclaredField("tokenStore");
            tokenStoreField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> tokenStore = (java.util.Map<String, String>) tokenStoreField.get(authService);
            tokenStore.put(token, username);
        } catch (Exception e) {
            // If reflection fails, log it but continue
            System.err.println("Failed to add token to tokenStore: " + e.getMessage());
        }
        
        return token;
    }

    @Test
    void createUser_SuperAdminCreatesUser_Success() throws Exception {
        // Store token for mocking
        authService.getUsernameFromToken(superAdminToken.substring(7));
        
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser1");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("newuser1@example.com");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("newuser1"));
    }

    @Test
    void createUser_AdminCreatesUser_Success() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser2");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("newuser2@example.com");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createUser_UserTriesToCreateUser_Forbidden() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser3");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("newuser3@example.com");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_NoAuthorizationHeader_Unauthorized() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser4");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("newuser4@example.com");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAdmin_SuperAdminCreatesAdmin_Success() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newadmin1");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("Admin");
        request.setEmail("newadmin1@example.com");
        // Role will be set to ADMIN automatically

        mockMvc.perform(post("/api/admin/users/admin")
                .header("Authorization", superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("newadmin1"));
    }

    @Test
    void createAdmin_AdminTriesToCreateAdmin_Forbidden() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newadmin2");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("Admin");
        request.setEmail("newadmin2@example.com");

        mockMvc.perform(post("/api/admin/users/admin")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeUserRole_SuperAdminChangesRole_Success() throws Exception {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("testuser");
        request.setRole(Role.ADMIN);

        mockMvc.perform(put("/api/admin/users/role")
                .header("Authorization", superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void changeUserRole_AdminTriesToChangeRole_Forbidden() throws Exception {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("testuser");
        request.setRole(Role.ADMIN);

        mockMvc.perform(put("/api/admin/users/role")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeUserRole_UserTriesToChangeRole_Forbidden() throws Exception {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("testuser");
        request.setRole(Role.ADMIN);

        mockMvc.perform(put("/api/admin/users/role")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_InvalidRequest_BadRequest() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        // Missing required fields

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeUserRole_InvalidRequest_BadRequest() throws Exception {
        ChangeRoleRequest request = new ChangeRoleRequest();
        // Missing required fields

        mockMvc.perform(put("/api/admin/users/role")
                .header("Authorization", superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}