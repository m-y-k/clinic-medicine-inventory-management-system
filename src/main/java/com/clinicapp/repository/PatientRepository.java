package com.clinicapp.repository;

import com.clinicapp.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository extends MongoRepository<Patient, String> {
    List<Patient> findByFirstNameContainingIgnoreCase(String firstName);
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    List<Patient> findByPhone(String phone);
}
