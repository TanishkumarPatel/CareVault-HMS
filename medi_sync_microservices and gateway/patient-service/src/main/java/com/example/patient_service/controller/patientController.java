package com.example.patient_service.controller;

import com.example.patient_service.dto.PatientRequestDTO;
import com.example.patient_service.dto.PatientResponseDTO;
import com.example.patient_service.dto.validators.CreatePatientValidationGroup;
import com.example.patient_service.model.patient;
import com.example.patient_service.repository.patient_repository;
import com.example.patient_service.service.patient_service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")

@Tag(name="patient", description="API for managing patients")//for swagger UI
public class patientController {
    private final patient_service patient_service;
    private final patient_repository patient_repository;

    public patientController(patient_service patient_service, patient_repository patient_repository) {
        this.patient_service = patient_service;
        this.patient_repository = patient_repository;
    }

    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDTO>> getPatients(){
        List<PatientResponseDTO> patients= patient_service.getpatients();
        return ResponseEntity.ok().body(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable String id) {
        PatientResponseDTO patient = patient_service.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @PostMapping
    @Operation(summary = "Create a new patient")
    public ResponseEntity<PatientResponseDTO> createpatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO){

        PatientResponseDTO patientResponseDTO=patient_service.createpatient(patientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a new patient")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id,@Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO){
    PatientResponseDTO patientResponseDTO =patient_service.updatepatient(id, patientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a  patient")
    public ResponseEntity<PatientResponseDTO> deletepatient(@PathVariable UUID id){
        patient_service.deletepatient(id);
        return ResponseEntity.noContent().build();
    }
}
