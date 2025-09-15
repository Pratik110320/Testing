package com.team.OpenGalaxy.service;

import com.team.OpenGalaxy.model.DTOs.ProblemRequestDTO;
import com.team.OpenGalaxy.model.Problem;
import com.team.OpenGalaxy.repository.ProblemRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import com.team.OpenGalaxy.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ProblemService {

    private static final Logger logger = Logger.getLogger(ProblemService.class.getName());
    private final ProblemRepository problemRepository;
    private final UserService userService;
    private final EmailService emailService;
    public ProblemService(ProblemRepository problemRepository, UserService userService,EmailService emailService) {
        this.problemRepository = problemRepository;
        this.userService = userService;
        this.emailService = emailService;
    }


    public Problem createProblem(DefaultOAuth2User oauthUser, ProblemRequestDTO dto) {
        try {
            String githubId = oauthUser.getAttribute("id").toString();
            User user = userService.getUserByGithubId(githubId);

            Problem problem = new Problem();
            problem.setTitle(dto.getTitle());
            problem.setDescription(dto.getDescription());
            problem.setCodeSnippet(dto.getCodeSnippet());
            problem.setRepoLink(dto.getRepoLink());
            problem.setEnvironment(dto.getEnvironment());
            problem.setLanguage(dto.getLanguage());
            problem.setStatus("OPEN");
            problem.setTags(dto.getTags());
            problem.setPostedBy(user.getId());
            problem.setCreatedAt(new Date());
            problem.setUpdatedAt(new Date());
            problem.setSolutionIds(new ArrayList<>());

            // save and capture the saved entity
            Problem savedProblem = problemRepository.save(problem);

            // send email AFTER successful save (does not affect main flow if fails)
            try {
                emailService.sendProblemPostedAlert(savedProblem);
            } catch (Exception e) {
                logger.warning("Failed to send problem posted email: " + e.getMessage());
            }

            return savedProblem;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to create problem: " + e.getMessage());
            throw new RuntimeException("Failed to create problem: " + e.getMessage(), e);
        }
    }

    public List<Problem> getAllProblems() {
        try {
            return problemRepository.findAll();
        } catch (Exception e) {
            logger.severe("Failed to retrieve all problems: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve all problems: " + e.getMessage(), e);
        }
    }

    public Problem getProblemById(String id) {
        try {
            return problemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Problem not found with ID: " + id));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to retrieve problem with ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve problem: " + e.getMessage(), e);
        }
    }

    public List<Problem> getProblemsByUser(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);
            return problemRepository.findByPostedBy(user.getId());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to retrieve problems for user with GitHub ID " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve user problems: " + e.getMessage(), e);
        }
    }

    public List<Problem> getSavedProblems(String githubId) {
        try {
            User user = userService.getUserByGithubId(githubId);
            List<String> savedProblemIds = user.getSavedProblems();
            if (savedProblemIds == null || savedProblemIds.isEmpty()) {
                return new ArrayList<>();
            }
            return problemRepository.findAllById(savedProblemIds);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to retrieve saved problems for user with GitHub ID " + githubId + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve saved problems: " + e.getMessage(), e);
        }
    }

    public Problem updateProblem(String id, ProblemRequestDTO dto, String githubId) {
        try {
            Problem problem = getProblemById(id);
            User user = userService.getUserByGithubId(githubId);

            if (!problem.getPostedBy().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to update this problem");
            }

            problem.setTitle(dto.getTitle());
            problem.setDescription(dto.getDescription());
            problem.setCodeSnippet(dto.getCodeSnippet());
            problem.setRepoLink(dto.getRepoLink());
            problem.setEnvironment(dto.getEnvironment());
            problem.setLanguage(dto.getLanguage());
            problem.setStatus(dto.getStatus());
            problem.setTags(dto.getTags());
            problem.setUpdatedAt(new Date());

            return problemRepository.save(problem);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to update problem " + id + ": " + e.getMessage());
            throw new RuntimeException("Failed to update problem: " + e.getMessage(), e);
        }
    }

    public void deleteProblem(String id, String githubId) {
        try {
            Problem problem = getProblemById(id);
            User user = userService.getUserByGithubId(githubId);

            if (!problem.getPostedBy().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to delete this problem");
            }

            problemRepository.deleteById(id);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to delete problem " + id + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete problem: " + e.getMessage(), e);
        }
    }

    public Problem toggleLikeProblem(String problemId, String githubId) {
        try {
            Problem problem = getProblemById(problemId);
            User user = userService.getUserByGithubId(githubId);

            String userId = user.getId();

            if (problem.getLikes() == null) {
                problem.setLikes(new ArrayList<>());
            }
            if (user.getLikedProblems() == null) {
                user.setLikedProblems(new ArrayList<>());
            }

            List<String> likes = problem.getLikes();
            List<String> userLikes = user.getLikedProblems();

            if (likes.contains(userId)) {
                likes.remove(userId);
                userLikes.remove(problemId);
            } else {
                likes.add(userId);
                userLikes.add(problemId);
            }

            problem.setUpdatedAt(new Date());
            user.setUpdatedAt(new Date());

            userService.save(user);
            return problemRepository.save(problem);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to toggle like for problem " + problemId + ": " + e.getMessage());
            throw new RuntimeException("Failed to toggle like: " + e.getMessage(), e);
        }
    }

    public Problem toggleSaveProblem(String problemId, String githubId) {
        try {
            Problem problem = getProblemById(problemId);
            User user = userService.getUserByGithubId(githubId);

            String userId = user.getId();

            if (problem.getSavedBy() == null) {
                problem.setSavedBy(new ArrayList<>());
            }
            if (user.getSavedProblems() == null) {
                user.setSavedProblems(new ArrayList<>());
            }

            List<String> savedBy = problem.getSavedBy();
            List<String> userSaves = user.getSavedProblems();

            if (savedBy.contains(userId)) {
                savedBy.remove(userId);
                userSaves.remove(problemId);
            } else {
                savedBy.add(userId);
                userSaves.add(problemId);
            }

            problem.setUpdatedAt(new Date());
            user.setUpdatedAt(new Date());

            userService.save(user);
            return problemRepository.save(problem);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to toggle save for problem " + problemId + ": " + e.getMessage());
            throw new RuntimeException("Failed to toggle save: " + e.getMessage(), e);
        }
    }

    public Problem saveProblem(Problem problem) {
        try {
            return problemRepository.save(problem);
        } catch (Exception e) {
            logger.severe("Failed to save problem: " + e.getMessage());
            throw new RuntimeException("Failed to save problem: " + e.getMessage(), e);
        }
    }

    public Problem updateProblemStatus(String problemId, String status) {
        try {
            Problem problem = getProblemById(problemId);
            if (!"OPEN".equals(status) && !"SOLVED".equals(status)) {
                throw new RuntimeException("Invalid status: " + status);
            }
            problem.setStatus(status);
            problem.setUpdatedAt(new Date());
            return problemRepository.save(problem);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to update problem status for problem " + problemId + ": " + e.getMessage());
            throw new RuntimeException("Failed to update problem status: " + e.getMessage(), e);
        }
    }
}