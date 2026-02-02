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

    public Medicine addMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    public Medicine updateMedicine(Long id, Medicine updated) {

        return medicineRepository.findById(id).map(medicine -> {

            medicine.setName(updated.getName());
            medicine.setUnit(updated.getUnit());
            medicine.setAltUnit(updated.getAltUnit());
            medicine.setPrice(updated.getPrice());
            medicine.setStockQuantity(updated.getStockQuantity());

            return medicineRepository.save(medicine);

        }).orElseThrow(() -> new RuntimeException("Medicine not found"));
    }

    public boolean deleteMedicine(Long id) {

        if (medicineRepository.existsById(id)) {
            medicineRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
