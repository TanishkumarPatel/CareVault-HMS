package com.example.appointmentservice.service;

import appointment.events.AppointmentEvent;
import com.example.appointmentservice.dto.appointmentRequest;
import com.example.appointmentservice.dto.appointmentResponse;
import com.example.appointmentservice.dto.slotResponse;
import com.example.appointmentservice.dto.triageResponse;
import com.example.appointmentservice.exception.Patientnotfoundexception;
import com.example.appointmentservice.mapper.appointmentMapper;
import com.example.appointmentservice.model.appointmentSlot;
import com.example.appointmentservice.model.appointment;
import com.example.appointmentservice.repository.appointmentRepository;
import com.example.appointmentservice.repository.slotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;


@Service
public class appointmentService {
    private static final Logger log = LoggerFactory.getLogger(appointmentService.class);
    private final appointmentRepository appointmentRepository;
    private final slotRepository slotRepository;
    private final KafkaTemplate<String,byte[]> kafkaTemplate;
    private final RestTemplate restTemplate;

    public appointmentService(appointmentRepository appointmentRepository, KafkaTemplate<String,byte[]> kafkaTemplate,RestTemplate restTemplate, slotRepository slotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
        this.slotRepository = slotRepository;
    }

    public List<slotResponse> getAvailableSlots(String department) {
        return slotRepository.findByDepartmentAndStatus(department, "AVAILABLE")
                .stream()
                .map(appointmentMapper::toSlotDTO)
                .toList();
    }

