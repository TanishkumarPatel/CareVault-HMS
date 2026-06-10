package com.example.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class geminiService {
    private static final Logger log = LoggerFactory.getLogger(geminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> analyzeSymptoms(String symptoms) {

        String prompt = """
                You are a hospital triage assistant.
                A patient described their symptoms as: "%s"
                
                Based on these symptoms identify:
                1. The most appropriate hospital department from this list only:
                   Cardiology, Orthopedics, Neurology, General Medicine, ENT,
                   Dermatology, Gastroenterology, Psychiatry, Gynecology,
                   Pediatrics, Ophthalmology, Urology
                2. Urgency level: LOW, MEDIUM, or HIGH
                3. A brief reason (1-2 sentences only)
                
                Reply ONLY with valid JSON in exactly this format, no other text:
                {
                  "department": "Department Name",
                  "urgency": "HIGH",
                  "reason": "Brief reason here"
                }
                
                If the input is not a medical symptom description, reply:
                {
                  "department": "INVALID",
                  "urgency": "NONE",
                  "reason": "Not a valid symptom description"
                }
                """.formatted(symptoms);

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String urlWithKey = apiUrl + "?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String aiText = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

            aiText = aiText.replace("```json", "").replace("```", "").trim();

            JsonNode aiJson = objectMapper.readTree(aiText);

            Map<String, String> result = new HashMap<>();
            result.put("department", aiJson.path("department").asText("General Medicine"));
            result.put("urgency", aiJson.path("urgency").asText("MEDIUM"));
            result.put("reason", aiJson.path("reason").asText("Please consult a doctor."));

            log.info("Gemini result: dept={}, urgency={}", result.get("department"), result.get("urgency"));
            return result;

        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage());
            Map<String, String> fallback = new HashMap<>();
            fallback.put("department", "General Medicine");
            fallback.put("urgency", "MEDIUM");
            fallback.put("reason", "AI analysis unavailable. Defaulting to General Medicine.");
            return fallback;
        }
    }
}
