package com.pratik.OpenGalaxy.model.DTOs;

// DTO for certificate verification response
class CertificateVerificationDTO {
    private boolean valid;
    private String certificateId;
    private String message;
    private CertificateResponseDTO certificate;

    public CertificateVerificationDTO() {}

    public CertificateVerificationDTO(boolean valid, String certificateId, String message) {
        this.valid = valid;
        this.certificateId = certificateId;
        this.message = message;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CertificateResponseDTO getCertificate() {
        return certificate;
    }

    public void setCertificate(CertificateResponseDTO certificate) {
        this.certificate = certificate;
    }}