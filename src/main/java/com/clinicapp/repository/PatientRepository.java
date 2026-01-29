package com.clinicapp.repository;

import com.clinicapp.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends MongoRepository<Patient, String> {
    List<Patient> findByFirstName(String firstName);
    List<Patient> findByLastName(String lastName);
    Optional<Patient> findByPhone (String phone);
}
