package com.pratik.OpenGalaxy.config;


import com.pratik.OpenGalaxy.model.User;
import com.pratik.OpenGalaxy.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final UserService userService;

    public Oauth2Login(UserService userService) {
        this.userService = userService;
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

            // Fallback for fullName if null
            if (fullName == null || fullName.trim().isEmpty()) {
                fullName = username;
            }

            // Handle null email
            if (email == null || email.trim().isEmpty()) {
                email = username + "@github.com"; // Placeholder email
            }

            Optional<User> existingUser = userService.findByGithubId(githubId);

            if (existingUser.isEmpty()) {
                User newUser = new User();
                newUser.setGithubId(githubId);
                newUser.setUsername(username);
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setProfilePicture(avatarUrl);
                newUser.setPassword(null);
                newUser.setCreatedAt(new Date());
                newUser.setUpdatedAt(new Date());
                userService.save(newUser);
            } else {
                User user = existingUser.get();
                user.setEmail(email);
                user.setFullName(fullName);
                user.setProfilePicture(avatarUrl);
                user.setUpdatedAt(new Date());
                userService.save(user);
            }

            response.sendRedirect("/welcome");
        } catch (Exception e) {
            logger.severe("Authentication failed: " + e.getMessage());
            throw new ServletException("Authentication failed: " + e.getMessage(), e);
        }
    }
}