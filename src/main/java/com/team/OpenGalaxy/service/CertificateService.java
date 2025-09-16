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

    public CertificateService(CertificateRepository certificateRepository,
                              UserService userService,
                              TemplateEngine templateEngine) {
        this.certificateRepository = certificateRepository;
        this.userService = userService;
    }

    public Certificate createCertificate(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);


            List<String> badges = user.getBadges();
            if (badges == null || !badges.contains(SolutionService.FINAL_BADGE_NAME)) {
                throw new RuntimeException("User has not earned the final badge ('" + SolutionService.FINAL_BADGE_NAME + "') yet.");
            }


            String courseTitle = determineCourseTitle(user.getPoints(), user.getBadges());
            String primarySkill = determinePrimarySkill(user.getBadges());


            return createCertificateRecord(githubId, courseTitle, primarySkill, user.getBadges());

        } catch (Exception e) {
            logger.severe("Failed to create certificate: " + e.getMessage());
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


    private Certificate createCertificateRecord(String githubId, String courseTitle, String primarySkill, List<String> allSkills) {
        try {
            User user = userService.getUserByGithubId(githubId);
            String certificateId = UUID.randomUUID().toString();
            String githubProfileUrl = "https://github.com/" + user.getUsername();


            Certificate certificate = new Certificate();
            certificate.setId(certificateId);
            certificate.setUserId(user.getId());
            certificate.setUserName(user.getFullName() != null ? user.getFullName() : user.getUsername());
            certificate.setCourseTitle(courseTitle);
            certificate.setPrimarySkill(primarySkill);
            certificate.setAllSkills(allSkills);
            certificate.setVerificationUrl(baseUrl + "/api/certificates/view/" + certificateId);
            certificate.setGithubProfileUrl(githubProfileUrl);
            certificate.setGeneratedAt(new Date());
            certificate.setIssuedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

            return certificateRepository.save(certificate);

        } catch (Exception e) {
            logger.severe("Failed to create certificate record for user " + githubId + ": " + e.getMessage());
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