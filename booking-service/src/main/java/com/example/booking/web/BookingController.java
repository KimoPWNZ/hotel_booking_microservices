package com.example.booking.web;
import com.example.booking.entity.Booking;
import com.example.booking.repo.BookingRepository;
import com.example.booking.service.SagaOrchestrator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BookingController {
    private final SagaOrchestrator saga;
    private final BookingRepository bookingRepo;

    @PostMapping("/booking")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Booking> create(
            @Valid @RequestBody BookingRequest req, 
            @RequestHeader(name="X-Request-Id", required=false) String rid, 
            Authentication auth) {
        if (req.startDate.isAfter(req.endDate) || req.startDate.isEqual(req.endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (req.startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
        Booking b = saga.createBooking(Long.valueOf(auth.getName()), req.roomId, req.startDate, req.endDate, rid);
        return ResponseEntity.ok(b);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<Booking> my(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookingRepo.findByUserId(Long.valueOf(auth.getName()), pageable);
    }

    @GetMapping("/booking/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Booking> one(@PathVariable Long id, Authentication auth) {
        return bookingRepo.findById(id)
                .filter(b -> b.getUserId().equals(Long.valueOf(auth.getName())))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/booking/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        return bookingRepo.findById(id)
                .filter(b -> b.getUserId().equals(Long.valueOf(auth.getName())))
                .map(b -> { b.setStatus("CANCELLED"); bookingRepo.save(b); return ResponseEntity.ok().build(); })
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    static class BookingRequest { 
        @NotNull(message = "Room ID is required")
        Long roomId; 
        
        @NotNull(message = "Start date is required")
        LocalDate startDate; 
        
        @NotNull(message = "End date is required")
        LocalDate endDate; 
        
        boolean autoSelect; 
    }
}
