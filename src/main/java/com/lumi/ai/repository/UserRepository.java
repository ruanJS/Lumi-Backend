package com.lumi.ai.repository;

import com.lumi.ai.model.User;
import com.lumi.ai.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findTopByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRole(UserRole role);

    long countByRole(UserRole role);
}
