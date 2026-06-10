package com.example.appointmentservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_slots")
public class appointmentSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String doctorName;

    private String department;

    private LocalDateTime slotTime;

    private String status;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = "AVAILABLE";
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(LocalDateTime slotTime) {
        this.slotTime = slotTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
