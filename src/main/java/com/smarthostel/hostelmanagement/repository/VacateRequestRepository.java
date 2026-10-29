
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.VacateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacateRequestRepository
        extends JpaRepository<VacateRequest, Long> {

    // Check whether a student has submitted any vacate request
    boolean existsByStudent_Id(Long studentId);

    // Retrieve a student's vacate request history
    List<VacateRequest> findByStudent_IdOrderByRequestedAtDesc(
            Long studentId
    );

    // Check whether the student has a pending request
    boolean existsByStudent_IdAndStatus(
            Long studentId,
            VacateRequest.RequestStatus status
    );
}
