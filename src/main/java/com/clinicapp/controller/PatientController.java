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
        Patient saved = (Patient) patientService.createPatient(patient).getBody();
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatient(@PathVariable String id) {
        Patient patient = patientService.getPatientById(id).get();
        if (patient != null)
            return ResponseEntity.ok(patient);
        else
            return ResponseEntity.status(404).body("Patient not found");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable String id, @RequestBody Patient patient) {
        Patient updated = patientService.updatePatient(id, patient);
        if (updated != null)
            return ResponseEntity.ok(updated);
        else
            return ResponseEntity.status(404).body("Patient not found");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable String id) {
        if (patientService.deletePatient(id))
            return ResponseEntity.ok("Deleted successfully");
        else
            return ResponseEntity.status(404).body("Patient not found");
    }
}
