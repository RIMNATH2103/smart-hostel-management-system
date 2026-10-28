
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student-auth")
public class StudentAuthController {

    private final StudentService studentService;

    public StudentAuthController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Check student credentials and create a login session
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        Optional<Student> student = studentService.authenticateStudent(
                loginRequest.username(), loginRequest.password()
        );

        if (student.isEmpty()) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Invalid username or password.")
            );
        }

        // Create a fresh session after successful authentication
        HttpSession existingSession = request.getSession(false);

        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("studentId", student.get().getId());

        return ResponseEntity.ok(
                Map.of("message", "Login successful.")
        );
    }

    public record LoginRequest(String username, String password) {
    }
}
