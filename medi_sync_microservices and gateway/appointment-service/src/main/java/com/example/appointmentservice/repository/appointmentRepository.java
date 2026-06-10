package com.example.appointmentservice.repository;

import com.example.appointmentservice.model.appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface appointmentRepository extends JpaRepository<appointment, UUID> {
    List<appointment> findByStatus(String status);
}
