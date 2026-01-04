package com.example.hotel.repo;
import com.example.hotel.entity.RoomLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomLockRepository extends JpaRepository<RoomLock, Long> {
    @Query("select l from RoomLock l where l.roomId = :roomId and l.startDate <= :endDate and l.endDate >= :startDate")
    List<RoomLock> findOverlaps(@Param("roomId") Long roomId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    Optional<RoomLock> findByRequestId(String requestId);
    void deleteByRequestId(String requestId);
}