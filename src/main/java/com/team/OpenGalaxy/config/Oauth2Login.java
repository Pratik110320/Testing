package com.team.OpenGalaxy.config;

import com.team.OpenGalaxy.model.User;
import com.team.OpenGalaxy.service.EmailService;
import com.team.OpenGalaxy.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class Oauth2Login implements AuthenticationSuccessHandler {

    private static final Logger logger = Logger.getLogger(Oauth2Login.class.getName());
    @Value("${frontend.url}")
    private String frontendUrl;
    private final UserService userService;
    private final EmailService emailService;

    public Oauth2Login(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            Map<String, Object> attributes = ((org.springframework.security.oauth2.core.user.DefaultOAuth2User) authentication.getPrincipal()).getAttributes();

            String githubId = attributes.get("id").toString();
            String username = (String) attributes.get("login");
            String email = (String) attributes.get("email"); // Might be null if not public
            String fullName = (String) attributes.get("name");
            String avatarUrl = (String) attributes.get("avatar_url");

            // Fallbacks
            if (fullName == null || fullName.trim().isEmpty()) fullName = username;
            if (email == null || email.trim().isEmpty()) email = username + "@github.com"; // Placeholder

            Optional<User> existingUser = userService.findByGithubId(githubId);

            User user;
            if (existingUser.isEmpty()) {
                // New user creation
                user = new User();
                user.setGithubId(githubId);
                user.setUsername(username);
                user.setEmail(email);
                user.setFullName(fullName);
                user.setProfilePicture(avatarUrl);
                user.setPassword(null);
                user.setCreatedAt(new Date());
                user.setUpdatedAt(new Date());
                userService.save(user);
            } else {
                // Existing user — only update fullName/email if empty, always update avatar
                user = existingUser.get();

                if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                    user.setFullName(fullName);
                }
                if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                    user.setEmail(email);
                }

                // Always update profile picture from GitHub
                user.setProfilePicture(avatarUrl);

                user.setUpdatedAt(new Date());
                userService.save(user);
            }

            // Send login success email
            emailService.sendLoginSuccessAlert(user);

            response.sendRedirect(frontendUrl);
        } catch (Exception e) {
            logger.severe("Authentication failed: " + e.getMessage());
            throw new ServletException("Authentication failed: " + e.getMessage(), e);
        }
    }
}
