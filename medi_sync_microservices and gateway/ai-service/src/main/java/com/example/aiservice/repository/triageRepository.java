package com.example.aiservice.repository;

import com.example.aiservice.model.triageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface triageRepository extends JpaRepository<triageRecord, UUID> {

    List<triageRecord> findByStatus(String status);

    List<triageRecord> findByPatientId(String patientId);
}

