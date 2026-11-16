package com.paisatrack.service;

import com.paisatrack.dto.RegisterRequest;
import com.paisatrack.entity.User;
import java.util.Optional;

/**
 * Service interface for user registration and lookup operations.
 */
public interface UserService {

    User registerUser(RegisterRequest request);

    Optional<User> findById(Long userId);
}
