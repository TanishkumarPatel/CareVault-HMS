package com.example.aiservice.service;

import com.example.aiservice.dto.triageRequest;
import com.example.aiservice.dto.triageResponse;
import com.example.aiservice.model.triageRecord;
import com.example.aiservice.repository.triageRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class triageService {
    private final geminiService geminiService;
    private final triageRepository triageRepository;

    public triageService(geminiService geminiService, triageRepository triageRepository) {
        this.geminiService = geminiService;
        this.triageRepository = triageRepository;
    }


    public triageResponse analyze(triageRequest request) {

        if (request.getSymptoms().trim().length() < 5) {
            throw new IllegalArgumentException("Please describe your symptoms in more detail.");
        }

        Map<String, String> aiResult = geminiService.analyzeSymptoms(request.getSymptoms());

        if ("INVALID".equals(aiResult.get("department"))) {
            throw new IllegalArgumentException("Please enter valid medical symptoms.");
        }

        triageRecord record = new triageRecord();
        record.setPatientId(request.getPatientId());
        record.setSymptoms(request.getSymptoms());
        record.setSuggestedDepartment(aiResult.get("department"));
        record.setUrgencyLevel(aiResult.get("urgency"));
        record.setReason(aiResult.get("reason"));

        triageRecord saved = triageRepository.save(record);
        return toResponse(saved);
    }

    public triageResponse getById(UUID id) {
        triageRecord record = triageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Triage record not found: " + id));
        return toResponse(record);
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

    public void updateStatus(UUID id, String status) {
        triageRecord record = triageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Triage record not found: " + id));
        record.setStatus(status);
        triageRepository.save(record);
    }
}
