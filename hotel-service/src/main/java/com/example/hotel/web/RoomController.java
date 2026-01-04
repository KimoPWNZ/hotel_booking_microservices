package com.example.hotel.web;
import com.example.hotel.entity.Room;
import com.example.hotel.service.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Room> list() { 
        return roomService.listAvailable(); 
    }

    @GetMapping("/recommend")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Room> recommend() { 
        return roomService.recommend(); 
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Room> get(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getById(id));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<RoomService.RoomOccupancyStats> getStats(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getOccupancyStats(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> create(@Valid @RequestBody RoomRequest req) {
        Room room = roomService.create(req.hotelId, req.number);
        return ResponseEntity.ok(room);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody RoomUpdateRequest req) {
        Room room = roomService.update(id, req.available);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Data
    static class RoomRequest {
        @NotNull(message = "Hotel ID is required")
        Long hotelId;
        
        @NotBlank(message = "Room number is required")
        String number;
    }

    @Data
    static class RoomUpdateRequest {
        Boolean available;
    }
}
