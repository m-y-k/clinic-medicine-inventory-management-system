package com.clinicapp.controller;

import com.clinicapp.model.Patient;
import com.clinicapp.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatient(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        Patient updated = patientService.updatePatient(id, patient);

        if (updated != null)
            return ResponseEntity.ok(updated);

        return ResponseEntity.status(404).body("Patient not found");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        if (patientService.deletePatient(id))
            return ResponseEntity.ok("Deleted successfully");

        return ResponseEntity.status(404).body("Patient not found");
    }
}
