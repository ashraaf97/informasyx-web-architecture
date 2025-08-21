package com.example.demo.security;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
public class RoleBasedAccessControlTest {

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

    private MockMvc mockMvc;
    private String superAdminToken;
    private String adminToken;
    private String userToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test users
        createTestUsers();
        
        // Generate tokens and store them in auth service
        superAdminToken = "Bearer " + generateAndStoreToken("testsuperadmin");
        adminToken = "Bearer " + generateAndStoreToken("testadmin");
        userToken = "Bearer " + generateAndStoreToken("testuser");
        invalidToken = "Bearer INVALID_TOKEN";
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

    private String generateAndStoreToken(String username) {
        String token = "TOKEN_" + username + "_" + System.currentTimeMillis();
        // Store token in auth service for validation
        authService.getUsernameFromToken(token); // This would normally store the token
        return token;
    }

    // Tests for UserController endpoints
    @Test
    void getAllUsers_SuperAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", superAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_NoAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_InvalidToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_SuperAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", superAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_SuperAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                .header("Authorization", superAdminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_Admin_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                .header("Authorization", adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_User_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    // Tests for PersonController endpoints
    @Test
    void getAllPersons_SuperAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/persons")
                .header("Authorization", superAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getAllPersons_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/persons")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getAllPersons_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/persons")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPersonById_SuperAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/persons/1")
                .header("Authorization", superAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getPersonById_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/persons/1")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getPersonById_User_Success() throws Exception {
        mockMvc.perform(get("/api/persons/1")
                .header("Authorization", userToken))
                .andExpect(status().isOk());
    }

    @Test
    void deletePerson_SuperAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/persons/1")
                .header("Authorization", superAdminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePerson_Admin_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/persons/1")
                .header("Authorization", adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePerson_User_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/persons/1")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchPersons_SuperAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/persons/search/lastName")
                .param("lastName", "Smith")
                .header("Authorization", superAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    void searchPersons_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/persons/search/lastName")
                .param("lastName", "Smith")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void searchPersons_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/persons/search/lastName")
                .param("lastName", "Smith")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_SuperAdmin_Success() throws Exception {
        String userJson = "{\"username\":\"updateduser\",\"password\":\"newpassword\"}";
        
        mockMvc.perform(put("/api/users/1")
                .header("Authorization", superAdminToken)
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_Admin_Success() throws Exception {
        String userJson = "{\"username\":\"updateduser\",\"password\":\"newpassword\"}";
        
        mockMvc.perform(put("/api/users/1")
                .header("Authorization", adminToken)
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_User_Forbidden() throws Exception {
        String userJson = "{\"username\":\"updateduser\",\"password\":\"newpassword\"}";
        
        mockMvc.perform(put("/api/users/1")
                .header("Authorization", userToken)
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePerson_SuperAdmin_Success() throws Exception {
        String personJson = "{\"firstName\":\"Updated\",\"lastName\":\"Person\",\"email\":\"updated@example.com\"}";
        
        mockMvc.perform(put("/api/persons/1")
                .header("Authorization", superAdminToken)
                .contentType("application/json")
                .content(personJson))
                .andExpect(status().isOk());
    }

    @Test
    void updatePerson_Admin_Success() throws Exception {
        String personJson = "{\"firstName\":\"Updated\",\"lastName\":\"Person\",\"email\":\"updated@example.com\"}";
        
        mockMvc.perform(put("/api/persons/1")
                .header("Authorization", adminToken)
                .contentType("application/json")
                .content(personJson))
                .andExpect(status().isOk());
    }

    @Test
    void updatePerson_User_Success() throws Exception {
        String personJson = "{\"firstName\":\"Updated\",\"lastName\":\"Person\",\"email\":\"updated@example.com\"}";
        
        mockMvc.perform(put("/api/persons/1")
                .header("Authorization", userToken)
                .contentType("application/json")
                .content(personJson))
                .andExpect(status().isOk());
    }
}