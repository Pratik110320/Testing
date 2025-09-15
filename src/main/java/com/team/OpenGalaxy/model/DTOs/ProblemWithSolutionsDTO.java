package com.team.OpenGalaxy.model.DTOs;

import java.util.List;

public class ProblemWithSolutionsDTO {
    private ProblemResponseDTO problem;
    private List<SolutionResponseDTO> solutions;

    // Getters and setters
    public ProblemResponseDTO getProblem() {
        return problem;
    }

    public void setProblem(ProblemResponseDTO problem) {
        this.problem = problem;
    }

    public List<SolutionResponseDTO> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<SolutionResponseDTO> solutions) {
        this.solutions = solutions;
    }
}