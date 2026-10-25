
package com.smarthostel.hostelmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_allocations")
public class RoomAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "allocated_at", nullable = false)
    private LocalDateTime allocatedAt;

    @Column(name = "vacated_at")
    private LocalDateTime vacatedAt;

    public RoomAllocation() {
    }

    @PrePersist
    public void onCreate() {
        if (allocatedAt == null) {
            allocatedAt = LocalDateTime.now();
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

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public LocalDateTime getVacatedAt() {
        return vacatedAt;
    }

    public void setVacatedAt(LocalDateTime vacatedAt) {
        this.vacatedAt = vacatedAt;
    }
}
