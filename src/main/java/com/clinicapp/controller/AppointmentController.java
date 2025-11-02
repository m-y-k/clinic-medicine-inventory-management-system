package com.clinicapp.controller;

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
    public ResponseEntity<Appointment> create(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(service.createAppointment(appointment));
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAll() {
        return ResponseEntity.ok(service.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Appointment appointment = service.getById(id).get();
        if (appointment != null)
            return ResponseEntity.ok(appointment);
        else
            return ResponseEntity.status(404).body("Appointment not found");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Appointment appointment) {
        Appointment updated = service.updateAppointment(id, appointment);
        if (updated != null)
            return ResponseEntity.ok(updated);
        else
            return ResponseEntity.status(404).body("Appointment not found");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (service.deleteAppointment(id))
            return ResponseEntity.ok("Deleted successfully");
        else
            return ResponseEntity.status(404).body("Appointment not found");
    }
}
