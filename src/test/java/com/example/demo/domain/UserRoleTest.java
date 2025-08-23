package com.example.demo.domain;

import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRoleTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = new Person();
        testPerson.setFirstName("Test");
        testPerson.setLastName("User");
        testPerson.setEmail("test@example.com");
        testPerson.setPhoneNumber("1234567890");
        testPerson.setAddress("123 Test St");
        testPerson = entityManager.persistAndFlush(testPerson);
    }

    @Test
    void testUserDefaultRole() {
        // Test that a new user has USER role by default
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setPerson(testPerson);

        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void testUserRolePersistence() {
        // Test that role is properly persisted and retrieved
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setPerson(testPerson);
        user.setRole(Role.ADMIN);

        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();

        User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(retrievedUser);
        assertEquals(Role.ADMIN, retrievedUser.getRole());
        assertEquals("testuser", retrievedUser.getUsername());
    }

    @Test
    void testUserRoleEnumMapping() {
        // Test all role enum values can be persisted and retrieved
        User userRole = createUserWithRole("user", Role.USER);
        User adminRole = createUserWithRole("admin", Role.ADMIN);
        User superAdminRole = createUserWithRole("superadmin", Role.SUPER_ADMIN);

        entityManager.persistAndFlush(userRole);
        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(superAdminRole);
        entityManager.clear();

        User retrievedUser = userRepository.findByUsername("user").orElse(null);
        assertNotNull(retrievedUser);
        assertEquals(Role.USER, retrievedUser.getRole());

        User retrievedAdmin = userRepository.findByUsername("admin").orElse(null);
        assertNotNull(retrievedAdmin);
        assertEquals(Role.ADMIN, retrievedAdmin.getRole());

        User retrievedSuperAdmin = userRepository.findByUsername("superadmin").orElse(null);
        assertNotNull(retrievedSuperAdmin);
        assertEquals(Role.SUPER_ADMIN, retrievedSuperAdmin.getRole());
    }

    @Test
    void testUserRoleUpdate() {
        // Test that user role can be updated
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setPerson(testPerson);
        user.setRole(Role.USER);

        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();

        // Update role
        User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(retrievedUser);
        retrievedUser.setRole(Role.ADMIN);
        
        User updatedUser = entityManager.persistAndFlush(retrievedUser);
        entityManager.clear();

        // Verify update
        User finalUser = userRepository.findById(updatedUser.getId()).orElse(null);
        assertNotNull(finalUser);
        assertEquals(Role.ADMIN, finalUser.getRole());
    }

    @Test
    void testUserRoleNotNull() {
        // Test that role field is properly handled when null
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setPerson(testPerson);
        user.setRole(null);

        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();

        User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(retrievedUser);
        assertNull(retrievedUser.getRole());
    }

    @Test
    void testUserEntityAllArgConstructor() {
        // Test User creation with all arguments constructor
        User user = new User(1L, "testuser", "password", testPerson, true, true, Role.ADMIN);
        
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals(testPerson, user.getPerson());
        assertTrue(user.isActive());
        assertTrue(user.isEmailVerified());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void testUserEntityNoArgConstructor() {
        // Test User creation with no arguments constructor
        User user = new User();
        
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getPerson());
        assertTrue(user.isActive()); // Default value
        assertFalse(user.isEmailVerified()); // Default value
        assertEquals(Role.USER, user.getRole()); // Default value
    }

    @Test
    void testUserRoleGetterSetter() {
        // Test role getter and setter methods
        User user = new User();
        
        // Test setter
        user.setRole(Role.SUPER_ADMIN);
        assertEquals(Role.SUPER_ADMIN, user.getRole());
        
        // Test changing role
        user.setRole(Role.USER);
        assertEquals(Role.USER, user.getRole());
        
        // Test setting to null
        user.setRole(null);
        assertNull(user.getRole());
    }

    @Test
    void testUserRoleInDatabaseQuery() {
        // Test querying users by role
        User user1 = createUserWithRole("user1", Role.USER);
        User admin1 = createUserWithRole("admin1", Role.ADMIN);
        User admin2 = createUserWithRole("admin2", Role.ADMIN);
        User superAdmin = createUserWithRole("superadmin", Role.SUPER_ADMIN);

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(admin1);
        entityManager.persistAndFlush(admin2);
        entityManager.persistAndFlush(superAdmin);
        entityManager.clear();

        // Query users by role using custom query
        // Note: This would typically be done with a custom repository method
        // For this test, we'll verify the data is stored correctly
        
        User retrievedUser = userRepository.findByUsername("user1").orElse(null);
        User retrievedAdmin1 = userRepository.findByUsername("admin1").orElse(null);
        User retrievedAdmin2 = userRepository.findByUsername("admin2").orElse(null);
        User retrievedSuperAdmin = userRepository.findByUsername("superadmin").orElse(null);

        assertNotNull(retrievedUser);
        assertNotNull(retrievedAdmin1);
        assertNotNull(retrievedAdmin2);
        assertNotNull(retrievedSuperAdmin);

        assertEquals(Role.USER, retrievedUser.getRole());
        assertEquals(Role.ADMIN, retrievedAdmin1.getRole());
        assertEquals(Role.ADMIN, retrievedAdmin2.getRole());
        assertEquals(Role.SUPER_ADMIN, retrievedSuperAdmin.getRole());
    }

    @Test
    void testUserRoleDatabaseColumnMapping() {
        // Test that role is stored as string in database
        User user = createUserWithRole("testuser", Role.SUPER_ADMIN);
        User savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();

        // Query raw data to verify string storage
        Object[] result = (Object[]) entityManager.getEntityManager()
            .createNativeQuery("SELECT username, role FROM app_user WHERE username = 'testuser'")
            .getSingleResult();
        
        assertEquals("testuser", result[0]);
        assertEquals("SUPER_ADMIN", result[1]); // Should be stored as string
    }

    private User createUserWithRole(String username, Role role) {
        // Create a unique person for each user to avoid person_id constraint violations
        Person person = new Person();
        person.setFirstName("Test");
        person.setLastName(username);
        person.setEmail(username + "@example.com");
        person.setPhoneNumber("1234567890");
        person.setAddress("123 Test St");
        person = entityManager.persistAndFlush(person);

        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setPerson(person);
        user.setRole(role);
        user.setActive(true);
        user.setEmailVerified(true);
        return user;
    }
}