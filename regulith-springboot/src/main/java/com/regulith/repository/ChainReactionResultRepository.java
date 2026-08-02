package com.regulith.repository;

import com.regulith.model.ChainReactionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChainReactionResultRepository extends JpaRepository<ChainReactionResult, Long> {
    List<ChainReactionResult> findByEventId(Long eventId);
    List<ChainReactionResult> findByEngagementIdOrderByTimestampDesc(String engagementId);
}
