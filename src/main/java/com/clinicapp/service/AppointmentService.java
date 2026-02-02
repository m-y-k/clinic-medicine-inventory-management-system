package com.clinicapp.service;

import com.clinicapp.dto.request.AppointmentRequest;
import com.clinicapp.dto.request.PrescriptionRequest;
import com.clinicapp.dto.response.AppointmentResponse;
import com.clinicapp.dto.response.PrescriptionResponse;
import com.clinicapp.model.*;
import com.clinicapp.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    @Autowired
    private AppointmentRepository repo;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public AppointmentResponse createAppointment(AppointmentRequest request) {

        Appointment appointment = new Appointment();

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean isDoctorOrAdmin =
                doctor.getRole().toString().equals("DOCTOR") ||
                        doctor.getRole().toString().equals("ADMIN");

        if (!isDoctorOrAdmin)
            throw new RuntimeException("The current user is not a Doctor.");

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setScheduledAt(request.getScheduledAt());
        appointment.setStatus(request.getStatus());
        appointment.setNotes(request.getNotes());

        List<Prescription> prescriptionList = new ArrayList<>();

        for (PrescriptionRequest pr : request.getPrescriptions()) {

            Medicine medicine = medicineRepository.findById(pr.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine not found"));

            Prescription p = new Prescription();
            p.setAppointment(appointment);
            p.setMedicine(medicine);
            p.setQuantity(pr.getQuantity());
            p.setDosage(pr.getDosage());

            prescriptionList.add(p);
        }

        appointment.setPrescriptions(prescriptionList);

        applyPrescriptionStockChange(appointment, false);

        Appointment saved = repo.save(appointment);

        // ✅ map to response DTO
        return mapToResponse(saved);
    }


    public List<AppointmentResponse> getAllAppointments() {
        return repo.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public AppointmentResponse getById(Long id) {

        Appointment a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        return mapToResponse(a);
    }


    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {

        Appointment appointment = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // restore old stock
        applyPrescriptionStockChange(appointment, true);

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setScheduledAt(request.getScheduledAt());
        appointment.setStatus(request.getStatus());
        appointment.setNotes(request.getNotes());

        // clear old prescriptions
        for (Prescription p : appointment.getPrescriptions()) {
            prescriptionRepository.deleteById(p.getId());
        }
        appointment.getPrescriptions().clear();

        List<Prescription> newList = new ArrayList<>();

        for (PrescriptionRequest pr : request.getPrescriptions()) {

            Medicine medicine = medicineRepository.findById(pr.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine not found"));

            Prescription p = new Prescription();
            p.setAppointment(appointment);
            p.setMedicine(medicine);
            p.setQuantity(pr.getQuantity());
            p.setDosage(pr.getDosage());

            newList.add(p);
        }

        appointment.setPrescriptions(newList);

        // deduct new stock
        applyPrescriptionStockChange(appointment, false);

        Appointment saved = repo.save(appointment);

        return mapToResponse(saved);
    }


    public void deleteAppointment(Long id) {

        Appointment a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        applyPrescriptionStockChange(a, true);

        repo.delete(a);
    }


    // ---------- STOCK LOGIC ----------
    private void applyPrescriptionStockChange(Appointment appointment, boolean restore) {

        if (appointment.getPrescriptions() == null) return;

        for (Prescription p : appointment.getPrescriptions()) {

            Medicine med = medicineRepository.findById(
                    p.getMedicine().getId()
            ).orElseThrow(() -> new RuntimeException("Medicine not found"));

            int newStock = restore
                    ? med.getStockQuantity() + p.getQuantity()
                    : med.getStockQuantity() - p.getQuantity();

            med.setStockQuantity(newStock);

            medicineRepository.save(med);
        }
    }

    private AppointmentResponse mapToResponse(Appointment a) {

        AppointmentResponse res = new AppointmentResponse();

        res.setId(a.getId());

        res.setPatientId(a.getPatient().getId());
        res.setPatientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName());

        res.setDoctorId(a.getDoctor().getId());
        res.setDoctorName(a.getDoctor().getUsername());

        res.setScheduledAt(a.getScheduledAt());
        res.setStatus(a.getStatus());
        res.setNotes(a.getNotes());

        List<PrescriptionResponse> presList = a.getPrescriptions().stream().map(p -> {

            PrescriptionResponse pr = new PrescriptionResponse();

            pr.setMedicineId(p.getMedicine().getId());
            pr.setMedicineName(p.getMedicine().getName());
            pr.setQuantity(p.getQuantity());
            pr.setDosage(p.getDosage());

            return pr;

        }).toList();

        res.setPrescriptions(presList);

        return res;
    }

}

