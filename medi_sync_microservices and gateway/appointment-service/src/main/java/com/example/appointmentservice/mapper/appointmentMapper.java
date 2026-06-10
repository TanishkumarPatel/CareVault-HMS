package com.example.appointmentservice.mapper;

import com.example.appointmentservice.dto.appointmentRequest;
import com.example.appointmentservice.dto.appointmentResponse;
import com.example.appointmentservice.dto.slotResponse;
import com.example.appointmentservice.model.appointment;
import com.example.appointmentservice.model.appointmentSlot;

import java.time.LocalDateTime;
import java.util.UUID;

public class appointmentMapper {
    public static appointmentResponse toDTO(appointment appointment) {
        appointmentResponse response = new appointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatientId().toString());
        response.setDoctorName(appointment.getDoctorName());
        response.setAppointmentTime(appointment.getAppointmentTime().toString());
        response.setDepartment(appointment.getDepartment());
        response.setStatus(appointment.getStatus());
        response.setTriageId(appointment.getTriageId());
        response.setSymptoms(appointment.getSymptoms());
        response.setReason(appointment.getReason());
        response.setUrgencyLevel(appointment.getUrgencyLevel());
        response.setRejectionReason(appointment.getRejectionReason());
        response.setRecommendedDepartment(appointment.getRecommendedDepartment());
        response.setRecommendedDoctor(appointment.getRecommendedDoctor());
        return response;
    }

    public static appointment toModel(appointmentRequest request) {
        appointment appointment = new appointment();
        appointment.setPatientId(UUID.fromString(request.getPatientId()));
        appointment.setDoctorName(request.getDoctorName());
        appointment.setAppointmentTime(LocalDateTime.parse(request.getAppointmentTime()));
        return appointment;
    }

    public static slotResponse toSlotDTO(appointmentSlot slot) {
        slotResponse response = new slotResponse();
        response.setSlotId(slot.getId());
        response.setDoctorName(slot.getDoctorName());
        response.setDepartment(slot.getDepartment());
        response.setSlotTime(slot.getSlotTime().toString());
        response.setStatus(slot.getStatus());
        return response;
    }
}
