package com.pratik.OpenGalaxy.controller;



import com.pratik.OpenGalaxy.model.Certificate;
import com.pratik.OpenGalaxy.service.CertificateService;
import com.pratik.OpenGalaxy.service.ChromePdfService;

import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class CertificateController {

    private static final Logger logger = Logger.getLogger(CertificateController.class.getName());
    private final CertificateService certificateService;
    private final ChromePdfService chromePdfService;

    public CertificateController(CertificateService certificateService, ChromePdfService chromePdfService) {
        this.certificateService = certificateService;
        this.chromePdfService = chromePdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateCertificate(
            @AuthenticationPrincipal DefaultOAuth2User oauthUser,
            @RequestParam(required = false) String courseTitle) {
        try {
            String githubId = oauthUser.getAttribute("id").toString();

            Certificate certificate;
            if (courseTitle != null && !courseTitle.trim().isEmpty()) {
                certificate = certificateService.generateCertificate(githubId, courseTitle);
            } else {
                certificate = certificateService.generateCertificateForAchievement(githubId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificateId", certificate.getId());
            response.put("certificate", certificate);
            response.put("certificateViewUrl", certificate.getVerificationUrl()); // shows template
            response.put("githubProfileUrl", certificate.getGithubProfileUrl());  // goes to GitHub
            response.put("message", "Certificate generated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("Error generating certificate: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to generate certificate: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/my-certificates")
    public ResponseEntity<Map<String, Object>> getMyCertificates(
            @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            String githubId = oauthUser.getAttribute("id").toString();
            List<Certificate> certificates = certificateService.getCertificatesByUser(githubId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificates", certificates);
            response.put("count", certificates.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error retrieving certificates: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve certificates: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{certificateId}")
    public ResponseEntity<Map<String, Object>> getCertificateDetails(@PathVariable String certificateId) {
        try {
            Certificate certificate = certificateService.getCertificateById(certificateId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificate", certificate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error retrieving certificate: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Certificate not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/verify/{certificateId}")
    public ResponseEntity<Map<String, Object>> verifyCertificate(@PathVariable String certificateId) {
        try {
            boolean isValid = certificateService.verifyCertificate(certificateId);
            Certificate certificate = null;

            if (isValid) {
                certificate = certificateService.getCertificateById(certificateId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("certificateId", certificateId);
            if (certificate != null) {
                response.put("certificate", certificate);
                response.put("message", "Certificate is valid and authentic");
            } else {
                response.put("message", "Certificate not found or invalid");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error verifying certificate: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("message", "Error verifying certificate");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/view/{certificateId}")
    public ModelAndView viewCertificate(@PathVariable String certificateId) {
        try {
            return certificateService.getCertificateView(certificateId);
        } catch (Exception e) {
            logger.severe("Error viewing certificate: " + e.getMessage());
            ModelAndView errorView = new ModelAndView("error");
            errorView.addObject("message", "Certificate not found");
            return errorView;
        }
    }

    @GetMapping("/html/{certificateId}")
    public ResponseEntity<String> getCertificateHtml(@PathVariable String certificateId) {
        try {
            String html = certificateService.generateCertificateHtml(certificateId);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html")
                    .body(html);
        } catch (Exception e) {
            logger.severe("Error generating certificate HTML: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("<html><body><h1>Certificate not found</h1></body></html>");
        }
    }



    @GetMapping("/download/{certificateId}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String certificateId) {
        try {
            // 1️⃣ Generate HTML for certificate
            String rawHtml = certificateService.generateCertificateHtml(certificateId);

            // 2️⃣ Convert HTML to PDF (in memory, no saving to C:\temp)
            byte[] pdfBytes = chromePdfService.renderHtmlToPdfInMemory(rawHtml);

            // 3️⃣ Send PDF directly to browser
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }


}


