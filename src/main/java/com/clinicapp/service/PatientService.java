package com.clinicapp.service;

import com.clinicapp.model.Patient;
import com.clinicapp.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    public ResponseEntity<?> createPatient(Patient patient) {
        Optional<Patient> optionalPatient = patientRepository.findByPhone(patient.getPhone());
        if (optionalPatient.isPresent()) {
            return new ResponseEntity<>( optionalPatient.get(), HttpStatus.BAD_REQUEST );
        }
        patient.setCreatedAt(new Date());
        patient.setUpdatedAt(new Date());
        return new ResponseEntity<>( patientRepository.save(patient), HttpStatusCode.valueOf(200) );
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(String id) {
        return patientRepository.findById(id);
    }

    public Patient updatePatient(String id, Patient updated) {
        return patientRepository.findById(id).map(patient -> {
            patient.setFirstName(updated.getFirstName());
            patient.setLastName(updated.getLastName());
            patient.setDob(updated.getDob());
            patient.setGender(updated.getGender());
            patient.setPhone(updated.getPhone());
            patient.setEmail(updated.getEmail());
            patient.setAddress(updated.getAddress());
            patient.setUpdatedAt(new Date());
            return patientRepository.save(patient);
        }).orElse(null);
    }

    public boolean deletePatient(String id) {
        if (patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
