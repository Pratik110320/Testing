package com.pratik.OpenGalaxy.model.DTOs;

import java.util.Date;
import java.util.List;

public class ProblemResponseDTO {
    private String id;
    private String title;
    private String description;
    private String codeSnippet;
    private String repoLink;
    private String environment;
    private String status;
    private List<String> tags;
    private UserResponseDTO postedBy;
    private List<String> likes;
    private List<String> savedBy;
    private List<String> solutionIds;
    private Date createdAt;
    private Date updatedAt;
    private String language;



    // Getters and setters
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }
    public String getRepoLink() {
        return repoLink;
    }

    public void setRepoLink(String repoLink) {
        this.repoLink = repoLink;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public UserResponseDTO getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(UserResponseDTO postedBy) {
        this.postedBy = postedBy;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public List<String> getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(List<String> savedBy) {
        this.savedBy = savedBy;
    }

    public List<String> getSolutionIds() {
        return solutionIds;
    }

    public void setSolutionIds(List<String> solutionIds) {
        this.solutionIds = solutionIds;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}