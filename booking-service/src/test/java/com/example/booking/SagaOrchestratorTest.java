package com.example.booking;

import com.example.booking.entity.Booking;
import com.example.booking.repo.BookingRepository;
import com.example.booking.service.HotelClient;
import com.example.booking.service.SagaOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class SagaOrchestratorTest {

    @Autowired
    private SagaOrchestrator saga;

    @Autowired
    private BookingRepository bookingRepo;

    @MockBean
    private HotelClient hotelClient;

    @BeforeEach
    void setUp() {
        bookingRepo.deleteAll();
    }

    @Test
    void testSuccessfulBookingConfirmation() {
        // Given
        Long userId = 1L;
        Long roomId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        String requestId = "req-success-001";

        // When hotel confirms availability
        doNothing().when(hotelClient).confirm(any(), any(), any(), any(), any());

        // Then booking should be CONFIRMED
        Booking booking = saga.createBooking(userId, roomId, start, end, requestId);

        assertNotNull(booking);
        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals(userId, booking.getUserId());
        assertEquals(roomId, booking.getRoomId());
        verify(hotelClient, times(1)).confirm(roomId, start, end, booking.getBookingUid(), requestId);
    }

    @Test
    void testSagaCompensationOnHotelFailure() {
        // Given
        Long userId = 1L;
        Long roomId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        String requestId = "req-fail-001";

        // When hotel throws exception (room not available)
        doThrow(new RuntimeException("Room not available"))
                .when(hotelClient).confirm(any(), any(), any(), any(), any());

        // Then booking should be CANCELLED and release should be called
        assertThrows(RuntimeException.class, () -> {
            saga.createBooking(userId, roomId, start, end, requestId);
        });

        verify(hotelClient, times(1)).release(roomId, requestId);
        
        // Verify booking is saved with CANCELLED status
        Booking booking = bookingRepo.findByRequestId(requestId).orElse(null);
        assertNotNull(booking);
        assertEquals("CANCELLED", booking.getStatus());
    }

    @Test
    void testIdempotency() {
        // Given
        Long userId = 1L;
        Long roomId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        String requestId = "req-idempotent-001";

        doNothing().when(hotelClient).confirm(any(), any(), any(), any(), any());

        // When creating booking with same requestId twice
        Booking booking1 = saga.createBooking(userId, roomId, start, end, requestId);
        Booking booking2 = saga.createBooking(userId, roomId, start, end, requestId);

        // Then should return the same booking
        assertEquals(booking1.getId(), booking2.getId());
        assertEquals(booking1.getBookingUid(), booking2.getBookingUid());
        
        // Hotel confirm should only be called once
        verify(hotelClient, times(1)).confirm(any(), any(), any(), any(), any());
    }

    @Test
    void testConcurrentBookingRequests() throws InterruptedException {
        // Given
        Long userId = 1L;
        Long roomId = 1L;
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);

        doNothing().when(hotelClient).confirm(any(), any(), any(), any(), any());

        // When creating multiple bookings concurrently
        Thread t1 = new Thread(() -> saga.createBooking(userId, roomId, start, end, "req-concurrent-001"));
        Thread t2 = new Thread(() -> saga.createBooking(userId, roomId, start, end, "req-concurrent-002"));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Then both bookings should be created
        Booking b1 = bookingRepo.findByRequestId("req-concurrent-001").orElse(null);
        Booking b2 = bookingRepo.findByRequestId("req-concurrent-002").orElse(null);

        assertNotNull(b1);
        assertNotNull(b2);
        assertNotEquals(b1.getId(), b2.getId());
    }
}
