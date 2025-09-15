package com.team.OpenGalaxy.model.DTOs;

import java.util.List;

public class LeaderboardResponseDTO {

    private List<UserResponseDTO> yearly;
    private List<UserResponseDTO> monthly;
    private List<UserResponseDTO> weekly;

    public List<UserResponseDTO> getYearly() {
        return yearly;
    }

    public void setYearly(List<UserResponseDTO> yearly) {
        this.yearly = yearly;
    }

    public List<UserResponseDTO> getMonthly() {
        return monthly;
    }

    public void setMonthly(List<UserResponseDTO> monthly) {
        this.monthly = monthly;
    }

    public List<UserResponseDTO> getWeekly() {
        return weekly;
    }

    public void setWeekly(List<UserResponseDTO> weekly) {
        this.weekly = weekly;
    }
}