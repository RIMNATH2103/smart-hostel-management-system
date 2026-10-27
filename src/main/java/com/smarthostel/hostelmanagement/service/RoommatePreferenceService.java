
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import com.smarthostel.hostelmanagement.repository.RoommatePreferenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoommatePreferenceService {

    private final RoommatePreferenceRepository preferenceRepository;

    public RoommatePreferenceService(
            RoommatePreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
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
        return preferenceRepository.findAll()
                .stream()
                .filter(preference ->
                        preference.getStudent().getId().equals(studentId))
                .toList();
    }

    // Save a roommate preference
    public RoommatePreference savePreference(
            RoommatePreference preference) {

        if (preference == null
                || preference.getStudent() == null
                || preference.getPreferredStudent() == null) {
            throw new IllegalArgumentException(
                    "Both students are required."
            );
        }

        if (preference.getStudent().getId()
                .equals(preference.getPreferredStudent().getId())) {
            throw new IllegalArgumentException(
                    "A student cannot select themselves as a roommate."
            );
        }

        return preferenceRepository.save(preference);
    }
}
