package com.example.booking.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity @Table(name="bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PENDING/CONFIRMED/CANCELLED
    @Column(unique = true)
    private String requestId;
    @Column(unique = true)
    private String bookingUid;
    private Instant createdAt;
    private Instant updatedAt;
}