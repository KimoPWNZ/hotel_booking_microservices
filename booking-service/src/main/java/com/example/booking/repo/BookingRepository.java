package com.example.booking.repo;
import com.example.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b where b.roomId = :roomId and b.startDate <= :endDate and b.endDate >= :startDate and b.status <> 'CANCELLED'")
    List<Booking> findOverlaps(@Param("roomId") Long roomId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    Optional<Booking> findByRequestId(String requestId);
    Page<Booking> findByUserId(Long userId, Pageable pageable);
}