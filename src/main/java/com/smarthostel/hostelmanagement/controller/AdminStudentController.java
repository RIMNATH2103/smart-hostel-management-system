
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import com.smarthostel.hostelmanagement.service.StudentService;
import com.smarthostel.hostelmanagement.service.RoomAllocationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    private final StudentService studentService;
    private final RoomAllocationService allocationService;

    public AdminStudentController(
            StudentService studentService,
            RoomAllocationService allocationService) {

        this.studentService = studentService;
        this.allocationService = allocationService;
    }

    // Information displayed on the Student Management page
    public record StudentInfo(
            Long id,
            String studentId,
            String fullName,
            String roomNumber,
            String allocationStatus
    ) {
    }

    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("adminId") instanceof Long;
    }

    // Convert a student into safe information for the admin page
    private StudentInfo toStudentInfo(Student student) {

        Optional<RoomAllocation> allocation =
                allocationService.getCurrentAllocation(student.getId());

        String roomNumber = "Not Allocated";
        String allocationStatus = "Waiting";

        if (allocation.isPresent()) {
            roomNumber = allocation.get().getRoom().getRoomNumber();
            allocationStatus = "Allocated";
        }

        return new StudentInfo(
                student.getId(),
                student.getStudentId(),
                student.getFullName(),
                roomNumber,
                allocationStatus
        );
    }

    // View all registered students
    @GetMapping
    public ResponseEntity<List<StudentInfo>> getAllStudents(
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).build();
        }

        List<StudentInfo> students = studentService.getAllStudents()
                .stream()
                .map(this::toStudentInfo)
                .toList();

        return ResponseEntity.ok(students);
    }
}
