package com.example.booking.service;
import com.example.booking.entity.Booking;
import com.example.booking.entity.ProcessedRequest;
import com.example.booking.repo.BookingRepository;
import com.example.booking.repo.ProcessedRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaOrchestrator {
    private final BookingRepository bookingRepo;
    private final ProcessedRequestRepository processedRepo;
    private final HotelClient hotelClient;

    @Transactional
    public Booking createBooking(Long userId, Long roomId, LocalDate start, LocalDate end, String requestId) {
        if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();
        String rid = requestId;
        Booking existing = bookingRepo.findByRequestId(rid).orElse(null);
        if (existing != null) return existing;

        Booking booking = Booking.builder()
                .userId(userId).roomId(roomId)
                .startDate(start).endDate(end)
                .status("PENDING")
                .requestId(rid)
                .bookingUid(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
        bookingRepo.save(booking);
        processedRepo.save(ProcessedRequest.builder()
                .requestId(rid).bookingId(booking.getId()).status("PENDING").updatedAt(Instant.now()).build());

        try {
            hotelClient.confirm(roomId, start, end, booking.getBookingUid(), rid);
            booking.setStatus("CONFIRMED");
            booking.setUpdatedAt(Instant.now());
            processedRepo.save(ProcessedRequest.builder()
                    .requestId(rid).bookingId(booking.getId()).status("CONFIRMED").updatedAt(Instant.now()).build());
        } catch (Exception ex) {
            try { hotelClient.release(roomId, rid); } catch (Exception ignored) {}
            booking.setStatus("CANCELLED");
            booking.setUpdatedAt(Instant.now());
            processedRepo.save(ProcessedRequest.builder()
                    .requestId(rid).bookingId(booking.getId()).status("CANCELLED").updatedAt(Instant.now()).build());
            throw ex;
        }
        return booking;
    }
}