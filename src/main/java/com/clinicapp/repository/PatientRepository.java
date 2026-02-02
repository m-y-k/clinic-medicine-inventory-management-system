package com.clinicapp.repository;

import com.clinicapp.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByFirstNameContainingIgnoreCase(String firstName);

    List<Patient> findByLastNameContainingIgnoreCase(String lastName);

    Optional<Patient> findByPhone(String phone);

    Optional<Patient> findByEmail(String email);
}

