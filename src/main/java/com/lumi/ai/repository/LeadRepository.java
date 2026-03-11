package com.lumi.ai.repository;

import com.lumi.ai.model.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {
    
    boolean existsByEmail(String email);
    
    long countByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT l FROM Lead l WHERE " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(l.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Lead> search(String searchTerm, Pageable pageable);
}
