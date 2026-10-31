
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.repository.RoommatePreferenceRepository;
import com.smarthostel.hostelmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smarthostel.hostelmanagement.service.RoomAllocationService;

import java.util.List;
import java.util.Optional;

@Service
public class RoommatePreferenceService {

    private final RoommatePreferenceRepository preferenceRepository;
    private final StudentRepository studentRepository;
    private final RoomAllocationService allocationService;


    public RoommatePreferenceService(
            RoommatePreferenceRepository preferenceRepository,
            StudentRepository studentRepository,
            RoomAllocationService allocationService) {

        this.preferenceRepository = preferenceRepository;
        this.studentRepository = studentRepository;
        this.allocationService = allocationService;
    }

    public List<RoommatePreference> getAllPreferences() {
        return preferenceRepository.findAll();
    }

    public Optional<RoommatePreference> getPreferenceById(Long id) {
        return preferenceRepository.findById(id);
    }

    public List<RoommatePreference> getPreferencesByStudent(Long studentId) {
        return preferenceRepository.findByStudent_Id(studentId);
    }

    @Transactional
    public RoommatePreference savePreference(
            RoommatePreference preference) {

        if (preference == null
                || preference.getStudent() == null
                || preference.getPreferredStudent() == null) {
            throw new IllegalArgumentException(
                    "Both students are required."
            );
        }

        Long studentId = preference.getStudent().getId();
        Long preferredStudentId =
                preference.getPreferredStudent().getId();

        if (studentId == null || preferredStudentId == null) {
            throw new IllegalArgumentException(
                    "Valid student IDs are required."
            );
        }

        if (allocationService.getCurrentAllocation(studentId).isPresent()) {
            throw new IllegalStateException(
                    "You cannot change roommate preferences while you have an allocated room."
            );
        }

        if (studentId.equals(preferredStudentId)) {
            throw new IllegalArgumentException(
                    "You cannot select yourself as a roommate."
            );
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found."
                ));

        Student preferredStudent =
                studentRepository.findById(preferredStudentId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Preferred roommate not found."
                        ));

        if (preferenceRepository
                .existsByStudent_IdAndPreferredStudent_Id(
                        studentId, preferredStudentId)) {

            throw new IllegalArgumentException(
                    "You have already selected this roommate."
            );
        }

        if (preferenceRepository.countByStudent_Id(studentId) >= 3) {
            throw new IllegalArgumentException(
                    "You can select a maximum of 3 roommates."
            );
        }

        preference.setStudent(student);
        preference.setPreferredStudent(preferredStudent);

        return preferenceRepository.save(preference);
    }

    @Transactional
    public void removePreference(Long studentId, Long preferenceId) {

        if (studentId == null || preferenceId == null) {
            throw new IllegalArgumentException(
                    "Valid student and preference IDs are required."
            );
        }

        if (allocationService.getCurrentAllocation(studentId).isPresent()) {
            throw new IllegalStateException(
                    "You cannot change roommates while you have an allocated room."
            );
        }

        RoommatePreference preference = preferenceRepository
                .findById(preferenceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Roommate preference not found."
                ));

        if (!preference.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException(
                    "You cannot remove another student's preference."
            );
        }

        preferenceRepository.delete(preference);
    }

}
