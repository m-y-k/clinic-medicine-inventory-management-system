package com.clinicapp.service;

import com.clinicapp.model.Appointment;
import com.clinicapp.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository repo;

    public Appointment createAppointment(Appointment appointment) {
        appointment.setCreatedAt(new Date());
        appointment.setUpdatedAt(new Date());
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
            a.setPatientId(updated.getPatientId());
            a.setDoctorId(updated.getDoctorId());
            a.setScheduledAt(updated.getScheduledAt());
            a.setStatus(updated.getStatus());
            a.setNotes(updated.getNotes());
            a.setImages(updated.getImages());
            a.setUpdatedAt(new Date());
            return repo.save(a);
        }).orElse(null);
    }

    public boolean deleteAppointment(String id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
