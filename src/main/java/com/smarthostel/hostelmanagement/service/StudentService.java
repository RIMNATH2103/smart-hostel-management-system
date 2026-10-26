package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public boolean isEmailAvailable(String email) {
        return !studentRepository.existsByEmail(email);
    }

    public boolean isStudentIdAvailable(String studentId) {
        return !studentRepository.existsByStudentId(studentId);
    }

    public void validateNewStudent(Student student) {

        if (student == null) {
            throw new IllegalArgumentException(
                    "Student information is required."
            );
        }

        String fullName = student.getFullName();

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Student full name is required."
            );
        }

        String phoneNumber = student.getPhoneNumber();

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Student phone number is required."
            );
        }

        if (!phoneNumber.matches("\\+?[0-9]{10,15}")) {
            throw new IllegalArgumentException(
                    "Please enter a valid phone number (10 to 15 digits)."
            );
        }

        String password = student.getPassword();

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        String email = student.getEmail();

        if (email == null || email.isBlank()
                || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }

        if (!isEmailAvailable(email)) {
            throw new IllegalArgumentException(
                    "This email address is already registered."
            );
        }

        String studentId = student.getStudentId();

        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Student registration number is required."
            );
        }

        if (!isStudentIdAvailable(studentId)) {
            throw new IllegalArgumentException(
                    "This student registration number is already registered."
            );
        }
    }

    public Student registerStudent(Student student) {

        validateNewStudent(student);

        String hashedPassword = passwordEncoder.encode(
                student.getPassword()
        );

        student.setPassword(hashedPassword);

        return studentRepository.save(student);
    }
}
