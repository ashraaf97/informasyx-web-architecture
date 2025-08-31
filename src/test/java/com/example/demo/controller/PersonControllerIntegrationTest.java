package com.example.demo.controller;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.PersonDTO;
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
class PersonControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Person testPerson;
    private User testUser;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean up any existing test data
        userRepository.deleteAll();
        personRepository.deleteAll();

        // Create test person and admin user
        testPerson = new Person();
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("1234567890");
        testPerson.setAddress("123 Main St");

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

        // Create regular user for testing authorization
        Person regularPerson = new Person();
        regularPerson.setFirstName("Jane");
        regularPerson.setLastName("Smith");
        regularPerson.setEmail("jane.smith@example.com");
        regularPerson.setPhoneNumber("0987654321");
        regularPerson.setAddress("456 Oak Ave");

        User regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setPassword(passwordEncoder.encode("user123"));
        regularUser.setPerson(regularPerson);
        regularUser.setActive(true);
        regularUser.setEmailVerified(true);
        regularUser.setRole(Role.USER);

        regularPerson.setUser(regularUser);
        personRepository.save(regularPerson);
        userRepository.save(regularUser);

        // Get tokens for testing authorization
        adminToken = getAuthToken("admin", "admin123");
        userToken = getAuthToken("user", "user123");
    }

    private String getAuthToken(String username, String password) {
        com.example.demo.domain.dto.LoginRequest loginRequest = new com.example.demo.domain.dto.LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        
        var response = authService.login(loginRequest);
        return response.getToken();
    }

    @Test
    void getAllPersons_WithAdminRole_Success() throws Exception {
        mockMvc.perform(get("/api/persons")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].email").exists());
    }

    @Test
    void getAllPersons_WithUserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/persons")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPersons_WithoutAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPersonById_WithValidId_Success() throws Exception {
        mockMvc.perform(get("/api/persons/{id}", testPerson.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPerson.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getPersonById_WithInvalidId_NotFound() throws Exception {
        mockMvc.perform(get("/api/persons/{id}", 999L)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPersonById_WithUserRole_Success() throws Exception {
        mockMvc.perform(get("/api/persons/{id}", testPerson.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void createPerson_WithValidData_Success() throws Exception {
        PersonDTO newPerson = new PersonDTO();
        newPerson.setFirstName("Alice");
        newPerson.setLastName("Johnson");
        newPerson.setEmail("alice.johnson@example.com");
        newPerson.setPhoneNumber("5555551234");
        newPerson.setAddress("789 Pine St");

        mockMvc.perform(post("/api/persons")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPerson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.email").value("alice.johnson@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createPerson_WithUserRole_Forbidden() throws Exception {
        PersonDTO newPerson = new PersonDTO();
        newPerson.setFirstName("Alice");
        newPerson.setLastName("Johnson");
        newPerson.setEmail("alice.johnson@example.com");
        newPerson.setPhoneNumber("5555551234");
        newPerson.setAddress("789 Pine St");

        mockMvc.perform(post("/api/persons")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPerson)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPerson_WithInvalidData_BadRequest() throws Exception {
        PersonDTO invalidPerson = new PersonDTO();
        // Missing required fields

        mockMvc.perform(post("/api/persons")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPerson)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePerson_WithValidData_Success() throws Exception {
        PersonDTO updatedPerson = new PersonDTO();
        updatedPerson.setFirstName("Johnny");
        updatedPerson.setLastName("Doe");
        updatedPerson.setEmail("johnny.doe@example.com");
        updatedPerson.setPhoneNumber("1234567890");
        updatedPerson.setAddress("123 Main St");

        mockMvc.perform(put("/api/persons/{id}", testPerson.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPerson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.email").value("johnny.doe@example.com"));
    }

    @Test
    void updatePerson_WithInvalidId_NotFound() throws Exception {
        PersonDTO updatedPerson = new PersonDTO();
        updatedPerson.setFirstName("Johnny");
        updatedPerson.setLastName("Doe");
        updatedPerson.setEmail("johnny.doe@example.com");
        updatedPerson.setPhoneNumber("1234567890");
        updatedPerson.setAddress("123 Main St");

        mockMvc.perform(put("/api/persons/{id}", 999L)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPerson)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePerson_WithUserRole_Success() throws Exception {
        PersonDTO updatedPerson = new PersonDTO();
        updatedPerson.setFirstName("Johnny");
        updatedPerson.setLastName("Doe");
        updatedPerson.setEmail("johnny.doe@example.com");
        updatedPerson.setPhoneNumber("1234567890");
        updatedPerson.setAddress("123 Main St");

        mockMvc.perform(put("/api/persons/{id}", testPerson.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPerson)))
                .andExpect(status().isOk());
    }

    @Test
    void deletePerson_WithSuperAdminRole_Success() throws Exception {
        // Create super admin for delete permission
        Person superAdminPerson = new Person();
        superAdminPerson.setFirstName("Super");
        superAdminPerson.setLastName("Admin");
        superAdminPerson.setEmail("super.admin@example.com");
        superAdminPerson.setPhoneNumber("1111111111");
        superAdminPerson.setAddress("Admin St");

        User superAdmin = new User();
        superAdmin.setUsername("superadmin");
        superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
        superAdmin.setPerson(superAdminPerson);
        superAdmin.setActive(true);
        superAdmin.setEmailVerified(true);
        superAdmin.setRole(Role.SUPER_ADMIN);

        superAdminPerson.setUser(superAdmin);
        personRepository.save(superAdminPerson);
        userRepository.save(superAdmin);

        String superAdminToken = getAuthToken("superadmin", "superadmin123");

        mockMvc.perform(delete("/api/persons/{id}", testPerson.getId())
                .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePerson_WithAdminRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/persons/{id}", testPerson.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePerson_WithInvalidId_NotFound() throws Exception {
        // Create super admin for delete permission
        Person superAdminPerson = new Person();
        superAdminPerson.setFirstName("Super");
        superAdminPerson.setLastName("Admin");
        superAdminPerson.setEmail("super.admin@example.com");
        superAdminPerson.setPhoneNumber("1111111111");
        superAdminPerson.setAddress("Admin St");

        User superAdmin = new User();
        superAdmin.setUsername("superadmin");
        superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
        superAdmin.setPerson(superAdminPerson);
        superAdmin.setActive(true);
        superAdmin.setEmailVerified(true);
        superAdmin.setRole(Role.SUPER_ADMIN);

        superAdminPerson.setUser(superAdmin);
        personRepository.save(superAdminPerson);
        userRepository.save(superAdmin);

        String superAdminToken = getAuthToken("superadmin", "superadmin123");

        mockMvc.perform(delete("/api/persons/{id}", 999L)
                .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByLastName_WithResults_Success() throws Exception {
        mockMvc.perform(get("/api/persons/search/lastName")
                .param("lastName", "Doe")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    void findByLastName_NoResults_EmptyList() throws Exception {
        mockMvc.perform(get("/api/persons/search/lastName")
                .param("lastName", "NonExistent")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findByFullName_WithResults_Success() throws Exception {
        mockMvc.perform(get("/api/persons/search/name")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    void findByEmailContaining_WithResults_Success() throws Exception {
        mockMvc.perform(get("/api/persons/search/email")
                .param("email", "john.doe")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    void findByEmailContaining_NoResults_EmptyList() throws Exception {
        mockMvc.perform(get("/api/persons/search/email")
                .param("email", "nonexistent")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchEndpoints_WithUserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/persons/search/lastName")
                .param("lastName", "Doe")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/persons/search/name")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/persons/search/email")
                .param("email", "john.doe")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}