
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import com.smarthostel.hostelmanagement.service.RoommatePreferenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roommate-preferences")
public class RoommatePreferenceController {

    private final RoommatePreferenceService preferenceService;

    public RoommatePreferenceController(
            RoommatePreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    // Get all roommate preferences
    @GetMapping
    public List<RoommatePreference> getAllPreferences() {
        return preferenceService.getAllPreferences();
    }

    // Get a roommate preference by ID
    @GetMapping("/{id}")
    public ResponseEntity<RoommatePreference> getPreferenceById(
            @PathVariable Long id) {

        return preferenceService.getPreferenceById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get preferences submitted by a student
    @GetMapping("/student/{studentId}")
    public List<RoommatePreference> getPreferencesByStudent(
            @PathVariable Long studentId) {

        return preferenceService.getPreferencesByStudent(studentId);
    }

    // Save a roommate preference
    @PostMapping
    public ResponseEntity<?> savePreference(
            @RequestBody RoommatePreference preference) {

        try {
            RoommatePreference savedPreference =
                    preferenceService.savePreference(preference);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Roommate preference saved successfully. ID: "
                            + savedPreference.getId());

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }
}
