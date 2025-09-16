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

    // This method is for when a specific course title is provided
    public Certificate generateCertificate(String githubId, String courseTitle) {
        try {
            User user = userService.getUserByGithubId(githubId);
            String primarySkill = determinePrimarySkill(user.getBadges());
            return createCertificateRecord(githubId, courseTitle, primarySkill, user.getBadges());
        } catch (Exception e) {
            logger.severe("Failed to generate certificate with course title: " + e.getMessage());
            throw new RuntimeException("Failed to generate certificate: " + e.getMessage(), e);
        }
    }

    // This is for generating based on achievements
    public Certificate generateCertificateForAchievement(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);
            String courseTitle = determineCourseTitle(user.getPoints(), user.getBadges());
            String primarySkill = determinePrimarySkill(user.getBadges());
            return createCertificateRecord(githubId, courseTitle, primarySkill, user.getBadges());
        } catch (Exception e) {
            logger.severe("Failed to generate achievement certificate for user " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to generate achievement certificate: " + e.getMessage(), e);
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
            String qrCodeBase64 = generateQRCode(githubProfileUrl);

            Certificate certificate = new Certificate();
            certificate.setId(certificateId);
            certificate.setUserId(user.getId());
            certificate.setUserName(user.getFullName() != null ? user.getFullName() : user.getUsername());
            certificate.setCourseTitle(courseTitle);
            certificate.setPrimarySkill(primarySkill);
            certificate.setAllSkills(allSkills);
            certificate.setQrCodeData(qrCodeBase64);
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

    public String generateCertificateHtml(String certificateId) {
        Certificate certificate = getCertificateById(certificateId);
        Context context = new Context();
        context.setVariable("name", certificate.getUserName());
        context.setVariable("courseTitle", certificate.getCourseTitle());
        context.setVariable("allSkills", certificate.getAllSkills());
        context.setVariable("completionDate", certificate.getIssuedDate());
        context.setVariable("qrCodeData", certificate.getQrCodeData());
        return templateEngine.process("certificate", context);
    }

    private String generateQRCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            logger.severe("Failed to generate QR code: " + e.getMessage());
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }
}