
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.repository.StudentRepository;
import com.smarthostel.hostelmanagement.repository.RoomAllocationRepository;
import com.smarthostel.hostelmanagement.repository.VacateRequestRepository;
import com.smarthostel.hostelmanagement.repository.RoommatePreferenceRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomAllocationRepository roomAllocationRepository;
    private final VacateRequestRepository vacateRequestRepository;
    private final RoommatePreferenceRepository roommatePreferenceRepository;

    public StudentService(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder,
            RoomAllocationRepository roomAllocationRepository,
            VacateRequestRepository vacateRequestRepository,
            RoommatePreferenceRepository roommatePreferenceRepository) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.roomAllocationRepository = roomAllocationRepository;
        this.vacateRequestRepository = vacateRequestRepository;
        this.roommatePreferenceRepository = roommatePreferenceRepository;
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

    private void validateNewStudent(Student student) {

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

        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException(
                    "Email is already registered."
            );
        }

        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException(
                    "Student username is already registered."
            );
        }
    }

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

        String studentId = student.getStudentId();

        if (studentId == null
                || !studentId.matches("EG/[0-9]{4}/[0-9]{4}")) {
            throw new IllegalArgumentException(
                    "Student registration number must follow EG/YYYY/NNNN format."
            );
        }
    }

    public Student registerStudent(Student student) {

        validateNewStudent(student);

        student.setPassword(
                passwordEncoder.encode(student.getPassword())
        );

        return studentRepository.save(student);
    }

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

        if (!existingStudent.getEmail()
                .equals(updatedStudent.getEmail())
                && !isEmailAvailable(updatedStudent.getEmail())) {
            throw new IllegalArgumentException(
                    "This email address is already registered."
            );
        }

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

        return studentRepository.save(existingStudent);
    }

    @Transactional
    public void deleteStudent(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found."
                ));

        if (roomAllocationRepository.existsByStudent_Id(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete student: room allocation history exists."
            );
        }

        if (vacateRequestRepository.existsByStudent_Id(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete student: vacate request history exists."
            );
        }

        if (roommatePreferenceRepository.existsByStudent_Id(id)
                || roommatePreferenceRepository
                .existsByPreferredStudent_Id(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete student: roommate preferences exist."
            );
        }

        studentRepository.delete(student);
    }

    public Optional<Student> authenticateStudent(
            String username, String password) {

        if (username == null || password == null) {
            return Optional.empty();
        }

        if (!username.matches("EG/[0-9]{4}/[0-9]{4}")) {
            return Optional.empty();
        }

        Optional<Student> student = studentRepository
                .findByStudentId(username);

        if (student.isPresent()
                && passwordEncoder.matches(
                password, student.get().getPassword())) {
            return student;
        }

        return Optional.empty();
    }

}
