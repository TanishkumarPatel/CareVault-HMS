package com.example.aiservice.service;

import com.example.aiservice.dto.chatRequest;
import com.example.aiservice.dto.chatResponse;
import com.example.aiservice.dto.triageResponse;
import com.example.aiservice.model.triageConversation;
import com.example.aiservice.model.triageRecord;
import com.example.aiservice.repository.triageConversationRepository;
import com.example.aiservice.repository.triageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class triageService {
    private static final Logger log = LoggerFactory.getLogger(triageService.class);
    private static final int MAX_TURNS = 4;

    private final geminiService geminiService;
    private final triageRepository triageRepository;
    private final triageConversationRepository conversationRepository;

    public triageService(geminiService geminiService,
                         triageRepository triageRepository,
                         triageConversationRepository conversationRepository) {
        this.geminiService = geminiService;
        this.triageRepository = triageRepository;
        this.conversationRepository = conversationRepository;
    }

    // ─── EXISTING METHOD (unchanged) ───────────────────────────────────────────
    public triageResponse getById(UUID id) {
        triageRecord record = triageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Triage record not found: " + id));
        return toResponse(record);
    }

    public void updateStatus(UUID id, String status) {
        triageRecord record = triageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Triage record not found: " + id));
        record.setStatus(status);
        triageRepository.save(record);
    }

    // ─── NEW: MULTI-TURN CHAT ───────────────────────────────────────────────────

    @Transactional
    public chatResponse chat(chatRequest request) {

        String sessionId = request.getSessionId();
        String patientId = request.getPatientId();
        String userMessage = request.getMessage().trim();

        // ── Guardrail 1: Prompt injection check (Java, free) ──
        if (isPromptInjection(userMessage)) {
            log.warn("Prompt injection detected for session: {}", sessionId);
            throw new IllegalArgumentException("Invalid input. Please describe your medical symptoms.");
        }

        // ── Guardrail 2: Medical input validation (Gemini YES/NO call) ──
        if (!geminiService.isMedicalInput(userMessage)) {
            log.warn("Non-medical input detected for session: {}", sessionId);
            throw new IllegalArgumentException("Please describe your medical symptoms.");
        }

        // ── Count current turns (number of user messages so far) ──
        int currentTurns = conversationRepository.countBySessionIdAndRole(sessionId, "user");

        // ── Save patient message ──
        saveMessage(sessionId, patientId, "user", userMessage, currentTurns + 1);

        // ── Fetch full conversation history ──
        List<triageConversation> history = conversationRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        // ── Build history list for Gemini ──
        List<Map<String, String>> conversationHistory = new ArrayList<>();
        for (triageConversation msg : history) {
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("message", msg.getMessage());
            conversationHistory.add(m);
        }

        // ── Check if we've hit the turn limit ──
        boolean isForceFinal = (currentTurns + 1) >= MAX_TURNS;

        // ── Call Gemini main chat ──
        Object geminiResult = geminiService.chat(conversationHistory, isForceFinal);

        // ── Handle response ──
        if (geminiResult instanceof Map) {
            // Final result — AI is confident (or forced)
            @SuppressWarnings("unchecked")
            Map<String, String> result = (Map<String, String>) geminiResult;

            if ("INVALID".equals(result.get("department"))) {
                // Delete conversation and throw error
                conversationRepository.deleteBySessionId(sessionId);
                throw new IllegalArgumentException("Please enter valid medical symptoms.");
            }

            // Build symptoms summary from all user messages
            String symptomsSummary = history.stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .map(triageConversation::getMessage)
                    .reduce("", (a, b) -> a + " " + b)
                    .trim();

            // Save triage record
            triageRecord record = new triageRecord();
            record.setPatientId(patientId);
            record.setSymptoms(symptomsSummary);
            record.setSuggestedDepartment(result.get("department"));
            record.setUrgencyLevel(result.get("urgency"));
            record.setReason(result.get("reason"));
            triageRecord saved = triageRepository.save(record);

            // Delete conversation history (cleanup)
            conversationRepository.deleteBySessionId(sessionId);
            log.info("Chat completed for session: {}, triageId: {}", sessionId, saved.getId());

            return chatResponse.finalResult(
                    result.get("department"),
                    result.get("urgency"),
                    result.get("reason"),
                    saved.getId().toString()
            );

        } else {
            // Follow-up question — save AI message and return to patient
            String aiQuestion = (String) geminiResult;
            saveMessage(sessionId, patientId, "model", aiQuestion, currentTurns + 1);
            return chatResponse.question(aiQuestion);
        }
    }

    // ─── SCHEDULED CLEANUP (runs every day at midnight) ────────────────────────

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupAbandonedConversations() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        conversationRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Scheduled cleanup: deleted abandoned conversations older than 24 hours");
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private void saveMessage(String sessionId, String patientId, String role, String message, int turnCount) {
        triageConversation msg = new triageConversation();
        msg.setSessionId(sessionId);
        msg.setPatientId(patientId);
        msg.setRole(role);
        msg.setMessage(message);
        msg.setTurnCount(turnCount);
        conversationRepository.save(msg);
    }

    private boolean isPromptInjection(String input) {
        String lower = input.toLowerCase();
        return lower.contains("ignore previous") ||
                lower.contains("ignore all") ||
                lower.contains("forget your") ||
                lower.contains("forget previous") ||
                lower.contains("you are now") ||
                lower.contains("new system prompt") ||
                lower.contains("disregard") ||
                lower.contains("override instructions") ||
                lower.contains("act as");
    }

    private triageResponse toResponse(triageRecord record) {
        triageResponse response = new triageResponse();
        response.setTriageId(record.getId());
        response.setPatientId(record.getPatientId());
        response.setSymptoms(record.getSymptoms());
        response.setSuggestedDepartment(record.getSuggestedDepartment());
        response.setUrgencyLevel(record.getUrgencyLevel());
        response.setReason(record.getReason());
        response.setStatus(record.getStatus());
        return response;
    }
}