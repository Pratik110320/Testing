package com.pratik.OpenGalaxy.repository;


import com.pratik.OpenGalaxy.model.Problem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends MongoRepository<Problem, String> {
    Optional<Problem> findById(String id);

    List<Problem> findByPostedBy(String id);
}
