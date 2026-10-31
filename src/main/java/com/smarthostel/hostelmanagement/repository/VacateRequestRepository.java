
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.VacateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacateRequestRepository
        extends JpaRepository<VacateRequest, Long> {

    boolean existsByStudent_Id(Long studentId);

    List<VacateRequest> findByStudent_IdOrderByRequestedAtDesc(
            Long studentId
    );

    boolean existsByStudent_IdAndStatus(
            Long studentId,
            VacateRequest.RequestStatus status
    );
}
