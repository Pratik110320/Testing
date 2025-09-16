package com.team.OpenGalaxy.controller;

import com.team.OpenGalaxy.model.Certificate;
import com.team.OpenGalaxy.service.CertificateService;
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

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createCertificate(
            @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            String githubId = oauthUser.getAttribute("id").toString();

            Certificate certificate = certificateService.createCertificate(githubId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificateId", certificate.getId());
            response.put("certificate", certificate);
            response.put("message", "Certificate created successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warning("Error creating certificate: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
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