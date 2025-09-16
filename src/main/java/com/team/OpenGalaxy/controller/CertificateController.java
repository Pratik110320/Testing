package com.team.OpenGalaxy.controller;

import com.team.OpenGalaxy.model.Certificate;
import com.team.OpenGalaxy.model.User; // Import User model
import com.team.OpenGalaxy.service.CertificateService;
import com.team.OpenGalaxy.service.UserService; // Import UserService
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private static final Logger logger = Logger.getLogger(CertificateController.class.getName());
    private final CertificateService certificateService;
    private final UserService userService; // Add UserService

    // Update constructor to inject UserService
    public CertificateController(CertificateService certificateService, UserService userService) {
        this.certificateService = certificateService;
        this.userService = userService;
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createCertificate(
            @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            // 1. Get User object once
            String githubId = oauthUser.getAttribute("id").toString();
            User user = userService.getUserByGithubId(githubId);

            // 2. Pass User object to the service
            // This method now returns an existing or new certificate
            Certificate certificate = certificateService.createCertificate(user);

            // 3. Build the enhanced response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificateId", certificate.getId());
            response.put("certificate", certificate);
            response.put("githubUsername", user.getUsername()); // Add githubUsername
            response.put("fullName", user.getFullName());       // Add fullName
            response.put("message", "Certificate retrieved successfully."); // Updated message
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warning("Error creating or retrieving certificate: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            // 4. Handle specific "insufficient badges" error
            if (e.getMessage().contains("must have all")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Handle legacy "final badge" message just in case (from old code)
            if (e.getMessage().contains("final badge")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/view/{certificateId}")
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
}