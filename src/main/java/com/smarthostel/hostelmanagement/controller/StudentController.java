
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(
            @RequestBody Student student) {

        try {
            Student savedStudent =
                    studentService.registerStudent(student);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Student registered successfully. ID: "
                            + savedStudent.getId());

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllStudents(HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        return studentService.getStudentById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody Student student,
            HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        try {
            Student updatedStudent =
                    studentService.updateStudent(id, student);

            return ResponseEntity.ok(
                    "Student updated successfully. ID: "
                            + updatedStudent.getId()
            );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        try {
            studentService.deleteStudent(id);

            return ResponseEntity.ok(
                    "Student deleted successfully."
            );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("adminId") instanceof Long;
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Administrator login required.");
    }
}
