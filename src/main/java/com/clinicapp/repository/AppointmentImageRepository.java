package com.clinicapp.repository;

import com.clinicapp.model.AppointmentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentImageRepository extends JpaRepository<AppointmentImage, Long> {

    List<AppointmentImage> findByAppointmentId(Long appointmentId);
}

