package com.team.OpenGalaxy.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String githubId;
    private String password;
    private String email;
    private String username;
    private String fullName;
    private String profilePicture;
    private int points;
    private List<String> badges;
    private List<String> likedProblems;
    private List<String> savedProblems;
    private Date createdAt;
    private Date updatedAt;

    public User() {
        this.points = 0;
        this.badges = new ArrayList<>();
        this.likedProblems = new ArrayList<>();
        this.savedProblems = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public List<String> getLikedProblems() {
        return likedProblems;
    }

    public void setLikedProblems(List<String> likedProblems) {
        this.likedProblems = likedProblems;
    }

    public List<String> getSavedProblems() {
        return savedProblems;
    }

    public void setSavedProblems(List<String> savedProblems) {
        this.savedProblems = savedProblems;
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