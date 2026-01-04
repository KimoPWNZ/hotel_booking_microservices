package com.example.hotel.web;
import com.example.hotel.service.RoomService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class InternalRoomController {
    private final RoomService roomService;

    @PostMapping("/{id}/confirm-availability")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> confirm(@PathVariable Long id, @RequestBody ConfirmRequest req) {
        roomService.confirmAvailability(id, req.startDate, req.endDate, req.bookingUid, req.requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> release(@RequestBody ReleaseRequest req) {
        roomService.release(req.requestId);
        return ResponseEntity.ok().build();
    }

    @Data
    static class ConfirmRequest { LocalDate startDate; LocalDate endDate; String bookingUid; String requestId; }
    @Data
    static class ReleaseRequest { String requestId; }
}