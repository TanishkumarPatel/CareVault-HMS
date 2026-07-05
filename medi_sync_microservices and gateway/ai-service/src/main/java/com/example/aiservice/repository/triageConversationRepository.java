package com.example.aiservice.repository;

import com.example.aiservice.model.triageConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

public interface triageConversationRepository extends JpaRepository<triageConversation, UUID> {

    List<triageConversation> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    // delete all messages for a completed session
    void deleteBySessionId(String sessionId);

    // count turns for a session (for hard limit check)
    int countBySessionIdAndRole(String sessionId, String role);

    // for scheduled cleanup — delete abandoned conversations older than 24 hours
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
