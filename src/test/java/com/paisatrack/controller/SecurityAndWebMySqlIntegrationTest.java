
package com.paisatrack.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

import com.paisatrack.entity.Category;
import com.paisatrack.entity.CategoryType;
import com.paisatrack.repository.CategoryRepository;
import com.paisatrack.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end MockMvc tests against real MySQL verifying authentication,
 * CSRF enforcement, route protection, and session logout.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndWebMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final List<String> createdEmails = new ArrayList<>();

    @AfterEach
    void cleanUpUsers() {
        for (String email : createdEmails) {
            userRepository.findByEmailIgnoreCase(email)
                    .ifPresent(userRepository::delete);
        }
        createdEmails.clear();
    }

    @Test
    void unauthenticatedRequestsAreProtected() throws Exception {

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/api/analytics/data"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginCsrfTransactionCreateAndLogoutWorkEndToEnd()
            throws Exception {

        String email = "web_" + UUID.randomUUID() + "@paisatrack.in";
        createdEmails.add(email);

        // 1. Register user via POST /register with CSRF
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Sneha Kulkarni")
                        .param("email", email)
                        .param("password", "MyPass123")
                        .param("confirmPassword", "MyPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // 2. Authenticate via Spring Security BCrypt form login
        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("email", email)
                        .password("MyPass123"))
                .andExpect(authenticated().withUsername(email))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        // 3. Access protected routes while authenticated
        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/analytics").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/data").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk());

        // 4. Verify CSRF protection rejects a missing CSRF token
        Category expenseCategory = categoryRepository
                .findByCategoryTypeOrderByCategoryIdAsc(CategoryType.EXPENSE)
                .get(0);

        mockMvc.perform(post("/transactions/save")
                        .session(session)
                        .param("transactionType", "EXPENSE")
                        .param("amount", "150.00")
                        .param("categoryId",
                                String.valueOf(expenseCategory.getCategoryId()))
                        .param("transactionDate", "2026-11-16"))
                .andExpect(status().isForbidden());

        // 5. Verify valid CSRF saves transaction in MySQL
        mockMvc.perform(post("/transactions/save")
                        .session(session)
                        .with(csrf())
                        .param("transactionType", "EXPENSE")
                        .param("amount", "150.00")
                        .param("categoryId",
                                String.valueOf(expenseCategory.getCategoryId()))
                        .param("transactionDate", "2026-11-16")
                        .param("description", "Bus ticket"))
                .andExpect(status().is3xxRedirection());

        // 6. Logout invalidates session
        mockMvc.perform(post("/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection());
    }
}
