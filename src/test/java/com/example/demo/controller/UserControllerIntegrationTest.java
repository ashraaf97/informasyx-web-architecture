package com.example.demo.controller;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.domain.dto.UserCreateDTO;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.impl.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-integration.properties")
@Transactional
class UserControllerIntegrationTest {

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

    private MockMvc mockMvc;
    private User testUser;
    private Person testPerson;
    private String adminToken;
    private String superAdminToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean up any existing test data
        userRepository.deleteAll();
        personRepository.deleteAll();

        // Create test person and admin user
        testPerson = new Person();
        testPerson.setFirstName("Admin");
        testPerson.setLastName("User");
        testPerson.setEmail("admin@example.com");
        testPerson.setPhoneNumber("1234567890");
        testPerson.setAddress("Admin St");

        testUser = new User();
        testUser.setUsername("admin");
        testUser.setPassword(passwordEncoder.encode("admin123"));
        testUser.setPerson(testPerson);
        testUser.setActive(true);
        testUser.setEmailVerified(true);
        testUser.setRole(Role.ADMIN);

        testPerson.setUser(testUser);
        personRepository.save(testPerson);
        userRepository.save(testUser);

        // Create super admin user
        Person superAdminPerson = new Person();
        superAdminPerson.setFirstName("Super");
        superAdminPerson.setLastName("Admin");
        superAdminPerson.setEmail("superadmin@example.com");
        superAdminPerson.setPhoneNumber("0987654321");
        superAdminPerson.setAddress("Super Admin St");

        User superAdminUser = new User();
        superAdminUser.setUsername("superadmin");
        superAdminUser.setPassword(passwordEncoder.encode("superadmin123"));
        superAdminUser.setPerson(superAdminPerson);
        superAdminUser.setActive(true);
        superAdminUser.setEmailVerified(true);
        superAdminUser.setRole(Role.SUPER_ADMIN);

        superAdminPerson.setUser(superAdminUser);
        personRepository.save(superAdminPerson);
        userRepository.save(superAdminUser);

