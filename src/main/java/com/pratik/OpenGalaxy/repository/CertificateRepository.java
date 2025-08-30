package com.pratik.OpenGalaxy.repository;


import com.pratik.OpenGalaxy.model.Certificate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends MongoRepository<Certificate, String> {

    List<Certificate> findByUserId(String userId);

    List<Certificate> findByUserIdAndIsActive(String userId, boolean isActive);

    Optional<Certificate> findByIdAndIsActive(String id, boolean isActive);

    List<Certificate> findByCourseTitle(String courseTitle);

    List<Certificate> findByPrimarySkill(String primarySkill);

    boolean existsByIdAndIsActive(String id, boolean isActive);

    long countByUserId(String userId);
}