package com.example.demo.config;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.context.annotation.Profile("!test & !h2")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create initial super admin user if it doesn't exist
        if (userRepository.findByUsername("superadmin").isEmpty()) {
            createSuperAdmin();
        }
    }

    private void createSuperAdmin() {
        try {
            // Create Person for super admin
            Person person = new Person();
            person.setFirstName("Super");
            person.setLastName("Admin");
            person.setEmail("superadmin@example.com");
            person.setPhoneNumber("0000000000");
            person.setAddress("System Administrator");
            
            Person savedPerson = personRepository.save(person);

            // Create Super Admin User
            User superAdmin = new User();
            superAdmin.setUsername("superadmin");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123")); // Change this in production
            superAdmin.setPerson(savedPerson);
            superAdmin.setActive(true);
            superAdmin.setEmailVerified(true);
            superAdmin.setRole(Role.SUPER_ADMIN);

            userRepository.save(superAdmin);
            
            log.info("Super Admin user created successfully with username: superadmin");
            log.warn("IMPORTANT: Change the default Super Admin password in production!");
            
        } catch (Exception e) {
            log.error("Failed to create Super Admin user", e);
        }
    }
}