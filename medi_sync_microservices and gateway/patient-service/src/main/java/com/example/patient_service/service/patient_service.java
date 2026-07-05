package com.example.patient_service.service;

import com.example.patient_service.dto.PatientRequestDTO;
import com.example.patient_service.dto.PatientResponseDTO;
import com.example.patient_service.exception.EmailAlreadyExistsException;
import com.example.patient_service.exception.Patientnotfoundexception;
import com.example.patient_service.kafka.kafkaProducer;
import com.example.patient_service.mapper.patientMapper;
import com.example.patient_service.model.patient;
import com.example.patient_service.repository.patient_repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class patient_service {
    private final kafkaProducer kafkaProducer;
    private patient_repository patient_repository;

    public patient_service(patient_repository patient_repository, kafkaProducer kafkaProducer) {
        this.patient_repository = patient_repository;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getpatients() {
        List<patient> patients = patient_repository.findAll();

//        ************IMP***********
//        patientMapper.toDTO(pat)
//        patientMapper::toDTO
//        both the above are same
        List<PatientResponseDTO> patientResponseDTOS = patients.stream().map(patientMapper::toDTO).toList();
        return patientResponseDTOS;
    }

    public PatientResponseDTO createpatient(PatientRequestDTO patientRequestDTO) {
        if (patient_repository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email " + "already exists"
                            + patientRequestDTO.getEmail());
    }

        patient newpatient = patient_repository.save(patientMapper.toModel(patientRequestDTO));

        kafkaProducer.sendEvent(newpatient);
        return patientMapper.toDTO(newpatient);
    }

    public PatientResponseDTO getPatientById(String id) {
        patient patient = patient_repository.findById(UUID.fromString(id))
                .orElseThrow(() -> new Patientnotfoundexception("Patient not found with id: "+id));

        return patientMapper.toDTO(patient);
    }

    public PatientResponseDTO updatepatient(UUID id, PatientRequestDTO patientRequestDTO) {
        patient patient = patient_repository.findById(id).orElseThrow(() -> new Patientnotfoundexception("patient not found with id: " + id));

        if (patient_repository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)) {
            throw new EmailAlreadyExistsException("A patient with this email already exists" + patientRequestDTO.getEmail());
        }
        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        patient updatedpatient = patient_repository.save(patient);
        return patientMapper.toDTO(updatedpatient);
    }

    public void deletepatient(UUID id) {
        patient_repository.deleteById(id);
    }
}
