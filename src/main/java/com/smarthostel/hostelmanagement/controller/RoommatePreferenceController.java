
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.service.RoommatePreferenceService;
import com.smarthostel.hostelmanagement.service.StudentService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roommate-preferences")
public class RoommatePreferenceController {

    private final RoommatePreferenceService preferenceService;
    private final StudentService studentService;

    public RoommatePreferenceController(
            RoommatePreferenceService preferenceService,
            StudentService studentService) {

        this.preferenceService = preferenceService;
        this.studentService = studentService;
    }

    // View the logged-in student's selected roommates
    @GetMapping("/my")
    public ResponseEntity<?> getMyPreferences(HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        List<Map<String, Object>> preferences =
                preferenceService.getPreferencesByStudent((Long) studentId)
                        .stream()
                        .map(preference -> Map.<String, Object>of(
                                "id", preference.getId(),
                                "studentId",
                                preference.getPreferredStudent().getStudentId(),
                                "fullName",
                                preference.getPreferredStudent().getFullName()
                        ))
                        .toList();

        return ResponseEntity.ok(preferences);
    }

    // Select a preferred roommate for the logged-in student
    @PostMapping("/my")
    public ResponseEntity<?> addMyPreference(
            @RequestBody PreferenceRequest request,
            HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        if (request.preferredStudentId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Select a student first."));
        }

        Student student = studentService.getStudentById((Long) studentId)
                .orElse(null);

        Student preferredStudent =
                studentService.getStudentById(request.preferredStudentId())
                        .orElse(null);

        if (student == null || preferredStudent == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Student not found."));
        }

        RoommatePreference preference = new RoommatePreference();
        preference.setStudent(student);
        preference.setPreferredStudent(preferredStudent);

        try {
            preferenceService.savePreference(preference);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message",
                            "Roommate preference saved successfully."
                    ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        }
    }


    // Get students available for roommate selection
    @GetMapping("/available-students")
    public ResponseEntity<?> getAvailableStudents(HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        List<Map<String, Object>> students =
                studentService.getAllStudents()
                        .stream()
                        .filter(student ->
                                !student.getId().equals(studentId))
                        .map(student -> Map.<String, Object>of(
                                "id", student.getId(),
                                "studentId", student.getStudentId(),
                                "fullName", student.getFullName()
                        ))
                        .toList();

        return ResponseEntity.ok(students);
    }


    // Request body contains only the preferred student's database ID
    public record PreferenceRequest(Long preferredStudentId) {
    }
}
