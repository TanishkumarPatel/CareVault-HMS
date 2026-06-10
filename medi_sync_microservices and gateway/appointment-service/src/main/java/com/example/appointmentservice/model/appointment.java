package com.example.appointmentservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private UUID patientId;
    private String doctorName;
    private LocalDateTime appointmentTime;
    private String Department;
    private UUID slotId;
    private String status;
    private String triageId;

    @Column(length = 2000)
    private String symptoms;

    @Column(length = 2000)
    private String reason;

    private String urgencyLevel;

    private String rejectionReason;
    private String recommendedDepartment;
    private String recommendedDoctor;

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRecommendedDepartment() {
        return recommendedDepartment;
    }

    public void setRecommendedDepartment(String recommendedDepartment) {
        this.recommendedDepartment = recommendedDepartment;
    }

    public String getRecommendedDoctor() {
        return recommendedDoctor;
    }

    public void setRecommendedDoctor(String recommendedDoctor) {
        this.recommendedDoctor = recommendedDoctor;
    }

    public String getTriageId() {
        return triageId;
    }

    public void setTriageId(String triageId) {
        this.triageId = triageId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = "PENDING_REVIEW";
        }
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String department) {
        Department = department;
    }

    public UUID getSlotId() {
        return slotId;
    }

    public void setSlotId(UUID slotId) {
        this.slotId = slotId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }
}
