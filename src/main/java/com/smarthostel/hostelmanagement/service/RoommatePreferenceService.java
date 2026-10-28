
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.repository.RoommatePreferenceRepository;
import com.smarthostel.hostelmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoommatePreferenceService {

    private final RoommatePreferenceRepository preferenceRepository;
    private final StudentRepository studentRepository;

    public RoommatePreferenceService(
            RoommatePreferenceRepository preferenceRepository,
            StudentRepository studentRepository) {

        this.preferenceRepository = preferenceRepository;
        this.studentRepository = studentRepository;
    }

    // Get all roommate preferences
    public List<RoommatePreference> getAllPreferences() {
        return preferenceRepository.findAll();
    }

    // Find a preference by its ID
    public Optional<RoommatePreference> getPreferenceById(Long id) {
        return preferenceRepository.findById(id);
    }

    // Get preferences submitted by a student
    public List<RoommatePreference> getPreferencesByStudent(Long studentId) {
        return preferenceRepository.findByStudent_Id(studentId);
    }

    // Save a roommate preference
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

        // Students cannot select themselves
        if (studentId.equals(preferredStudentId)) {
            throw new IllegalArgumentException(
                    "You cannot select yourself as a roommate."
            );
        }

        // Make sure both students exist
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found."
                ));

        Student preferredStudent =
                studentRepository.findById(preferredStudentId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Preferred roommate not found."
                        ));

        // Prevent duplicate roommate selections
        if (preferenceRepository
                .existsByStudent_IdAndPreferredStudent_Id(
                        studentId, preferredStudentId)) {

            throw new IllegalArgumentException(
                    "You have already selected this roommate."
            );
        }

        // Maximum 3 preferred roommates per student
        if (preferenceRepository.countByStudent_Id(studentId) >= 3) {
            throw new IllegalArgumentException(
                    "You can select a maximum of 3 roommates."
            );
        }

        preference.setStudent(student);
        preference.setPreferredStudent(preferredStudent);

        return preferenceRepository.save(preference);
    }
}
