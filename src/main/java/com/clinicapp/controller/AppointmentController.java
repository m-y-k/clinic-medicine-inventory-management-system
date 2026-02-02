package com.clinicapp.controller;

import com.clinicapp.dto.request.AppointmentRequest;
import com.clinicapp.dto.response.AppointmentResponse;
import com.clinicapp.model.Appointment;
import com.clinicapp.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(service.createAppointment(request));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAll() {
        return ResponseEntity.ok(service.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> update(@PathVariable Long id,
                                                      @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(service.updateAppointment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

}

