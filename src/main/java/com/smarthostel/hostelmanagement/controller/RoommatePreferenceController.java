
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.service.RoommatePreferenceService;
import com.smarthostel.hostelmanagement.service.StudentService;
import com.smarthostel.hostelmanagement.service.RoomAllocationService;

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
    private final RoomAllocationService allocationService;


    public RoommatePreferenceController(
            RoommatePreferenceService preferenceService,
            StudentService studentService,
            RoomAllocationService allocationService) {

        this.preferenceService = preferenceService;
        this.studentService = studentService;
        this.allocationService = allocationService;
    }


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

        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", exception.getMessage()));
        }

    }

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
                        .filter(student ->
                                allocationService.getCurrentAllocation(student.getId()).isEmpty())

                        .map(student -> Map.<String, Object>of(
                                "id", student.getId(),
                                "studentId", student.getStudentId(),
                                "fullName", student.getFullName()
                        ))
                        .toList();

        return ResponseEntity.ok(students);
    }

    public record PreferenceRequest(Long preferredStudentId) {
    }

    @DeleteMapping("/my/{preferenceId}")
    public ResponseEntity<?> removeMyPreference(
            @PathVariable Long preferenceId,
            HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        try {
            preferenceService.removePreference(id, preferenceId);

            return ResponseEntity.ok(
                    Map.of("message", "Roommate preference removed successfully.")
            );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));

        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/my/edit-permission")
    public ResponseEntity<?> getEditPermission(HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        if (studentService.getStudentById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Student account not found."));
        }

        boolean canEdit = allocationService
                .getCurrentAllocation(id)
                .isEmpty();

        return ResponseEntity.ok(
                Map.of(
                        "canEdit", canEdit,
                        "message", canEdit
                                ? "You can manage your roommate preferences."
                                : "Roommate preferences are locked while you have an allocated room."
                )
        );
    }

}
