package com.team.OpenGalaxy.model.DTOs;


import java.util.Date;
import java.util.List;

class CertificateResponseDTO {
    private String id;
    private String userName;
    private String courseTitle;
    private String latestBadge;
    private List<String> allBadges;
    private String verificationUrl;
    private Date generatedAt;
    private String issuedDate;
    private boolean isActive;

    public CertificateResponseDTO() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getlatestBadge() {
        return latestBadge;
    }

    public void setlatestBadge(String latestBadge) {
        this.latestBadge = latestBadge;
    }

    public List<String> getallBadges() {
        return allBadges;
    }

    public void setallBadges(List<String> allBadges) {
        this.allBadges = allBadges;
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        this.verificationUrl = verificationUrl;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}