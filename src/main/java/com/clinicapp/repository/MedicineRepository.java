package com.clinicapp.repository;

import com.clinicapp.model.Medicine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends MongoRepository<Medicine, String> {
    boolean existsByName(String name);
}
