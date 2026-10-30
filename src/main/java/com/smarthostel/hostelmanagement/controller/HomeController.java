
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import com.smarthostel.hostelmanagement.service.StudentService;
import com.smarthostel.hostelmanagement.service.RoomAllocationService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final StudentService studentService;
    private final RoomAllocationService roomAllocationService;

    public HomeController(
            StudentService studentService,
            RoomAllocationService roomAllocationService) {

        this.studentService = studentService;
        this.roomAllocationService = roomAllocationService;
    }

    // Display the homepage
    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    // Display the student login page
    @GetMapping("/student/login")
    public String showStudentLoginPage(HttpSession session) {

        if (session.getAttribute("studentId") instanceof Long) {
            return "redirect:/student/dashboard";
        }

        return "student-login";
    }

    // Display the logged-in student's dashboard
    @GetMapping("/student/dashboard")
    public String showStudentDashboard(
            HttpSession session, Model model) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return "redirect:/student/login";
        }

        Optional<Student> student =
                studentService.getStudentById((Long) studentId);

        if (student.isEmpty()) {
            session.invalidate();
            return "redirect:/student/login";
        }

        model.addAttribute(
                "studentName",
                student.get().getFullName()
        );

        // Find the student's current room
        Optional<RoomAllocation> allocation =
                roomAllocationService.getCurrentAllocation((Long) studentId);

        if (allocation.isPresent()) {

            Long roomId = allocation.get().getRoom().getId();

            model.addAttribute(
                    "roomNumber",
                    allocation.get().getRoom().getRoomNumber()
            );

            model.addAttribute(
                    "roomCapacity",
                    allocation.get().getRoom().getCapacity()
            );

            model.addAttribute(
                    "roomOccupancy",
                    roomAllocationService.getOccupiedSlots(roomId)
            );

            // Retrieve other students sharing the room
            List<RoommateInfo> roommates =
                    roomAllocationService.getActiveAllocations(roomId)
                            .stream()
                            .map(RoomAllocation::getStudent)
                            .filter(roommate ->
                                    !roommate.getId().equals(studentId))
                            .map(roommate -> new RoommateInfo(
                                    roommate.getFullName(),
                                    roommate.getStudentId()
                            ))
                            .toList();

            model.addAttribute("roommates", roommates);
            model.addAttribute("hasRoom", true);

        } else {
            model.addAttribute("hasRoom", false);
            model.addAttribute("roommates", List.of());
        }

        return "student-dashboard";
    }

    // Information displayed in the student's roommates list
    public record RoommateInfo(
            String fullName,
            String studentId
    ) {
    }

    // Display roommate preferences page
    @GetMapping("/student/roommates")
    public String showRoommatePreferencesPage(HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return "redirect:/student/login";
        }

        return "student-roommates";
    }

    // Display student vacate request page
    @GetMapping("/student/vacate-request")
    public String showVacateRequestPage(HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return "redirect:/student/login";
        }

        return "student-vacate-request";
    }

    // Display admin login page
    @GetMapping("/admin/login")
    public String showAdminLoginPage(HttpSession session) {

        Object adminId = session.getAttribute("adminId");

        if (adminId instanceof Long) {
            return "redirect:/admin/dashboard";
        }

        return "admin-login";
    }

    // Display admin dashboard
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {

        Object adminId = session.getAttribute("adminId");

        if (!(adminId instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-dashboard";
    }

    // Display admin vacate requests page
    @GetMapping("/admin/vacate-requests")
    public String showAdminVacateRequestsPage(HttpSession session) {

        Object adminId = session.getAttribute("adminId");

        if (!(adminId instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-vacate-requests";
    }

    // Display the Room Management page for administrators
    @GetMapping("/admin/rooms")
    public String showAdminRoomsPage(HttpSession session) {

        if (!(session.getAttribute("adminId") instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-rooms";
    }


    // Display current allocations and previous room history
    @GetMapping("/admin/allocations")
    public String showAdminAllocationsPage(HttpSession session) {

        Object adminId = session.getAttribute("adminId");

        if (!(adminId instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-allocations";
    }

    // Display the Student Management page for administrators
    @GetMapping("/admin/students")
    public String showAdminStudentsPage(HttpSession session) {

        if (!(session.getAttribute("adminId") instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-students";
    }

}