        // Get tokens for testing authorization
        adminToken = getAuthToken("admin", "admin123");
        superAdminToken = getAuthToken("superadmin", "superadmin123");
    }

    private String getAuthToken(String username, String password) {
        com.example.demo.domain.dto.LoginRequest loginRequest = new com.example.demo.domain.dto.LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        
        var response = authService.login(loginRequest);
        return response.getToken();
    }

    @Test
    void getAllUsers_WithAdminRole_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].person").exists());
    }

    @Test
    void getAllUsers_WithoutAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_WithValidId_Success() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.person.email").value("admin@example.com"));
    }

    @Test
    void getUserById_WithInvalidId_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername_WithValidUsername_Success() throws Exception {
        mockMvc.perform(get("/api/users/username/{username}", "admin")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.person.email").value("admin@example.com"));
    }

    @Test
    void getUserByUsername_WithInvalidUsername_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/username/{username}", "nonexistent")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_WithSuperAdminRole_Success() throws Exception {
        PersonDTO newPersonDTO = new PersonDTO();
        newPersonDTO.setFirstName("New");
        newPersonDTO.setLastName("User");
        newPersonDTO.setEmail("newuser@example.com");
        newPersonDTO.setPhoneNumber("5555555555");
        newPersonDTO.setAddress("New User St");

        UserCreateDTO newUserDTO = new UserCreateDTO();
        newUserDTO.setUsername("newuser");
        newUserDTO.setPassword("newuser123");
        newUserDTO.setPerson(newPersonDTO);
        newUserDTO.setActive(true);
        newUserDTO.setRoles("USER");

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.person.email").value("newuser@example.com"));
    }

    @Test
    void createUser_WithAdminRole_Forbidden() throws Exception {
        PersonDTO newPersonDTO = new PersonDTO();
        newPersonDTO.setFirstName("New");
        newPersonDTO.setLastName("User");
        newPersonDTO.setEmail("newuser@example.com");
        newPersonDTO.setPhoneNumber("5555555555");
        newPersonDTO.setAddress("New User St");

        UserCreateDTO newUserDTO = new UserCreateDTO();
        newUserDTO.setUsername("newuser");
        newUserDTO.setPassword("newuser123");
        newUserDTO.setPerson(newPersonDTO);
        newUserDTO.setActive(true);
        newUserDTO.setRoles("USER");

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_WithDuplicateUsername_Conflict() throws Exception {
        PersonDTO newPersonDTO = new PersonDTO();
        newPersonDTO.setFirstName("Duplicate");
        newPersonDTO.setLastName("User");
        newPersonDTO.setEmail("duplicate@example.com");
        newPersonDTO.setPhoneNumber("5555555555");
        newPersonDTO.setAddress("Duplicate St");

        UserCreateDTO duplicateUserDTO = new UserCreateDTO();
        duplicateUserDTO.setUsername("admin"); // Already exists
        duplicateUserDTO.setPassword("duplicate123");
        duplicateUserDTO.setPerson(newPersonDTO);
        duplicateUserDTO.setActive(true);
        duplicateUserDTO.setRoles("USER");

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUserDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_WithValidData_Success() throws Exception {
        PersonDTO updatedPersonDTO = new PersonDTO();
        updatedPersonDTO.setFirstName("Updated");
        updatedPersonDTO.setLastName("Admin");
        updatedPersonDTO.setEmail("updated.admin@example.com");
        updatedPersonDTO.setPhoneNumber("1111111111");
        updatedPersonDTO.setAddress("Updated Admin St");

        UserCreateDTO updatedUserDTO = new UserCreateDTO();
        updatedUserDTO.setUsername("admin");
        updatedUserDTO.setPassword("admin123");
        updatedUserDTO.setPerson(updatedPersonDTO);
        updatedUserDTO.setActive(true);
        updatedUserDTO.setRoles("ADMIN");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person.firstName").value("Updated"))
                .andExpect(jsonPath("$.person.email").value("updated.admin@example.com"));
    }

    @Test
    void updateUser_WithInvalidId_NotFound() throws Exception {
        PersonDTO updatedPersonDTO = new PersonDTO();
        updatedPersonDTO.setFirstName("Updated");
        updatedPersonDTO.setLastName("User");
        updatedPersonDTO.setEmail("updated@example.com");
        updatedPersonDTO.setPhoneNumber("1111111111");
        updatedPersonDTO.setAddress("Updated St");

        UserCreateDTO updatedUserDTO = new UserCreateDTO();
        updatedUserDTO.setUsername("updated");
        updatedUserDTO.setPassword("updated123");
        updatedUserDTO.setPerson(updatedPersonDTO);
        updatedUserDTO.setActive(true);
        updatedUserDTO.setRoles("USER");

        mockMvc.perform(put("/api/users/{id}", 999L)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_WithSuperAdminRole_Success() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WithAdminRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_WithInvalidId_NotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999L)
                .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void existsByUsername_ExistingUsername_ReturnsTrue() throws Exception {
        mockMvc.perform(get("/api/users/exists/{username}", "admin")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void existsByUsername_NonExistingUsername_ReturnsFalse() throws Exception {
        mockMvc.perform(get("/api/users/exists/{username}", "nonexistent")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    void createUser_WithInvalidData_BadRequest() throws Exception {
        UserCreateDTO invalidUserDTO = new UserCreateDTO();
        // Missing required fields

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_WithInvalidData_BadRequest() throws Exception {
        UserCreateDTO invalidUserDTO = new UserCreateDTO();
        // Missing required fields

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allEndpoints_WithoutAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/username/{username}", "admin"))
                .andExpect(status().isUnauthorized());

        // Create valid request body for POST/PUT tests to ensure authentication is checked before validation
        UserCreateDTO validUserDTO = new UserCreateDTO();
        validUserDTO.setUsername("testuser123");
        validUserDTO.setPassword("testpassword123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDTO)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserDTO)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/exists/{username}", "admin"))
                .andExpect(status().isUnauthorized());
    }
}