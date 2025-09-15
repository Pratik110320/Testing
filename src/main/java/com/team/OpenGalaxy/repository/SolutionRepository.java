package com.team.OpenGalaxy.repository;


import com.team.OpenGalaxy.model.Solution;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SolutionRepository extends MongoRepository<Solution, String> {
    List<Solution> findByProblemId(String problemId);
    List<Solution> findBySubmittedBy(String submittedById);

}
