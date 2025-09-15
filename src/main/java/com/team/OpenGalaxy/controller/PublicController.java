package com.team.OpenGalaxy.controller;

import com.team.OpenGalaxy.model.DTOs.LeaderboardResponseDTO;
import com.team.OpenGalaxy.repository.ProblemRepository;
import com.team.OpenGalaxy.service.LeaderboardService;
import com.team.OpenGalaxy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Logger;
@RestController
@RequestMapping("/api/public")
public class PublicController {


    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private final UserService userService;
    private final ProblemRepository problemRepository;
    private final LeaderboardService leaderboardService;

    public PublicController(UserService userService, ProblemRepository problemRepository,LeaderboardService leaderboardService) {
        this.userService = userService;
        this.problemRepository = problemRepository;
        this.leaderboardService=leaderboardService;
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponseDTO> getLeaderboard() {
        try {
            LeaderboardResponseDTO response = new LeaderboardResponseDTO();

            Map<String, LocalDateTime> yearlyRange = leaderboardService.getTimeRange("yearly");
            response.setYearly(leaderboardService.getTopUsers(yearlyRange.get("start"), yearlyRange.get("end"), 3));

            Map<String, LocalDateTime> monthlyRange = leaderboardService.getTimeRange("monthly");
            response.setMonthly(leaderboardService.getTopUsers(monthlyRange.get("start"), monthlyRange.get("end"), 3));

            Map<String, LocalDateTime> weeklyRange = leaderboardService.getTimeRange("weekly");
            response.setWeekly(leaderboardService.getTopUsers(weeklyRange.get("start"), weeklyRange.get("end"), 3));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warning("Error retrieving leaderboard: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            logger.severe("Failed to retrieve leaderboard: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

}