    private String getPatientEmail(String patientId) {
        try {
            String url = "http://patient-service/patients/" + patientId;
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("email") != null) {
                return response.get("email").toString();
            }
        } catch (Exception e) {
            log.warn("Could not fetch patient email: {}", e.getMessage());
        }
        return null;
    }

    private int urgencyOrder(String urgencyLevel) {
        if (urgencyLevel == null) return 99;
        return switch (urgencyLevel.toUpperCase()) {
            case "HIGH"   -> 1;
            case "MEDIUM" -> 2;
            case "LOW"    -> 3;
            default       -> 99;
        };
    }

    public appointmentResponse bookAppointment(appointmentRequest request) {
        String url = "http://patient-service/patients/" + request.getPatientId();
        try {
            restTemplate.getForEntity(url, Object.class);
        } catch (Exception e) {
            throw new Patientnotfoundexception("Patient ID " + request.getPatientId() + " not found.");
        }

        appointment appointment = new appointment();
        appointment.setPatientId(UUID.fromString(request.getPatientId()));

        if (request.getTriageId() != null && !request.getTriageId().isEmpty()) {
            appointmentSlot slot = slotRepository.findById(UUID.fromString(request.getSlotId()))
                    .orElseThrow(() -> new RuntimeException("Slot not found: " + request.getSlotId()));

            if (!"AVAILABLE".equals(slot.getStatus())) {
                throw new RuntimeException("This slot is no longer available. Please select another.");
            }

            slot.setStatus("BOOKED");
            slotRepository.save(slot);

            appointment.setDoctorName(slot.getDoctorName());
            appointment.setAppointmentTime(slot.getSlotTime());
            appointment.setDepartment(slot.getDepartment());
            appointment.setSlotId(slot.getId());
            appointment.setStatus("PENDING_REVIEW");
            appointment.setTriageId(request.getTriageId());

            if (request.getTriageId() != null && !request.getTriageId().isEmpty()) {
                try {
                    String triageUrl = "http://ai-service/ai/triage/" + request.getTriageId();
                    triageResponse triage = restTemplate.getForObject(triageUrl, triageResponse.class);
                    if (triage != null) {
                        appointment.setSymptoms(triage.getSymptoms());
                        appointment.setReason(triage.getReason());
                        appointment.setUrgencyLevel(triage.getUrgencyLevel());
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch triage details: {}", e.getMessage());
                }
            }
            if (request.getTriageId() != null && !request.getTriageId().isEmpty()) {
                try {
                    String updateUrl = "http://ai-service/ai/triage/" + request.getTriageId() + "/status";
                    restTemplate.put(updateUrl, Map.of("status", "BOOKED"));
                } catch (Exception e) {
                    log.warn("Could not update triage status: {}", e.getMessage());
                }
            }
            log.info("AI triage booking saved as PENDING_REVIEW, slot: {}", slot.getId());

        } else {
            if (request.getDoctorName() == null || request.getDoctorName().isEmpty()) {
                throw new IllegalArgumentException("Doctor name is required for direct booking.");
            }
            if (request.getAppointmentTime() == null || request.getAppointmentTime().isEmpty()) {
                throw new IllegalArgumentException("Appointment time is required for direct booking.");
            }

            appointment.setDoctorName(request.getDoctorName());
            appointment.setAppointmentTime(LocalDateTime.parse(request.getAppointmentTime()));
            appointment.setDepartment(request.getDepartment());
            appointment.setSlotId(UUID.fromString(request.getSlotId()));
            appointment.setStatus("APPROVED");

            // Save first so ID is generated, then send Kafka
            appointment saved = appointmentRepository.save(appointment);
            sendKafkaEvent(saved);
            log.info("Direct booking APPROVED, notification sent: {}", saved.getId());
            return appointmentMapper.toDTO(saved);
        }

        appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toDTO(saved);
    }

    public List<appointmentResponse> getPendingAppointments() {
        return appointmentRepository.findByStatus("PENDING_REVIEW")
                .stream()
                .sorted(Comparator.comparingInt(a -> urgencyOrder(a.getUrgencyLevel())))
                .map(appointmentMapper::toDTO)
                .toList();
    }

    public appointmentResponse markUrgentCallDone(UUID appointmentId) {
        appointment apt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));
        apt.setUrgentCallDone(true);
        appointment saved = appointmentRepository.save(apt);
        log.info("Urgent call marked done for appointment: {}", appointmentId);
        return appointmentMapper.toDTO(saved);
    }

    public appointmentResponse approveAppointment(UUID appointmentId) {
        appointment apt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        apt.setStatus("APPROVED");
        appointment saved = appointmentRepository.save(apt);

        sendKafkaEvent(saved);
        log.info("Appointment APPROVED, notification sent: {}", appointmentId);

        return appointmentMapper.toDTO(saved);
    }

    public appointmentResponse rejectAppointment(UUID appointmentId, Map<String, String> body) {
        appointment apt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        String rejectionReason = body.get("rejectionReason");
        String recommendedDepartment = body.get("recommendedDepartment");
        String recommendedDoctor = body.get("recommendedDoctor");

        apt.setStatus("REJECTED");
        apt.setRejectionReason(rejectionReason);
        apt.setRecommendedDepartment(recommendedDepartment);
        apt.setRecommendedDoctor(recommendedDoctor);

        // Free up the slot so others can book it
        if (apt.getSlotId() != null) {
            slotRepository.findById(apt.getSlotId()).ifPresent(slot -> {
                slot.setStatus("AVAILABLE");
                slotRepository.save(slot);
            });
        }

        appointment saved = appointmentRepository.save(apt);
        sendKafkaRejectionEvent(saved);
        log.info("Appointment REJECTED: {}", appointmentId);

        return appointmentMapper.toDTO(saved);
    }

    private void sendKafkaEvent(appointment appointment) {
        String patientEmail = getPatientEmail(appointment.getPatientId().toString());
        AppointmentEvent event = AppointmentEvent.newBuilder()
                .setAppointmentId(appointment.getId().toString())
                .setPatientId(appointment.getPatientId().toString())
                .setDoctorName(appointment.getDoctorName())
                .setAppointmentTime(appointment.getAppointmentTime().toString())
                .setPatientEmail(patientEmail != null ? patientEmail : "")
                .setDepartment(appointment.getDepartment() != null ? appointment.getDepartment() : "")
                .setEventType("APPROVED")
                .build();
        try {
            kafkaTemplate.send("appointment", event.toByteArray());
            log.info(" Kafka event sent for appointment: {}", appointment.getId());
        } catch (Exception ex) {
            log.error(" Error sending Kafka event: {}", ex.getMessage());
        }
    }

    private void sendKafkaRejectionEvent(appointment appointment) {
        String patientEmail = getPatientEmail(appointment.getPatientId().toString());
        AppointmentEvent event = AppointmentEvent.newBuilder()
                .setAppointmentId(appointment.getId().toString())
                .setPatientId(appointment.getPatientId().toString())
                .setDoctorName(appointment.getRecommendedDoctor() != null ? appointment.getRecommendedDoctor() : "")
                .setAppointmentTime(appointment.getAppointmentTime().toString())
                .setPatientEmail(patientEmail != null ? patientEmail : "")
                .setDepartment(appointment.getRecommendedDepartment() != null ? appointment.getRecommendedDepartment() : "")
                .setEventType("REJECTED")
                .build();
        try {
            kafkaTemplate.send("appointment", event.toByteArray());
            log.info("Kafka rejection event sent: {}", appointment.getId());
        } catch (Exception ex) {
            log.error("Error sending rejection Kafka event: {}", ex.getMessage());
        }
    }
}
