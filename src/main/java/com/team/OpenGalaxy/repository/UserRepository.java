package com.team.OpenGalaxy.repository;


import com.team.OpenGalaxy.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByGithubId(String githubId);

}
