package com.team.OpenGalaxy.service;

import com.team.OpenGalaxy.model.DTOs.UserResponseDTO;
import com.team.OpenGalaxy.model.User;
import com.team.OpenGalaxy.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    public Optional<User> findByGithubId(String githubId) {
        try {
            return userRepository.findByGithubId(githubId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by GitHub ID: " + e.getMessage(), e);
        }
    }

    public User updateUserProfile(String githubId, String fullName, String username, String email) {
        try {
            Optional<User> optionalUser = userRepository.findByGithubId(githubId);
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("User not found for GitHub ID: " + githubId);
            }

            User user = optionalUser.get();
            if (fullName != null) user.setFullName(fullName);
            if (username != null) user.setUsername(username);
            if (email != null) user.setEmail(email);
            user.setUpdatedAt(new Date());

            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user profile: " + e.getMessage(), e);
        }
    }

    public User getUserByGithubId(String githubId) {
        return findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("User not found for GitHub ID: " + githubId));
    }

    public User getUserById(String submittedBy) {
        return userRepository.findById(submittedBy)
                .orElseThrow(() -> new RuntimeException("User not found : " + submittedBy));
    }

    // inside UserService class

    public UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setEmail(user.getEmail());
        dto.setGithubId(user.getGithubId());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setPoints(user.getPoints());
        dto.setBadges(user.getBadges());
        return dto;
    }

    // Optional: batch fetch helper (useful later for performance)
    public List<User> findAllByIds(List<String> ids) {
        try {
            return userRepository.findAllById(ids);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch users by ids: " + e.getMessage(), e);
        }
    }

}