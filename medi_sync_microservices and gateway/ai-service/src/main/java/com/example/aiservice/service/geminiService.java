package com.example.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class geminiService {
    private static final Logger log = LoggerFactory.getLogger(geminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // department details for system prompt
    private static final String DEPARTMENT_CONTEXT = """
            The hospital has the following departments:
            
            - Cardiology: chest pain, palpitations, shortness of breath, high blood pressure, irregular heartbeat
              HIGH urgency indicators: crushing chest pain, pain radiating to left arm/jaw, loss of consciousness
            
            - Orthopedics: joint pain, bone fractures, back pain, muscle injuries, knee/shoulder/hip problems
              HIGH urgency indicators: suspected fracture, severe trauma, inability to move a limb
            
            - Neurology: severe headaches, dizziness, seizures, memory loss, numbness, tingling, vision changes
              HIGH urgency indicators: sudden severe headache, facial drooping, one-sided weakness (stroke signs)
            
            - General Medicine: fever, fatigue, cold, flu, general body ache, weight loss, diabetes follow-up
              HIGH urgency indicators: very high fever (>104F), severe dehydration, difficulty breathing
            
            - ENT: ear pain, hearing loss, sore throat, nasal congestion, sinusitis, voice changes
              HIGH urgency indicators: severe throat swelling, complete hearing loss, breathing obstruction
            
            - Dermatology: skin rashes, acne, eczema, fungal infections, hair loss, skin allergies
              HIGH urgency indicators: rapidly spreading rash, severe allergic reaction, infected wounds
            
            - Gastroenterology: stomach pain, nausea, vomiting, diarrhea, constipation, acid reflux, bloating
              HIGH urgency indicators: severe abdominal pain, blood in stool/vomit, signs of appendicitis
            
            - Psychiatry: anxiety, depression, mood swings, sleep disorders, panic attacks, behavioral issues
              HIGH urgency indicators: suicidal thoughts, severe psychosis, self-harm
            
            - Gynecology: menstrual issues, pregnancy concerns, pelvic pain, vaginal discharge, fertility issues
              HIGH urgency indicators: heavy bleeding, severe pelvic pain, pregnancy complications
            
            - Pediatrics: children's illnesses, growth concerns, vaccinations, childhood infections
              HIGH urgency indicators: high fever in infants, difficulty breathing in children, seizures in children
            
            - Ophthalmology: eye pain, vision problems, redness, discharge, eye injuries
              HIGH urgency indicators: sudden vision loss, severe eye injury, chemical exposure to eyes
            
            - Urology: urinary problems, kidney stones, frequent urination, blood in urine, prostate issues
              HIGH urgency indicators: complete inability to urinate, severe kidney pain, blood in urine with pain
            """;

    public boolean isMedicalInput(String message) {
        String prompt = """
                Is the following input a valid medical symptom description?
                Reply with only YES or NO, nothing else.
                
                Input: "%s"
                """.formatted(message);

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
            String answer = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim().toUpperCase();
            return answer.startsWith("YES");
        } catch (Exception e) {
            log.error("Validation call error: {}", e.getMessage());
            return true; // if validation fails, allow through (fail open)
        }
    }


    /**
     * Sends full conversation history to Gemini.
     * conversationHistory: list of maps with "role" (user/model) and "message"
     * isForceFinal: if true, forces Gemini to give final JSON answer (turn limit reached)
     *
     * Returns either:
     *   - a follow-up question string (if AI needs more info)
     *   - a Map with department/urgency/reason (if AI is confident or forced)
     */
    public Object chat(List<Map<String, String>> conversationHistory, boolean isForceFinal) {

        String systemPrompt = """
                You are a medical triage assistant for hospital.
                
                %s
                
                Your task:
                - Ask ONE focused follow-up question at a time to gather more symptom details
                - When you are confident about the patient's condition, return ONLY this JSON:
                  {"department": "", "urgency": "HIGH/MEDIUM/LOW", "reason": ""}
                - If the input is not medical, return:
                  {"department": "INVALID", "urgency": "NONE", "reason": "Not a valid symptom description"}
                - Never return JSON until you are confident
                - Ask follow-up questions as plain text (no JSON)
                %s
                """.formatted(
                DEPARTMENT_CONTEXT,
                isForceFinal ? "\n- You MUST return the final JSON result now. Do not ask any more questions." : ""
        );

        // Build contents array for Gemini (system prompt as first user message)
        List<Map<String, Object>> contents = new ArrayList<>();

        // Add system context as first turn
        Map<String, Object> systemContent = new HashMap<>();
        systemContent.put("role", "user");
        systemContent.put("parts", List.of(Map.of("text", systemPrompt)));
        contents.add(systemContent);

        // Add model acknowledgment
        Map<String, Object> ackContent = new HashMap<>();
        ackContent.put("role", "model");
        ackContent.put("parts", List.of(Map.of("text", "Understood. I am ready to assist with medical triage.")));
        contents.add(ackContent);

        // Add actual conversation history
        for (Map<String, String> msg : conversationHistory) {
            Map<String, Object> msgContent = new HashMap<>();
            msgContent.put("role", msg.get("role")); // "user" or "model"
            msgContent.put("parts", List.of(Map.of("text", msg.get("message"))));
            contents.add(msgContent);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String urlWithKey = apiUrl + "?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String aiText = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            // Try to parse as JSON (final answer)
            String cleanText = aiText.replace("```json", "").replace("```", "").trim();
            try {
                JsonNode aiJson = objectMapper.readTree(cleanText);
                if (aiJson.has("department")) {
                    Map<String, String> result = new HashMap<>();
                    result.put("department", aiJson.path("department").asText("General Medicine"));
                    result.put("urgency", aiJson.path("urgency").asText("MEDIUM"));
                    result.put("reason", aiJson.path("reason").asText("Please consult a doctor."));
                    log.info("Chat final result: dept={}, urgency={}", result.get("department"), result.get("urgency"));
                    return result;
                }
            } catch (Exception ignored) {
                // not JSON — it's a follow-up question
            }

            // It's a follow-up question
            log.info("Chat follow-up question: {}", aiText);
            return aiText;

        } catch (Exception e) {
            log.error("Gemini chat error: {}", e.getMessage());
            throw new RuntimeException(
                    "AI triage service is temporarily unavailable. Please try again later."
            );
        }
    }
}