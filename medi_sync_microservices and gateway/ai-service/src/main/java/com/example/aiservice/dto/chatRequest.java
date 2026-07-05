package com.example.aiservice.dto;

import jakarta.validation.constraints.NotBlank;

public class chatRequest {
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotBlank(message = "Patient ID is required")
    private String patientId;

    @NotBlank(message = "Message is required")
    private String message;

    public @NotBlank(message = "Session ID is required") String getSessionId() {
        return sessionId;
    }

    public void setSessionId(@NotBlank(message = "Session ID is required") String sessionId) {
        this.sessionId = sessionId;
    }

    public @NotBlank(message = "Patient ID is required") String getPatientId() {
        return patientId;
    }

    public void setPatientId(@NotBlank(message = "Patient ID is required") String patientId) {
        this.patientId = patientId;
    }

    public @NotBlank(message = "Message is required") String getMessage() {
        return message;
    }

    public void setMessage(@NotBlank(message = "Message is required") String message) {
        this.message = message;
    }
}
