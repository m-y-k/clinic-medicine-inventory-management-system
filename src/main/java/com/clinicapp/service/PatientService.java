package com.clinicapp.service;

import com.clinicapp.model.Patient;
import com.clinicapp.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public ResponseEntity<?> createPatient(Patient patient) {

        if (patientRepository.findByPhone(patient.getPhone()).isPresent()) {
            return new ResponseEntity<>("Phone already exists", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(patientRepository.save(patient));
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Patient updatePatient(Long id, Patient updated) {

        return patientRepository.findById(id).map(patient -> {

            patient.setFirstName(updated.getFirstName());
            patient.setLastName(updated.getLastName());
            patient.setDob(updated.getDob());
            patient.setGender(updated.getGender());
            patient.setPhone(updated.getPhone());
            patient.setEmail(updated.getEmail());
            patient.setAddress(updated.getAddress());

            return patientRepository.save(patient);

        }).orElse(null);
    }

    public boolean deletePatient(Long id) {

        if (patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
