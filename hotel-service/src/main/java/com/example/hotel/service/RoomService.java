package com.example.hotel.service;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomLock;
import com.example.hotel.repo.RoomLockRepository;
import com.example.hotel.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomLockRepository lockRepository;

    public List<Room> listAvailable() { 
        return roomRepository.findAllAvailable(); 
    }

    public List<Room> recommend() {
        // Fair room selection: sort by times_booked ASC, then by id ASC for tie-breaking
        // This ensures rooms are used evenly and prevents idle rooms
        return roomRepository.findAllAvailable().stream()
                .sorted(Comparator.comparingLong(Room::getTimesBooked).thenComparing(Room::getId))
                .toList();
    }

    public Room getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + id));
    }

    @Transactional
    public Room create(Long hotelId, String number) {
        Room room = Room.builder()
                .hotelId(hotelId)
                .number(number)
                .available(true)
                .timesBooked(0)
                .createdAt(Instant.now())
                .build();
        return roomRepository.save(room);
    }

    @Transactional
    public Room update(Long id, Boolean available) {
        Room room = getById(id);
        if (available != null) room.setAvailable(available);
        return roomRepository.save(room);
    }

    @Transactional
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new IllegalArgumentException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    @Transactional
    public void confirmAvailability(Long roomId, LocalDate start, LocalDate end, String bookingUid, String requestId) {
        // Idempotency: check if this request was already processed
        if (lockRepository.findByRequestId(requestId).isPresent()) return;
        
        // Check for overlapping locks (conflict detection)
        List<RoomLock> overlaps = lockRepository.findOverlaps(roomId, start, end);
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Room is not available for the selected dates");
        }
        
        // Create lock
        RoomLock lock = RoomLock.builder()
                .roomId(roomId).startDate(start).endDate(end)
                .bookingUid(bookingUid).requestId(requestId)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        lockRepository.save(lock);
        
        // Increment timesBooked counter
        roomRepository.findById(roomId).ifPresent(r -> { 
            r.setTimesBooked(r.getTimesBooked() + 1); 
            roomRepository.save(r);
        });
    }

    @Transactional
    public void release(String requestId) {
        lockRepository.deleteByRequestId(requestId);
    }
    
    // Get occupancy statistics for a room
    public RoomOccupancyStats getOccupancyStats(Long roomId) {
        Room room = getById(roomId);
        long activeLocks = lockRepository.findAll().stream()
                .filter(lock -> lock.getRoomId().equals(roomId))
                .filter(lock -> lock.getExpiresAt().isAfter(Instant.now()))
                .count();
        return new RoomOccupancyStats(room.getId(), room.getTimesBooked(), activeLocks);
    }
    
    public record RoomOccupancyStats(Long roomId, long timesBooked, long currentLocks) {}
}
