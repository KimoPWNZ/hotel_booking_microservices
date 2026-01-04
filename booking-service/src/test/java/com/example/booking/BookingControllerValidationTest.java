package com.example.booking;

import com.example.booking.entity.Booking;
import com.example.booking.entity.User;
import com.example.booking.repo.BookingRepository;
import com.example.booking.repo.ProcessedRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ProcessedRequestRepository processedRequestRepository;

    @BeforeEach
    void setUp() {
        processedRequestRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void testCreateBookingWithoutAuthentication() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("roomId", 1L);
        request.put("startDate", LocalDate.now().plusDays(1).toString());
        request.put("endDate", LocalDate.now().plusDays(3).toString());

        mockMvc.perform(post("/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testCreateBookingWithValidationError_StartDateInPast() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("roomId", 1L);
        request.put("startDate", LocalDate.now().minusDays(1).toString());
        request.put("endDate", LocalDate.now().plusDays(3).toString());

        mockMvc.perform(post("/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("past")));
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testCreateBookingWithValidationError_EndBeforeStart() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("roomId", 1L);
        request.put("startDate", LocalDate.now().plusDays(3).toString());
        request.put("endDate", LocalDate.now().plusDays(1).toString());

        mockMvc.perform(post("/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("before end date")));
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testCreateBookingWithValidationError_MissingRoomId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("startDate", LocalDate.now().plusDays(1).toString());
        request.put("endDate", LocalDate.now().plusDays(3).toString());

        mockMvc.perform(post("/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testGetBookingsPagination() throws Exception {
        // Given - create some test bookings with future dates
        for (int i = 1; i <= 5; i++) {
            Booking booking = Booking.builder()
                    .userId(1L)
                    .roomId(1L)
                    .startDate(LocalDate.now().plusDays(i))
                    .endDate(LocalDate.now().plusDays(i + 2))
                    .status("CONFIRMED")
                    .requestId("req-" + i)
                    .bookingUid("uid-" + i)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            bookingRepository.save(booking);
        }

        // When/Then - request first page with size 2
        mockMvc.perform(get("/bookings")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testGetBooking_OnlyOwnBookings() throws Exception {
        // Given - create booking for user 1
        Booking booking = Booking.builder()
                .userId(1L)
                .roomId(1L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .status("CONFIRMED")
                .requestId("req-own")
                .bookingUid("uid-own")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        booking = bookingRepository.save(booking);

        // When - user 1 tries to get their booking
        mockMvc.perform(get("/booking/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "2", roles = {"USER"})
    void testGetBooking_CannotAccessOthersBookings() throws Exception {
        // Given - create booking for user 1
        Booking booking = Booking.builder()
                .userId(1L)
                .roomId(1L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .status("CONFIRMED")
                .requestId("req-other")
                .bookingUid("uid-other")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        booking = bookingRepository.save(booking);

        // When - user 2 tries to access user 1's booking
        mockMvc.perform(get("/booking/" + booking.getId()))
                .andExpect(status().isNotFound());
    }
}
