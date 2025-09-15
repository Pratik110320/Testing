package com.team.OpenGalaxy.controller;

import com.team.OpenGalaxy.model.DTOs.ProblemRequestDTO;
import com.team.OpenGalaxy.model.DTOs.ProblemResponseDTO;
import com.team.OpenGalaxy.model.DTOs.ProblemWithSolutionsDTO;
import com.team.OpenGalaxy.model.DTOs.UserResponseDTO;
import com.team.OpenGalaxy.model.Problem;
import com.team.OpenGalaxy.model.Solution;
import com.team.OpenGalaxy.model.User;
import com.team.OpenGalaxy.service.ProblemService;
import com.team.OpenGalaxy.service.SolutionService;
import com.team.OpenGalaxy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private static final Logger logger = Logger.getLogger(ProblemController.class.getName());
    private final ProblemService problemService;
    private final SolutionService solutionService;
    private final UserService userService;

    public ProblemController(UserService userService, ProblemService problemService, SolutionService solutionService) {
        this.problemService = problemService;
        this.solutionService = solutionService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ProblemResponseDTO> createProblem(@AuthenticationPrincipal DefaultOAuth2User oauthUser,
                                                            @RequestBody ProblemRequestDTO dto) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to create problem");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Problem problem = problemService.createProblem(oauthUser, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(problem));
        } catch (RuntimeException e) {
            logger.warning("Error creating problem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.severe("Failed to create problem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<ProblemResponseDTO>> getOwnProblems(@AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to retrieve own problems");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            List<Problem> problems = problemService.getProblemsByUser(githubId);
            return ResponseEntity.ok(problems.stream().map(this::toResponseDTO).collect(Collectors.toList()));
        } catch (RuntimeException e) {
            logger.warning("Error retrieving own problems: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to retrieve own problems: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/saved")
    public ResponseEntity<List<ProblemResponseDTO>> getSavedProblems(@AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to retrieve saved problems");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            List<Problem> problems = problemService.getSavedProblems(githubId);
            return ResponseEntity.ok(problems.stream().map(this::toResponseDTO).collect(Collectors.toList()));
        } catch (RuntimeException e) {
            logger.warning("Error retrieving saved problems: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to retrieve saved problems: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<ProblemResponseDTO>> getAllProblems() {
        try {
            List<Problem> problems = problemService.getAllProblems();
            return ResponseEntity.ok(problems.stream().map(this::toResponseDTO).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.severe("Failed to retrieve problems: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponseDTO> getProblemById(@PathVariable String id) {
        try {
            Problem problem = problemService.getProblemById(id);
            return ResponseEntity.ok(toResponseDTO(problem));
        } catch (RuntimeException e) {
            logger.warning("Problem not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to retrieve problem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}/solutions")
    public ResponseEntity<ProblemWithSolutionsDTO> getProblemWithSolutions(@PathVariable String id) {
        try {
            Problem problem = problemService.getProblemById(id);
            List<Solution> solutions = solutionService.getSolutionsByProblemId(id);
            ProblemWithSolutionsDTO response = new ProblemWithSolutionsDTO();
            response.setProblem(toResponseDTO(problem));
            response.setSolutions(solutions.stream()
                    .map(solutionService::toResponseDTO)
                    .collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warning("Problem not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to retrieve problem with solutions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProblemResponseDTO> updateProblem(@PathVariable String id,
                                                            @RequestBody ProblemRequestDTO dto,
                                                            @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to update problem: " + id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            String githubId = oauthUser.getAttribute("id").toString();
            Problem problem = problemService.updateProblem(id, dto, githubId);
            return ResponseEntity.ok(toResponseDTO(problem));
        } catch (RuntimeException e) {
            logger.warning("Error updating problem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            logger.severe("Failed to update problem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable String id,
                                              @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to delete problem: " + id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            problemService.deleteProblem(id, githubId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warning("Error deleting problem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            logger.severe("Failed to delete problem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<ProblemResponseDTO> toggleLike(@PathVariable String id,
                                                         @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to toggle like for problem: " + id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            Problem problem = problemService.toggleLikeProblem(id, githubId);
            return ResponseEntity.ok(toResponseDTO(problem));
        } catch (RuntimeException e) {
            logger.warning("Error toggling like: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to toggle like: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}/save")
    public ResponseEntity<ProblemResponseDTO> toggleSaveProblem(@PathVariable String id,
                                                                @AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                logger.warning("Unauthorized attempt to toggle save for problem: " + id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = principal.getAttribute("id").toString();
            Problem problem = problemService.toggleSaveProblem(id, githubId);
            return ResponseEntity.ok(toResponseDTO(problem));
        } catch (RuntimeException e) {
            logger.warning("Error toggling save: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to toggle save: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private ProblemResponseDTO toResponseDTO(Problem problem) {
        ProblemResponseDTO dto = new ProblemResponseDTO();
        dto.setId(problem.getId());
        dto.setTitle(problem.getTitle());
        dto.setDescription(problem.getDescription());
        dto.setCodeSnippet(problem.getCodeSnippet());
        dto.setRepoLink(problem.getRepoLink());
        dto.setEnvironment(problem.getEnvironment());
        dto.setLanguage(problem.getLanguage());
        dto.setStatus(problem.getStatus());
        dto.setTags(problem.getTags());

        // 🔹 Convert postedBy (String ID) to UserResponseDTO
        String postedById = problem.getPostedBy(); // this is the user ID stored in Problem
        if (postedById != null) {
            try {
                User user = userService.getUserById(postedById); // fetch User by ID
                if (user != null) {
                    // PLACE THE BLOCK HERE
                    UserResponseDTO userDTO = new UserResponseDTO();
                    userDTO.setUsername(user.getUsername());
                    userDTO.setFullName(user.getFullName());
                    userDTO.setProfilePicture(user.getProfilePicture());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setGithubId(user.getGithubId());
                    userDTO.setPoints(user.getPoints());
                    userDTO.setBadges(user.getBadges());
                    dto.setPostedBy(userDTO);
                } else {
                    dto.setPostedBy(null);
                }
            } catch (Exception e) {
                dto.setPostedBy(null); // fallback if user not found
            }
        } else {
            dto.setPostedBy(null);
        }

        dto.setLikes(problem.getLikes());
        dto.setSavedBy(problem.getSavedBy());
        dto.setSolutionIds(problem.getSolutionIds());
        dto.setCreatedAt(problem.getCreatedAt());
        dto.setUpdatedAt(problem.getUpdatedAt());
        return dto;
    }

}