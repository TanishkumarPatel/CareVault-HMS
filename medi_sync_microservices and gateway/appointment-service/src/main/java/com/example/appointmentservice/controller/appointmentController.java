package com.example.appointmentservice.controller;

import com.example.appointmentservice.dto.appointmentRequest;
import com.example.appointmentservice.dto.appointmentResponse;
import com.example.appointmentservice.dto.slotResponse;
import com.example.appointmentservice.service.appointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
public class appointmentController {
    private final appointmentService service;
    public appointmentController(appointmentService service) {
        this.service = service;
    }

    @GetMapping("/slots")
    public ResponseEntity<List<slotResponse>> getAvailableSlots(@RequestParam String department) {
        return ResponseEntity.ok(service.getAvailableSlots(department));
    }

    @PostMapping
    public ResponseEntity<appointmentResponse> createAppointment(@Valid @RequestBody appointmentRequest request) {
        appointmentResponse response = service.bookAppointment(request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<appointmentResponse>> getPendingAppointments() {
        return ResponseEntity.ok(service.getPendingAppointments());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<appointmentResponse> approveAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(service.approveAppointment(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<appointmentResponse> rejectAppointment(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.rejectAppointment(id, body));
    }
}
