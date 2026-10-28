
package com.smarthostel.hostelmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/student/login")
    public String showStudentLoginPage() {
        return "student-login";
    }
}
