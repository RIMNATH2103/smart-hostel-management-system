
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

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get student by database ID
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    // Get student by email
    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    // Get student by registration number
    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public boolean isEmailAvailable(String email) {
        return !studentRepository.existsByEmail(email);
    }

    public boolean isStudentIdAvailable(String studentId) {
        return !studentRepository.existsByStudentId(studentId);
    }

    // Validate student registration
    public void validateNewStudent(Student student) {

        if (student == null) {
            throw new IllegalArgumentException(
                    "Student information is required."
            );
        }

        validateStudentDetails(student);

        if (student.getPassword() == null
                || student.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        if (!isEmailAvailable(student.getEmail())) {
            throw new IllegalArgumentException(
                    "This email address is already registered."
            );
        }

        if (!isStudentIdAvailable(student.getStudentId())) {
            throw new IllegalArgumentException(
                    "This student registration number is already registered."
            );
        }
    }

    // Validate common student information
    private void validateStudentDetails(Student student) {

        if (student.getFullName() == null
                || student.getFullName().isBlank()) {
            throw new IllegalArgumentException(
                    "Student full name is required."
            );
        }

        String phoneNumber = student.getPhoneNumber();

        if (phoneNumber == null
                || !phoneNumber.matches("\\+?[0-9]{10,15}")) {
            throw new IllegalArgumentException(
                    "Please enter a valid phone number (10 to 15 digits)."
            );
        }

        String email = student.getEmail();

        if (email == null
                || !email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }

        if (student.getStudentId() == null
                || student.getStudentId().isBlank()) {
            throw new IllegalArgumentException(
                    "Student registration number is required."
            );
        }
    }

    // CREATE: Register a new student
    public Student registerStudent(Student student) {

        validateNewStudent(student);

        student.setPassword(
                passwordEncoder.encode(student.getPassword())
        );

        return studentRepository.save(student);
    }

    // UPDATE: Edit existing student information
    public Student updateStudent(Long id, Student updatedStudent) {

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found."
                ));

        if (updatedStudent == null) {
            throw new IllegalArgumentException(
                    "Student information is required."
            );
        }

        validateStudentDetails(updatedStudent);

        // Check email uniqueness when email changes
        if (!existingStudent.getEmail()
                .equals(updatedStudent.getEmail())
                && !isEmailAvailable(updatedStudent.getEmail())) {
            throw new IllegalArgumentException(
                    "This email address is already registered."
            );
        }

        // Check registration number uniqueness
        if (!existingStudent.getStudentId()
                .equals(updatedStudent.getStudentId())
                && !isStudentIdAvailable(updatedStudent.getStudentId())) {
            throw new IllegalArgumentException(
                    "This student registration number is already registered."
            );
        }

        existingStudent.setFullName(updatedStudent.getFullName());
        existingStudent.setEmail(updatedStudent.getEmail());
        existingStudent.setPhoneNumber(updatedStudent.getPhoneNumber());
        existingStudent.setStudentId(updatedStudent.getStudentId());

        // Existing password remains unchanged
        return studentRepository.save(existingStudent);
    }

    // DELETE: Remove a student record
    public void deleteStudent(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found."
                ));

        studentRepository.delete(student);
    }
}
