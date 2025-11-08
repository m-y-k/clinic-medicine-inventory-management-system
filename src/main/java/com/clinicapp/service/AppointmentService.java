package com.clinicapp.service;

import com.clinicapp.model.Appointment;
import com.clinicapp.model.Medicine;
import com.clinicapp.model.Prescription;
import com.clinicapp.repository.AppointmentRepository;
import com.clinicapp.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository repo;
    @Autowired
    private MedicineRepository medicineRepository;

    public Appointment createAppointment(Appointment appointment) {
        appointment.setCreatedAt(new Date());
        appointment.setUpdatedAt(new Date());

        // Deduct stock for prescribed medicines
        applyPrescriptionStockChange(appointment, false);

        return repo.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return repo.findAll();
    }

    public Optional<Appointment> getById(String id) {
        return repo.findById(id);
    }

    public Appointment updateAppointment(String id, Appointment updated) {
        return repo.findById(id).map(a -> {
            // restore medicine stock
            applyPrescriptionStockChange(a, true);

            a.setPatientId(updated.getPatientId());
            a.setDoctorId(updated.getDoctorId());
            a.setScheduledAt(updated.getScheduledAt());
            a.setStatus(updated.getStatus());
            a.setNotes(updated.getNotes());
            a.setImages(updated.getImages());
            a.setPrescriptions(updated.getPrescriptions());
            a.setUpdatedAt(new Date());

            // deduct new stock
            applyPrescriptionStockChange(updated, false);

            return repo.save(a);
        }).orElse(null);
    }

    public boolean deleteAppointment(String id) {
        if (repo.existsById(id)) {
            // Restore stock before deleting
            Optional<Appointment> optionalAppointment = repo.findById(id);
            if (optionalAppointment.isPresent()) {
                applyPrescriptionStockChange(optionalAppointment.get(), true);
            }
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    // Stock handling logic
    private void applyPrescriptionStockChange(Appointment appointment, boolean restoreStock) {
        if (appointment.getPrescriptions() == null) return;

        for (Prescription p : appointment.getPrescriptions()) {
            Optional<Medicine> medOpt = medicineRepository.findById(p.getMedicineId());
            if (medOpt.isPresent()) {
                Medicine med = medOpt.get();

                int newStock = restoreStock
                        ? med.getStockQuantity() + p.getQuantity()  // restore
                        : med.getStockQuantity() - p.getQuantity();  // deduct

//                if (newStock < 0) newStock = 0; // avoid negative stock
                med.setStockQuantity(newStock);
                med.setUpdatedAt(new Date());
                medicineRepository.save(med);
            }
        }
    }
}
