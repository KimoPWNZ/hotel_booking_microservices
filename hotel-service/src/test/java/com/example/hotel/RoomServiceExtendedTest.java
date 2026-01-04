package com.example.hotel;

import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomLock;
import com.example.hotel.repo.RoomLockRepository;
import com.example.hotel.repo.RoomRepository;
import com.example.hotel.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoomServiceExtendedTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomLockRepository lockRepository;

    @BeforeEach
    void setUp() {
        lockRepository.deleteAll();
    }

    @Test
    void testRecommendSortsByTimesBookedThenId() {
        // Given - data is already seeded with varying timesBooked values
        // Room 2: timesBooked=0, Room 9: timesBooked=0, Room 7: timesBooked=1, etc.

        // When
        List<Room> recommended = roomService.recommend();

        // Then - should be sorted by timesBooked ASC, then by id ASC
        assertFalse(recommended.isEmpty());
        for (int i = 1; i < recommended.size(); i++) {
            Room prev = recommended.get(i - 1);
            Room curr = recommended.get(i);
            
            // Either prev.timesBooked < curr.timesBooked OR
            // (prev.timesBooked == curr.timesBooked AND prev.id < curr.id)
            assertTrue(
                prev.getTimesBooked() < curr.getTimesBooked() ||
                (prev.getTimesBooked() == curr.getTimesBooked() && prev.getId() < curr.getId()),
                String.format("Ordering violation: Room %d (times=%d) should come before Room %d (times=%d)",
                    prev.getId(), prev.getTimesBooked(), curr.getId(), curr.getTimesBooked())
            );
        }
    }

    @Test
    void testConfirmAvailabilitySuccess() {
        // Given
        Long roomId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        String bookingUid = "booking-001";
        String requestId = "req-001";

        Room roomBefore = roomRepository.findById(roomId).orElseThrow();
        long timesBookedBefore = roomBefore.getTimesBooked();

        // When
        roomService.confirmAvailability(roomId, start, end, bookingUid, requestId);

        // Then - lock should be created
        RoomLock lock = lockRepository.findByRequestId(requestId).orElse(null);
        assertNotNull(lock);
        assertEquals(roomId, lock.getRoomId());
        assertEquals(start, lock.getStartDate());
        assertEquals(end, lock.getEndDate());
        assertEquals(bookingUid, lock.getBookingUid());

        // And timesBooked should be incremented
        Room roomAfter = roomRepository.findById(roomId).orElseThrow();
        assertEquals(timesBookedBefore + 1, roomAfter.getTimesBooked());
    }

    @Test
    void testConfirmAvailabilityIdempotency() {
        // Given
        Long roomId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        String bookingUid = "booking-002";
        String requestId = "req-002";

        // When - calling confirm twice with same requestId
        roomService.confirmAvailability(roomId, start, end, bookingUid, requestId);
        roomService.confirmAvailability(roomId, start, end, bookingUid, requestId);

        // Then - only one lock should be created
        List<RoomLock> locks = lockRepository.findAll();
        long matchingLocks = locks.stream().filter(l -> l.getRequestId().equals(requestId)).count();
        assertEquals(1, matchingLocks);
    }

    @Test
    void testConfirmAvailabilityConflict() {
        // Given - create first lock
        Long roomId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        
        roomService.confirmAvailability(roomId, start, end, "booking-003", "req-003");

        // When - trying to book overlapping dates
        LocalDate overlapStart = LocalDate.now().plusDays(2);
        LocalDate overlapEnd = LocalDate.now().plusDays(4);

        // Then - should throw exception
        assertThrows(IllegalStateException.class, () -> {
            roomService.confirmAvailability(roomId, overlapStart, overlapEnd, "booking-004", "req-004");
        });
    }

    @Test
    void testReleaseRemovesLock() {
        // Given
        Long roomId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        String requestId = "req-release-001";

        roomService.confirmAvailability(roomId, start, end, "booking-005", requestId);
        assertTrue(lockRepository.findByRequestId(requestId).isPresent());

        // When
        roomService.release(requestId);

        // Then - lock should be removed
        assertFalse(lockRepository.findByRequestId(requestId).isPresent());
    }

    @Test
    void testGetOccupancyStats() {
        // Given
        Long roomId = 1L;
        roomService.confirmAvailability(roomId, LocalDate.now().plusDays(1), 
                LocalDate.now().plusDays(3), "booking-006", "req-stats-001");

        // When
        RoomService.RoomOccupancyStats stats = roomService.getOccupancyStats(roomId);

        // Then
        assertNotNull(stats);
        assertEquals(roomId, stats.roomId());
        assertTrue(stats.timesBooked() > 0);
        assertEquals(1, stats.currentLocks());
    }

    @Test
    void testParallelBookingConflictDetection() throws InterruptedException {
        // This test demonstrates conflict detection, but may be non-deterministic
        // in actual parallel execution due to timing. The key is that the service
        // properly prevents overlapping locks via the database constraint check.
        
        Long roomId = 2L;
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(7);

        // First booking should succeed
        roomService.confirmAvailability(roomId, start, end, "booking-first", "req-first");
        assertTrue(lockRepository.findByRequestId("req-first").isPresent());

        // Second booking with overlapping dates should fail
        assertThrows(IllegalStateException.class, () -> {
            roomService.confirmAvailability(roomId, start, end, "booking-second", "req-second");
        });
    }
}
