package com.clinicapp.service;

import com.clinicapp.model.Medicine;
import com.clinicapp.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    // Create or add medicine
    public Medicine addMedicine(Medicine medicine) {
        medicine.setCreatedAt(new Date());
        medicine.setUpdatedAt(new Date());
        return medicineRepository.save(medicine);
    }

    // List all medicines
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    // Get medicine by ID
    public Optional<Medicine> getMedicineById(String id) {
        return medicineRepository.findById(id);
    }

    // Update medicine
    public Medicine updateMedicine(String id, Medicine updatedMedicine) {
        return medicineRepository.findById(id).map(medicine -> {
            medicine.setName(updatedMedicine.getName());
            medicine.setUnit(updatedMedicine.getUnit());
            medicine.setAltUnit(updatedMedicine.getAltUnit());
            medicine.setPrice(updatedMedicine.getPrice());
            medicine.setStockQuantity(updatedMedicine.getStockQuantity());
            medicine.setUpdatedAt(new Date());
            return medicineRepository.save(medicine);
        }).orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
    }

    // Delete medicine
    public boolean deleteMedicine(String id) {
        if (medicineRepository.existsById(id)) {
            medicineRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
