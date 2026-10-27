
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommatePreferenceRepository
        extends JpaRepository<RoommatePreference, Long> {

    boolean existsByStudent_Id(Long studentId);

    boolean existsByPreferredStudent_Id(Long studentId);
}
