package com.paisatrack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.paisatrack.dto.RegisterRequest;
import com.paisatrack.entity.User;
import com.paisatrack.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Real MySQL integration tests for user registration, duplicate email validation,
 * and BCrypt password hashing.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceMySqlIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    void registerUserPersistsInMySqlAndHashesPasswordWithBCrypt() {
        String email = "test_" + UUID.randomUUID() + "@paisatrack.in";
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Aarav Sharma");
        req.setEmail(email);
        req.setPassword("SecurePass123");
        req.setConfirmPassword("SecurePass123");

        User saved = userService.registerUser(req);
        entityManager.flush();
        entityManager.clear();

        User reloaded = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertNotNull(reloaded.getUserId());
        assertEquals("Aarav Sharma", reloaded.getFullName());
        assertEquals(email.toLowerCase(), reloaded.getEmail());
        assertNotEquals("SecurePass123", reloaded.getPasswordHash());
        assertTrue(reloaded.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches("SecurePass123", reloaded.getPasswordHash()));
    }

    @Test
    void registerUserRejectsDuplicateEmailIgnoringCase() {
        String email = "dup_" + UUID.randomUUID() + "@paisatrack.in";

        RegisterRequest first = new RegisterRequest();
        first.setFullName("Priya Nair");
        first.setEmail(email);
        first.setPassword("Password123");
        first.setConfirmPassword("Password123");
        userService.registerUser(first);
        entityManager.flush();

        RegisterRequest duplicate = new RegisterRequest();
        duplicate.setFullName("Priya Duplicate");
        duplicate.setEmail(email.toUpperCase());
        duplicate.setPassword("Password456");
        duplicate.setConfirmPassword("Password456");

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(duplicate));
    }

    @Test
    void registerUserRejectsMismatchedPasswords() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Rohan Verma");
        req.setEmail("mismatch_" + UUID.randomUUID() + "@paisatrack.in");
        req.setPassword("Password123");
        req.setConfirmPassword("Different999");

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(req));
    }
}
