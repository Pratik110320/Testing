package com.team.OpenGalaxy.service;

import com.team.OpenGalaxy.model.DTOs.UserResponseDTO;
import com.team.OpenGalaxy.model.Solution;
import com.team.OpenGalaxy.model.User;
import com.team.OpenGalaxy.repository.SolutionRepository;
import com.team.OpenGalaxy.repository.UserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private static final Logger logger = Logger.getLogger(LeaderboardService.class.getName());
    private final MongoTemplate mongoTemplate;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SolutionRepository solutionRepository;

    public LeaderboardService(MongoTemplate mongoTemplate, UserService userService, UserRepository userRepository, SolutionRepository solutionRepository) {
        this.mongoTemplate = mongoTemplate;
        this.userService = userService;
        this.userRepository = userRepository;
        this.solutionRepository = solutionRepository;
    }

    public List<UserResponseDTO> getTopUsers(LocalDateTime start, LocalDateTime end, int limit) {
        try {
            logger.info("Fetching leaderboard for period: start=" + start + ", end=" + end);

            // Aggregate solutions and join with users
            Aggregation solutionAggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("createdAt").gte(start).lte(end)),
                    Aggregation.lookup("users", "submittedBy", "_id", "userData"),
                    Aggregation.unwind("$userData"), // Use basic unwind, compatible with older versions
                    Aggregation.group("submittedBy")
                            .count().as("solutionCount")
                            .sum(ConditionalOperators.when(Criteria.where("isAccepted").is(true)).then(1).otherwise(0)).as("acceptedCount")
                            .first("userData").as("user"),
                    Aggregation.project()
                            .and("_id").as("userId")
                            .and("solutionCount").as("solutionCount")
                            .and("acceptedCount").as("acceptedCount")
                            .and("user.points").as("points")
                            .and("user").as("user")
            );

            AggregationResults<Map> solutionResults = mongoTemplate.aggregate(solutionAggregation, Solution.class, Map.class);
            Map<String, UserStats> userStatsMap = new HashMap<>();

            // Process aggregation results
            for (Map result : solutionResults.getMappedResults()) {
                String userId = (String) result.get("userId");
                Integer solutionCount = ((Number) result.get("solutionCount")).intValue();
                Integer acceptedCount = ((Number) result.get("acceptedCount")).intValue();
                Integer points = result.get("points") != null ? ((Number) result.get("points")).intValue() : 0;
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) result.get("user");
                User user = userService.getUserById(userId);

                if (user == null) {
                    logger.warning("Skipping solution for invalid user ID: " + userId);
                    continue;
                }

                userStatsMap.put(userId, new UserStats(solutionCount, acceptedCount, points, user));
            }

            // Include all users, even those without solutions
            List<User> allUsers = userRepository.findAll();
            logger.info("Total users found: " + allUsers.size());
            for (User user : allUsers) {
                userStatsMap.computeIfAbsent(user.getId(), k -> new UserStats(0, 0, user.getPoints(), user));
            }

            // Calculate scores and sort
            List<UserResponseDTO> result = userStatsMap.values().stream()
                    .map(stats -> {
                        stats.score = stats.points + 2 * stats.acceptedCount + stats.solutionCount;
                        logger.info("User " + stats.user.getUsername() + " score: " + stats.score +
                                ", points: " + stats.points +
                                ", accepted: " + stats.acceptedCount +
                                ", solutions: " + stats.solutionCount);
                        return stats;
                    })
                    .sorted((a, b) -> {
                        if (a.score != b.score) return Double.compare(b.score, a.score);
                        if (a.points != b.points) return Integer.compare(b.points, a.points);
                        if (a.acceptedCount != b.acceptedCount) return Integer.compare(b.acceptedCount, a.acceptedCount);
                        return Integer.compare(b.solutionCount, a.solutionCount);
                    })
                    .limit(limit)
                    .map(stats -> toUserResponseDTO(stats.user))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("Leaderboard result size: " + result.size());
            return result;
        } catch (Exception e) {
            logger.severe("Failed to get top users: " + e.getMessage());
            throw new RuntimeException("Failed to get leaderboard: " + e.getMessage(), e);
        }
    }

    private static class UserStats {
        int solutionCount;
        int acceptedCount;
        int points;
        double score;
        User user;

        UserStats(int solutionCount, int acceptedCount, int points, User user) {
            this.solutionCount = solutionCount;
            this.acceptedCount = acceptedCount;
            this.points = points;
            this.score = 0;
            this.user = user;
        }
    }

    private UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            logger.warning("Attempted to convert null user to UserResponseDTO");
            return null;
        }
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setPoints(user.getPoints());
        dto.setBadges(user.getBadges());
        return dto;
    }

    public Map<String, LocalDateTime> getTimeRange(String period) {
        ZoneId zoneId = ZoneId.of("Asia/Kolkata"); // IST
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDateTime start, end;

        switch (period.toLowerCase()) {
            case "yearly":
                start = now.with(LocalTime.MIN).withDayOfYear(1);
                end = now.with(LocalTime.MAX);
                break;
            case "monthly":
                start = now.with(LocalTime.MIN).withDayOfMonth(1);
                end = now.with(LocalTime.MAX);
                break;
            case "weekly":
                start = now.with(LocalTime.MIN).with(DayOfWeek.MONDAY);
                end = now.with(LocalTime.MAX);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        logger.info("Time range for " + period + ": start=" + start + ", end=" + end);
        return Map.of("start", start, "end", end);
    }
}