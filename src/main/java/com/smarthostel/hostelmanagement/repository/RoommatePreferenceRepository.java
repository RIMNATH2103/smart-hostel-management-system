
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoommatePreferenceRepository
        extends JpaRepository<RoommatePreference, Long> {

    boolean existsByStudent_Id(Long studentId);

    boolean existsByPreferredStudent_Id(Long studentId);

    List<RoommatePreference> findByStudent_Id(Long studentId);

    boolean existsByStudent_IdAndPreferredStudent_Id(
            Long studentId,
            Long preferredStudentId
    );

    long countByStudent_Id(Long studentId);
}
