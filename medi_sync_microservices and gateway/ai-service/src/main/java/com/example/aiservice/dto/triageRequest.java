package com.example.aiservice.dto;


import jakarta.validation.constraints.NotBlank;

public class triageRequest {
    @NotBlank(message = "Patient ID is required")
    private String patientId;

    @NotBlank(message = "Please describe your symptoms")
    private String symptoms;

    public String getPatientId() {
        return patientId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSymptoms() {
        return symptoms;
    }
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
}

