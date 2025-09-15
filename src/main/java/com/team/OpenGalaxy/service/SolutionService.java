package com.team.OpenGalaxy.service;

import com.team.OpenGalaxy.model.DTOs.SolutionRequestDTO;
import com.team.OpenGalaxy.model.DTOs.SolutionResponseDTO;
import com.team.OpenGalaxy.model.DTOs.UserResponseDTO;
import com.team.OpenGalaxy.model.Problem;
import com.team.OpenGalaxy.model.Solution;
import com.team.OpenGalaxy.model.User;
import com.team.OpenGalaxy.repository.SolutionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class SolutionService {

    private static final Logger logger = Logger.getLogger(SolutionService.class.getName());
    private final SolutionRepository solutionRepository;
    private final ProblemService problemService;
    private final UserService userService;
    private final CertificateService certificateService;

    private static final int[] BADGE_THRESHOLDS = {1, 3, 5, 7, 10, 12, 15, 17, 20, 25, 30, 35, 40, 45, 50};
    private static final String[] BADGE_NAMES = {
            "Code Spark", "Stellar Coder", "Cosmic Contributor", "Galaxy Explorer", "Nebula Navigator",
            "Star Solver", "Orbit Master", "Astro Achiever", "Supernova Star", "Interstellar Innovator",
            "Cosmic Trailblazer", "Galactic Guru", "Stellar Vanguard", "Nebula Champion", "Universal Legend"
    };


    public SolutionService(SolutionRepository solutionRepository,
                           ProblemService problemService,
                           UserService userService,
                           @Lazy CertificateService certificateService) {
        this.solutionRepository = solutionRepository;
        this.problemService = problemService;
        this.userService = userService;
        this.certificateService = certificateService;
    }

    public Solution createSolution(DefaultOAuth2User oauthUser, String problemId, SolutionRequestDTO dto) {
        try {
            String githubId = oauthUser.getAttribute("id").toString();
            User user = userService.getUserByGithubId(githubId);
            Problem problem = problemService.getProblemById(problemId);

            Solution solution = new Solution();
            solution.setProblemId(problemId);
            solution.setSubmittedBy(user.getId());
            solution.setContent(dto.getContent());
            solution.setCodeSnippet(dto.getCodeSnippet());
            solution.setRepoLink(dto.getRepoLink());
            solution.setIsAccepted(false);
            solution.setUpvoteCount(0);
            solution.setUpvotes(new ArrayList<>());
            solution.setCreatedAt(new Date());
            solution.setUpdatedAt(new Date());

            Solution savedSolution = solutionRepository.save(solution);

            List<String> solutionIds = problem.getSolutionIds();
            if (solutionIds == null) {
                solutionIds = new ArrayList<>();
            }
            solutionIds.add(savedSolution.getId());
            problem.setSolutionIds(solutionIds);
            problemService.saveProblem(problem);

            return savedSolution;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to create solution: " + e.getMessage());
            throw new RuntimeException("Failed to create solution: " + e.getMessage(), e);
        }
    }

    public List<Solution> getSolutionsByProblemId(String problemId) {
        try {
            return solutionRepository.findByProblemId(problemId);
        } catch (Exception e) {
            logger.severe("Failed to retrieve solutions for problem " + problemId + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve solutions: " + e.getMessage(), e);
        }
    }

    public List<Solution> getSolutionsByUser(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);
            return solutionRepository.findBySubmittedBy(user.getId());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to retrieve solutions for user with GitHub ID " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve user solutions: " + e.getMessage(), e);
        }
    }

    public Solution updateSolution(String solutionId, SolutionRequestDTO dto, String githubId) {
        try {
            Solution solution = solutionRepository.findById(solutionId)
                    .orElseThrow(() -> new RuntimeException("Solution not found with ID: " + solutionId));
            User user = userService.getUserByGithubId(githubId);

            if (!solution.getSubmittedBy().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to update this solution");
            }

            solution.setContent(dto.getContent());
            solution.setCodeSnippet(dto.getCodeSnippet());
            solution.setRepoLink(dto.getRepoLink());
            solution.setUpdatedAt(new Date());

            return solutionRepository.save(solution);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to update solution " + solutionId + ": " + e.getMessage());
            throw new RuntimeException("Failed to update solution: " + e.getMessage(), e);
        }
    }

    public void deleteSolution(String solutionId, String githubId) {
        try {
            Solution solution = solutionRepository.findById(solutionId)
                    .orElseThrow(() -> new RuntimeException("Solution not found with ID: " + solutionId));
            User user = userService.getUserByGithubId(githubId);

            if (!solution.getSubmittedBy().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to delete this solution");
            }

            Problem problem = problemService.getProblemById(solution.getProblemId());
            List<String> solutionIds = problem.getSolutionIds();
            if (solutionIds != null) {
                solutionIds.remove(solutionId);
                problem.setSolutionIds(solutionIds);
                if (solution.isAccepted()) {
                    problemService.updateProblemStatus(solution.getProblemId(), "OPEN");
                }
                problemService.saveProblem(problem);
            }

            solutionRepository.deleteById(solutionId);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to delete solution " + solutionId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete solution: " + e.getMessage(), e);
        }
    }

    public Solution toggleUpvoteSolution(String solutionId, String githubId) {
        try {
            Solution solution = solutionRepository.findById(solutionId)
                    .orElseThrow(() -> new RuntimeException("Solution not found with ID: " + solutionId));
            User user = userService.getUserByGithubId(githubId);

            String userId = user.getId();

            if (solution.getUpvotes() == null) {
                solution.setUpvotes(new ArrayList<>());
            }

            List<String> upvotes = solution.getUpvotes();
            if (upvotes.contains(userId)) {
                upvotes.remove(userId);
                solution.setUpvoteCount(solution.getUpvoteCount() - 1);
            } else {
                upvotes.add(userId);
                solution.setUpvoteCount(solution.getUpvoteCount() + 1);
            }

            solution.setUpdatedAt(new Date());
            return solutionRepository.save(solution);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to toggle upvote for solution " + solutionId + ": " + e.getMessage());
            throw new RuntimeException("Failed to toggle upvote: " + e.getMessage(), e);
        }
    }

    public Solution acceptSolution(String solutionId, String githubId) {
        try {
            Solution solution = solutionRepository.findById(solutionId)
                    .orElseThrow(() -> new RuntimeException("Solution not found with ID: " + solutionId));
            Problem problem = problemService.getProblemById(solution.getProblemId());
            User user = userService.getUserByGithubId(githubId);

            if (!problem.getPostedBy().equals(user.getId())) {
                throw new RuntimeException("Only the problem poster can accept a solution");
            }

            if (solution.isAccepted()) {
                solution.setIsAccepted(false);
                problemService.updateProblemStatus(solution.getProblemId(), "OPEN");
            } else {
                List<Solution> problemSolutions = solutionRepository.findByProblemId(solution.getProblemId());
                for (Solution s : problemSolutions) {
                    if (!s.getId().equals(solutionId) && s.isAccepted()) {
                        s.setIsAccepted(false);
                        solutionRepository.save(s);
                    }
                }
                solution.setIsAccepted(true);
                problemService.updateProblemStatus(solution.getProblemId(), "SOLVED");


                User submitter = userService.getUserById(solution.getSubmittedBy());
                List<String> oldBadges = new ArrayList<>(submitter.getBadges() != null ? submitter.getBadges() : new ArrayList<>());
                submitter.setPoints(submitter.getPoints() + 1);
                assignBadges(submitter);
                userService.save(submitter);


                List<String> newBadges = submitter.getBadges();
                if (newBadges.size() > oldBadges.size()) {
                    try {

                        String submitterGithubId = submitter.getGithubId();

                        if (submitterGithubId != null) {
                            certificateService.generateCertificateForAchievement(submitterGithubId);
                            logger.info("Certificate generated for user " + submitterGithubId + " after earning new badge");
                        }
                    } catch (Exception e) {
                        logger.warning("Failed to generate certificate for user after badge achievement: " + e.getMessage());

                    }
                }
            }

            solution.setUpdatedAt(new Date());
            return solutionRepository.save(solution);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to accept solution " + solutionId + ": " + e.getMessage());
            throw new RuntimeException("Failed to accept solution: " + e.getMessage(), e);
        }
    }

    private void assignBadges(User user) {
        int points = user.getPoints();
        List<String> currentBadges = user.getBadges() != null ? user.getBadges() : new ArrayList<>();

        for (int i = 0; i < BADGE_THRESHOLDS.length; i++) {
            if (points >= BADGE_THRESHOLDS[i] && !currentBadges.contains(BADGE_NAMES[i])) {
                currentBadges.add(BADGE_NAMES[i]);
            }
        }

        user.setBadges(currentBadges);
    }

    public SolutionResponseDTO toResponseDTO(Solution solution) {
        try {
            SolutionResponseDTO dto = new SolutionResponseDTO();
            dto.setId(solution.getId());
            dto.setProblemId(solution.getProblemId());
            dto.setSubmittedBy(solution.getSubmittedBy());
            User user = userService.getUserById(solution.getSubmittedBy());
            UserResponseDTO userDTO = new UserResponseDTO();
            userDTO.setUsername(user.getUsername());
            userDTO.setFullName(user.getFullName());
            userDTO.setProfilePicture(user.getProfilePicture());
            userDTO.setPoints(user.getPoints());
            userDTO.setBadges(user.getBadges());
            dto.setUser(userDTO);
            dto.setContent(solution.getContent());
            dto.setCodeSnippet(solution.getCodeSnippet());
            dto.setRepoLink(solution.getRepoLink());
            dto.setAccepted(solution.isAccepted());
            dto.setUpvoteCount(solution.getUpvoteCount());
            dto.setUpvotes(solution.getUpvotes());
            dto.setCreatedAt(solution.getCreatedAt());
            dto.setUpdatedAt(solution.getUpdatedAt());
            return dto;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to convert solution to DTO: " + e.getMessage());
            throw new RuntimeException("Failed to convert solution to DTO: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getUserAchievementStats(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);
            List<Solution> userSolutions = getSolutionsByUser(githubId);

            long acceptedSolutions = userSolutions.stream()
                    .filter(Solution::isAccepted)
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSolutions", userSolutions.size());
            stats.put("acceptedSolutions", acceptedSolutions);
            stats.put("points", user.getPoints());
            stats.put("badges", user.getBadges() != null ? user.getBadges() : new ArrayList<>());
            stats.put("badgeCount", user.getBadges() != null ? user.getBadges().size() : 0);


            int currentPoints = user.getPoints();
            String nextBadge = null;
            int pointsToNext = 0;

            for (int i = 0; i < BADGE_THRESHOLDS.length; i++) {
                if (currentPoints < BADGE_THRESHOLDS[i]) {
                    nextBadge = BADGE_NAMES[i];
                    pointsToNext = BADGE_THRESHOLDS[i] - currentPoints;
                    break;
                }
            }

            stats.put("nextBadge", nextBadge);
            stats.put("pointsToNextBadge", pointsToNext);

            return stats;
        } catch (Exception e) {
            logger.severe("Failed to get achievement stats for user " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to get achievement stats: " + e.getMessage(), e);
        }
    }
}
