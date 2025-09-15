    package com.team.OpenGalaxy.controller;

import com.team.OpenGalaxy.model.DTOs.SolutionRequestDTO;
import com.team.OpenGalaxy.model.DTOs.SolutionResponseDTO;
import com.team.OpenGalaxy.model.Solution;
import com.team.OpenGalaxy.service.SolutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solutions")
public class SolutionController {

    private static final Logger logger = Logger.getLogger(SolutionController.class.getName());
    private final SolutionService solutionService;

    public SolutionController(SolutionService solutionService) {
        this.solutionService = solutionService;
    }

    @PostMapping("/{problemId}")
    public ResponseEntity<SolutionResponseDTO> createSolution(@PathVariable String problemId,
                                                              @RequestBody SolutionRequestDTO dto,
                                                              @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to create solution for problem: " + problemId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Solution solution = solutionService.createSolution(oauthUser, problemId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(solutionService.toResponseDTO(solution));
        } catch (RuntimeException e) {
            logger.warning("Error creating solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.severe("Failed to create solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<SolutionResponseDTO>> getSolutionsByProblemId(@PathVariable String problemId) {
        try {
            List<Solution> solutions = solutionService.getSolutionsByProblemId(problemId);
            return ResponseEntity.ok(solutions.stream()
                    .map(solutionService::toResponseDTO)
                    .collect(Collectors.toList()));
        } catch (RuntimeException e) {
            logger.warning("Error retrieving solutions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to retrieve solutions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<SolutionResponseDTO>> getOwnSolutions(@AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to retrieve own solutions");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            List<Solution> solutions = solutionService.getSolutionsByUser(githubId);
            return ResponseEntity.ok(solutions.stream()
                    .map(solutionService::toResponseDTO)
                    .collect(Collectors.toList()));
        } catch (RuntimeException e) {
            logger.warning("Error retrieving own solutions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to retrieve own solutions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{solutionId}")
    public ResponseEntity<SolutionResponseDTO> updateSolution(@PathVariable String solutionId,
                                                              @RequestBody SolutionRequestDTO dto,
                                                              @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to update solution: " + solutionId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            String githubId = oauthUser.getAttribute("id").toString();
            Solution solution = solutionService.updateSolution(solutionId, dto, githubId);
            return ResponseEntity.ok(solutionService.toResponseDTO(solution));
        } catch (RuntimeException e) {
            logger.warning("Error updating solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            logger.severe("Failed to update solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{solutionId}")
    public ResponseEntity<Void> deleteSolution(@PathVariable String solutionId,
                                               @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to delete solution: " + solutionId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            solutionService.deleteSolution(solutionId, githubId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warning("Error deleting solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            logger.severe("Failed to delete solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{solutionId}/like")
    public ResponseEntity<SolutionResponseDTO> toggleLike(@PathVariable String solutionId,
                                                          @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to toggle like for solution: " + solutionId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            Solution solution = solutionService.toggleUpvoteSolution(solutionId, githubId);
            return ResponseEntity.ok(solutionService.toResponseDTO(solution));
        } catch (RuntimeException e) {
            logger.warning("Error toggling like for solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to toggle like for solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{solutionId}/upvote")
    public ResponseEntity<SolutionResponseDTO> toggleUpvote(@PathVariable String solutionId,
                                                            @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to toggle upvote for solution: " + solutionId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            Solution solution = solutionService.toggleUpvoteSolution(solutionId, githubId);
            return ResponseEntity.ok(solutionService.toResponseDTO(solution));
        } catch (RuntimeException e) {
            logger.warning("Error toggling upvote for solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.severe("Failed to toggle upvote for solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{solutionId}/accept")
    public ResponseEntity<SolutionResponseDTO> acceptSolution(@PathVariable String solutionId,
                                                              @AuthenticationPrincipal DefaultOAuth2User oauthUser) {
        try {
            if (oauthUser == null) {
                logger.warning("Unauthorized attempt to accept solution: " + solutionId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String githubId = oauthUser.getAttribute("id").toString();
            Solution solution = solutionService.acceptSolution(solutionId, githubId);
            return ResponseEntity.ok(solutionService.toResponseDTO(solution));
        } catch (RuntimeException e) {
            logger.warning("Error accepting solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            logger.severe("Failed to accept solution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}