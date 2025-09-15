package com.team.OpenGalaxy.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.team.OpenGalaxy.model.Certificate;
import com.team.OpenGalaxy.model.User;
import com.team.OpenGalaxy.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
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
    // REMOVED: private final SolutionService solutionService; // This was causing circular dependency
    private final TemplateEngine templateEngine;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public CertificateService(CertificateRepository certificateRepository,
                              UserService userService,
                              TemplateEngine templateEngine) {
        this.certificateRepository = certificateRepository;
        this.userService = userService;
        this.templateEngine = templateEngine;
    }

    public Certificate generateCertificate(String githubId, String courseTitle) {
        try {
            User user = userService.getUserByGithubId(githubId);

            // Generate unique certificate ID
            String certificateId = UUID.randomUUID().toString();

            // Get user's badges (skills)
            List<String> badges = user.getBadges();
            String primarySkill = badges != null && !badges.isEmpty() ?
                    badges.get(badges.size() - 1) : "Code Spark"; // Latest badge or default

            String githubProfileUrl = "https://github.com/" + user.getUsername();
            String certificateViewUrl = baseUrl + "/api/certificates/view/" + certificateId;

// Generate QR code for GitHub profile
            String qrCodeBase64 = generateQRCode(githubProfileUrl);

            Certificate certificate = new Certificate();
            certificate.setId(certificateId);
            certificate.setUserId(user.getId());
            certificate.setUserName(user.getFullName() != null ? user.getFullName() : user.getUsername());
            certificate.setCourseTitle(courseTitle);
            certificate.setPrimarySkill(primarySkill);
            certificate.setAllSkills(badges);
            certificate.setQrCodeData(qrCodeBase64);
            certificate.setVerificationUrl(certificateViewUrl); // for viewing the certificate
            certificate.setGithubProfileUrl(githubProfileUrl);   // new field in your model
            certificate.setGeneratedAt(new Date());
            certificate.setIssuedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

            Certificate savedCertificate = certificateRepository.save(certificate);

            logger.info("Certificate generated successfully for user: " + githubId + " with ID: " + certificateId);

            return savedCertificate;
        } catch (Exception e) {
            logger.severe("Failed to generate certificate for user " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to generate certificate: " + e.getMessage(), e);
        }
    }

    public Certificate getCertificateById(String certificateId) {
        return certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found with ID: " + certificateId));
    }

    public List<Certificate> getCertificatesByUser(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);
            return certificateRepository.findByUserId(user.getId());
        } catch (Exception e) {
            logger.severe("Failed to retrieve certificates for user " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve certificates: " + e.getMessage(), e);
        }
    }

    public ModelAndView getCertificateView(String certificateId) {
        try {
            Certificate certificate = getCertificateById(certificateId);

            ModelAndView modelAndView = new ModelAndView("certificate");
            modelAndView.addObject("name", certificate.getUserName());
            modelAndView.addObject("courseTitle", certificate.getCourseTitle());
            modelAndView.addObject("primarySkill", certificate.getPrimarySkill());
            modelAndView.addObject("allSkills", certificate.getAllSkills());
            modelAndView.addObject("completionDate", certificate.getIssuedDate());
            modelAndView.addObject("qrCodeData", certificate.getQrCodeData());
            modelAndView.addObject("verifyUrl", certificate.getVerificationUrl());
            modelAndView.addObject("issuer", "OpenGalaxy");

            return modelAndView;
        } catch (Exception e) {
            logger.severe("Failed to create certificate view for ID " + certificateId + ": " + e.getMessage());
            throw new RuntimeException("Failed to create certificate view: " + e.getMessage(), e);
        }
    }

    public String generateCertificateHtml(String certificateId) {
        try {
            Certificate certificate = getCertificateById(certificateId);

            Context context = new Context();
            context.setVariable("name", certificate.getUserName());
            context.setVariable("courseTitle", certificate.getCourseTitle());
            context.setVariable("primarySkill", certificate.getPrimarySkill());
            context.setVariable("allSkills", certificate.getAllSkills());
            context.setVariable("completionDate", certificate.getIssuedDate());
            context.setVariable("qrCodeData", certificate.getQrCodeData());
            context.setVariable("verifyUrl", certificate.getVerificationUrl());
            context.setVariable("issuer", "OpenGalaxy");

            return templateEngine.process("certificate", context);
        } catch (Exception e) {
            logger.severe("Failed to generate certificate HTML for ID " + certificateId + ": " + e.getMessage());
            throw new RuntimeException("Failed to generate certificate HTML: " + e.getMessage(), e);
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

    // MOVED AND SIMPLIFIED: This method now uses User data directly instead of SolutionService
    public Certificate generateCertificateForAchievement(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);

            // Determine course title based on user's badges/points
            String courseTitle = determineCourseTitle(user.getPoints(), user.getBadges());

            return generateCertificate(githubId, courseTitle);
        } catch (Exception e) {
            logger.severe("Failed to generate achievement certificate for user " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to generate achievement certificate: " + e.getMessage(), e);
        }
    }

    private String determineCourseTitle(int points, List<String> badges) {
        if (badges == null || badges.isEmpty()) {
            return "Problem Solving Fundamentals";
        }

        String latestBadge = badges.get(badges.size() - 1);
        return switch (latestBadge) {
            case "Code Spark" -> "Problem Solving Fundamentals";
            case "Stellar Coder", "Cosmic Contributor" -> "Intermediate Problem Solving";
            case "Galaxy Explorer", "Nebula Navigator", "Star Solver" -> "Advanced Algorithmic Thinking";
            case "Orbit Master", "Astro Achiever", "Supernova Star" -> "Expert Problem Solving";
            case "Interstellar Innovator", "Cosmic Trailblazer" -> "Master Algorithm Designer";
            default -> "Elite Problem Solving Mastery";
        };
    }

    private String generateQRCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", outputStream);

            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            logger.severe("Failed to generate QR code: " + e.getMessage());
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }




}