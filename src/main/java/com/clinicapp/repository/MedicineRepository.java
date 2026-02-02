package com.clinicapp.repository;

import com.clinicapp.model.Medicine;
//import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    boolean existsByName(String name);

    List<Medicine> findByNameContainingIgnoreCase(String name);

    List<Medicine> findByStockQuantityLessThan(int quantity);
}

