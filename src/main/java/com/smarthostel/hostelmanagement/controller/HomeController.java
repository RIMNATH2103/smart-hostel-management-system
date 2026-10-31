
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import com.smarthostel.hostelmanagement.service.StudentService;
import com.smarthostel.hostelmanagement.service.RoomAllocationService;
import com.smarthostel.hostelmanagement.entity.Admin;
import com.smarthostel.hostelmanagement.service.AdminService;

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
    private final AdminService adminService;

    public HomeController(
            StudentService studentService,
            RoomAllocationService roomAllocationService,
            AdminService adminService) {

        this.studentService = studentService;
        this.roomAllocationService = roomAllocationService;
        this.adminService = adminService;
    }

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/student/login")
    public String showStudentLoginPage(HttpSession session) {

        if (session.getAttribute("studentId") instanceof Long) {
            return "redirect:/student/dashboard";
        }

        return "student-login";
    }

    @GetMapping("/student/register")
    public String showStudentRegistrationPage(HttpSession session) {

        if (session.getAttribute("studentId") instanceof Long) {
            return "redirect:/student/dashboard";
        }

        return "student-register";
    }

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

    @GetMapping("/student/profile")
    public String showStudentProfile(HttpSession session, Model model) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long id)) {
            return "redirect:/student/login";
        }

        Optional<Student> student = studentService.getStudentById(id);

        if (student.isEmpty()) {
            session.invalidate();
            return "redirect:/student/login";
        }

        model.addAttribute("student", student.get());

        Optional<RoomAllocation> allocation =
                roomAllocationService.getCurrentAllocation(id);

        if (allocation.isPresent()) {
            model.addAttribute(
                    "roomNumber",
                    allocation.get().getRoom().getRoomNumber()
            );
        } else {
            model.addAttribute("roomNumber", "Not allocated");
        }

        return "student-profile";
    }


    public record RoommateInfo(
            String fullName,
            String studentId
    ) {
    }

    @GetMapping("/student/roommates")
    public String showRoommatePreferencesPage(HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return "redirect:/student/login";
        }

        return "student-roommates";
    }

    @GetMapping("/student/vacate-request")
    public String showVacateRequestPage(HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return "redirect:/student/login";
        }

        return "student-vacate-request";
    }

    @GetMapping("/admin/login")
    public String showAdminLoginPage(HttpSession session) {

        Object adminId = session.getAttribute("adminId");

        if (adminId instanceof Long) {
            return "redirect:/admin/dashboard";
        }

        return "admin-login";
    }

    @GetMapping("/admin/register")
    public String showAdminRegistrationPage(HttpSession session) {

        Object adminId = session.getAttribute("adminId");

        if (!(adminId instanceof Long id)
                || adminService.getAdminById(id).isEmpty()) {
            return "redirect:/admin/login";
        }

        return "admin-register";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {

        Object adminId = session.getAttribute("adminId");

        if (!(adminId instanceof Long id)) {
            return "redirect:/admin/login";
        }

        Optional<Admin> admin = adminService.getAdminById(id);

        if (admin.isEmpty()) {
            session.invalidate();
            return "redirect:/admin/login";
        }

        model.addAttribute("adminName", admin.get().getFullName());

        return "admin-dashboard";
    }

    @GetMapping("/admin/profile")
    public String showAdminProfile(HttpSession session, Model model) {

        Object adminId = session.getAttribute("adminId");

        if (!(adminId instanceof Long id)) {
            return "redirect:/admin/login";
        }

        Optional<Admin> admin = adminService.getAdminById(id);

        if (admin.isEmpty()) {
            session.invalidate();
            return "redirect:/admin/login";
        }

        model.addAttribute("admin", admin.get());

        return "admin-profile";
    }

    @GetMapping("/admin/vacate-requests")
    public String showAdminVacateRequestsPage(HttpSession session) {

        Object adminId = session.getAttribute("adminId");

        if (!(adminId instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-vacate-requests";
    }

    @GetMapping("/admin/rooms")
    public String showAdminRoomsPage(HttpSession session) {

        if (!(session.getAttribute("adminId") instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-rooms";
    }

    @GetMapping("/admin/allocations")
    public String showAdminAllocationsPage(HttpSession session) {

        Object adminId = session.getAttribute("adminId");

        if (!(adminId instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-allocations";
    }

    @GetMapping("/admin/students")
    public String showAdminStudentsPage(HttpSession session) {

        if (!(session.getAttribute("adminId") instanceof Long)) {
            return "redirect:/admin/login";
        }

        return "admin-students";
    }
}
