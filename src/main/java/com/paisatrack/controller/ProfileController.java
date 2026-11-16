package com.paisatrack.controller;

import com.paisatrack.entity.User;
import com.paisatrack.security.CustomUserDetails;
import com.paisatrack.service.UserService;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the user's Profile page with account details and logout action.
 */
@Controller
public class ProfileController {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter INDIAN_DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(
        @AuthenticationPrincipal CustomUserDetails principal,
        Model model
    ) {
        User user = userService.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException("Authenticated user record missing."));

        String memberSince = user.getCreatedAt()
            .atZone(IST_ZONE)
            .toLocalDate()
            .format(INDIAN_DATE_FORMAT);

        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("memberSince", memberSince);
        model.addAttribute("activeTab", "profile");
        return "profile";
    }
}
