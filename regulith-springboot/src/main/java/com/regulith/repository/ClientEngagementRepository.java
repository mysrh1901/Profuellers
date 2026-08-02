package com.regulith.repository;

import com.regulith.model.ClientEngagement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientEngagementRepository extends JpaRepository<ClientEngagement, String> {
}
