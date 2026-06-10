package com.example.aiservice.controller;

import com.example.aiservice.dto.triageRequest;
import com.example.aiservice.dto.triageResponse;
import com.example.aiservice.service.triageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ai/triage")
public class triageController {
    private final triageService service ;

    public triageController(triageService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<triageResponse> analyze(@Valid @RequestBody triageRequest request) {
        return ResponseEntity.ok(service.analyze(request));
    }

    @PutMapping("/{triageId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID triageId,
            @RequestBody Map<String, String> body) {
        service.updateStatus(triageId, body.get("status"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{triageId}")
    public ResponseEntity<triageResponse> getById(@PathVariable UUID triageId) {
        return ResponseEntity.ok(service.getById(triageId));
    }
}
