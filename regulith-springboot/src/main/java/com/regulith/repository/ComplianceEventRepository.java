package com.regulith.repository;

import com.regulith.model.ComplianceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplianceEventRepository extends JpaRepository<ComplianceEvent, Long> {
    List<ComplianceEvent> findByEngagementIdOrderByTimestampDesc(String engagementId);
    List<ComplianceEvent> findByProcessedFalse();
}
