
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoommatePreferenceRepository
        extends JpaRepository<RoommatePreference, Long> {

    // Check whether a student has selected any roommates
    boolean existsByStudent_Id(Long studentId);

    // Check whether a student has been selected by someone else
    boolean existsByPreferredStudent_Id(Long studentId);

    // Get all roommate preferences selected by a student
    List<RoommatePreference> findByStudent_Id(Long studentId);

    // Check whether a particular roommate is already selected
    boolean existsByStudent_IdAndPreferredStudent_Id(
            Long studentId,
            Long preferredStudentId
    );

    // Count how many roommates a student has selected
    long countByStudent_Id(Long studentId);
}
