
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
