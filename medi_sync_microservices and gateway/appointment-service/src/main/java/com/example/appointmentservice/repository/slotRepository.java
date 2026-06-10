package com.example.appointmentservice.repository;

import com.example.appointmentservice.model.appointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface slotRepository extends JpaRepository<appointmentSlot, UUID> {
    List<appointmentSlot> findByDepartmentAndStatus(String department, String status);
}
