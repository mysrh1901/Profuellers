package com.regulith.repository;

import com.regulith.model.AuditNarrative;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditNarrativeRepository extends JpaRepository<AuditNarrative, Long> {
    List<AuditNarrative> findByEngagementIdOrderByGeneratedAtDesc(String engagementId);
}
