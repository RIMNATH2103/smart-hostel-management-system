
package com.smarthostel.hostelmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "roommate_preferences",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"student_id", "preferred_student_id"}
                )
        }
)
public class RoommatePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preferred_student_id", nullable = false)
    private Student preferredStudent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RoommatePreference() {
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Student getPreferredStudent() {
        return preferredStudent;
    }

    public void setPreferredStudent(Student preferredStudent) {
        this.preferredStudent = preferredStudent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
