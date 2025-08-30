package com.pratik.OpenGalaxy.model.DTOs;


// Request DTO for certificate generation
public class CertificateRequestDTO {
    private String courseTitle;
    private boolean autoGenerate; // If true, auto-generate based on achievements

    public CertificateRequestDTO() {}

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }

    public void setAutoGenerate(boolean autoGenerate) {
        this.autoGenerate = autoGenerate;
    }
}

