package com.pratik.OpenGalaxy.controller;


import com.pratik.OpenGalaxy.model.DTOs.UserUpdateDTO;
import com.pratik.OpenGalaxy.model.User;
import com.pratik.OpenGalaxy.repository.ProblemRepository;
import com.pratik.OpenGalaxy.service.LeaderboardService;
import com.pratik.OpenGalaxy.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private final UserService userService;
    private final ProblemRepository problemRepository;
    private final LeaderboardService leaderboardService;

    public UserController(UserService userService, ProblemRepository problemRepository,LeaderboardService leaderboardService) {
        this.userService = userService;
        this.problemRepository = problemRepository;
        this.leaderboardService=leaderboardService;
    }

    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            String githubId = oauthUser.getAttribute("id").toString();
            return userService.getUserByGithubId(githubId);
        } catch (Exception e) {
            logger.severe("Failed to retrieve user: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve user: " + e.getMessage(), e);
        }
    }

    @PostMapping ("/me")
    public User updateUserProfile(@AuthenticationPrincipal DefaultOAuth2User oauthUser,
                                  @RequestBody UserUpdateDTO updateDTO) {
        try {
            String githubId = oauthUser.getAttribute("id").toString();
            return userService.updateUserProfile(githubId, updateDTO.getFullName(), updateDTO.getUsername(), updateDTO.getEmail());
        } catch (Exception e) {
            logger.severe("Failed to update user profile: " + e.getMessage());
            throw new RuntimeException("Failed to update user profile: " + e.getMessage(), e);
        }
    }

}