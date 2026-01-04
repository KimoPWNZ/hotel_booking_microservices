package com.example.hotel.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.Instant;

@Entity @Table(name="room_locks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomLock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String bookingUid;
    private String requestId;
    private Instant expiresAt;
}