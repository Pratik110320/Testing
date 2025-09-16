package com.team.OpenGalaxy.service;


import com.team.OpenGalaxy.model.Certificate;
import com.team.OpenGalaxy.model.User;
import com.team.OpenGalaxy.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class CertificateService {

    private static final Logger logger = Logger.getLogger(CertificateService.class.getName());

    private final CertificateRepository certificateRepository;
    private final UserService userService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    private static final int[] BADGE_THRESHOLDS = {1, 3, 5, 7, 10, 12, 15, 17, 20, 25, 30, 35, 40, 45, 50};
    private static final String[] BADGE_NAMES = {
            "Rookie 1", "Rookie 2", "Rookie 3", "Coder 1", "Coder 2",
            "Coder 3","Hacker 1", "Hacker 2", "Hacker 3", "Architect 1", "Architect 2",
            "Architect 3", "Legend 1", "Legend 2", "Legend 3"
    };
    public CertificateService(CertificateRepository certificateRepository,
                              UserService userService,
                              TemplateEngine templateEngine) {
        this.certificateRepository = certificateRepository;
        this.userService = userService;
    }

    /**
     * Creates a new certificate for a user if they meet all requirements,
     * or returns their existing active certificate.
     *
     * @param user The authenticated User object.
     * @return An existing or newly created Certificate.
     * @throws RuntimeException if the user has not met the badge requirements.
     */
    public Certificate createCertificate(User user) {
        try {
            // 1. Check if an active certificate already exists for this user
            List<Certificate> existingCerts = certificateRepository.findByUserIdAndIsActive(user.getId(), true);
            if (existingCerts != null && !existingCerts.isEmpty()) {
                logger.info("Returning existing active certificate for user: " + user.getId());
                return existingCerts.get(0); // Return the first active certificate found
            }

            // 2. Check for badge requirements
            List<String> badges = user.getBadges();
            int requiredBadgeCount = BADGE_NAMES.length; // Total number of badges (15)

            if (badges == null || badges.size() < requiredBadgeCount || !badges.contains(SolutionService.FINAL_BADGE_NAME)) {
                String errorMessage = String.format(
                        "User must have all %d badges, including '%s', to generate a certificate. User has %d badges.",
                        requiredBadgeCount,
                        SolutionService.FINAL_BADGE_NAME,
                        (badges == null ? 0 : badges.size())
                );
                logger.warning("Certificate generation denied for user " + user.getId() + ": " + errorMessage);
                throw new RuntimeException(errorMessage);
            }

            // 3. If no existing cert and requirements are met, create a new one
            logger.info("Creating new certificate for user: " + user.getId());
            String courseTitle = determineCourseTitle(user.getPoints(), user.getBadges());
            String primarySkill = determinePrimarySkill(user.getBadges());

            return createCertificateRecord(user, courseTitle, primarySkill, user.getBadges());

        } catch (Exception e) {
            logger.severe("Failed to create or retrieve certificate for user " + user.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to create certificate: " + e.getMessage(), e);
        }
    }

    // --- METHODS ADDED BACK IN ---

    public List<Certificate> getCertificatesByUser(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);
            return certificateRepository.findByUserId(user.getId());
        } catch (Exception e) {
            logger.severe("Failed to retrieve certificates for user " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve certificates: " + e.getMessage(), e);
        }
    }

    public boolean verifyCertificate(String certificateId) {
        try {
            return certificateRepository.existsById(certificateId);
        } catch (Exception e) {
            logger.severe("Failed to verify certificate " + certificateId + ": " + e.getMessage());
            return false;
        }
    }

    // --- HELPER AND OTHER METHODS ---


    /**
     * Helper method to create and save the Certificate document.
     * Now accepts a User object.
     */
    private Certificate createCertificateRecord(User user, String courseTitle, String latestBadge, List<String> allbadges) {
        try {
            String certificateId = UUID.randomUUID().toString();
            String githubProfileUrl = "https://github.com/" + user.getUsername();

            Certificate certificate = new Certificate();
            certificate.setId(certificateId);
            certificate.setUserId(user.getId());
            certificate.setUserName(user.getFullName() != null ? user.getFullName() : user.getUsername());
            certificate.setCourseTitle(courseTitle);
            certificate.setlatestBadge(latestBadge);
            certificate.setallBadges(allbadges);
            certificate.setVerificationUrl(baseUrl + "/api/certificates/view/" + certificateId);
            certificate.setGithubProfileUrl(githubProfileUrl);
            certificate.setGeneratedAt(new Date());
            certificate.setIssuedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

            return certificateRepository.save(certificate);

        } catch (Exception e) {
            logger.severe("Failed to create certificate record for user " + user.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to create certificate record: " + e.getMessage(), e);
        }
    }

    private String determinePrimarySkill(List<String> badges) {
        if (badges == null || badges.isEmpty()) return "Getting Started";
        return badges.get(badges.size() - 1);
    }

    private String determineCourseTitle(int points, List<String> badges) {
        if (badges == null || badges.isEmpty()) return "Welcome to OpenGalaxy";
        String latestBadge = badges.get(badges.size() - 1);
        return switch (latestBadge) {
            case "Code Spark" -> "Problem Solving Fundamentals";
            case "Stellar Coder", "Cosmic Contributor" -> "Intermediate Problem Solving";
            default -> "Advanced Problem Solving";
        };
    }

    public Certificate getCertificateById(String certificateId) {
        return certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found with ID: " + certificateId));
    }
}