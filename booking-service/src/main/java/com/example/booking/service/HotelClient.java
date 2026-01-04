package com.example.booking.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HotelClient {
    private final WebClient webClient;
    @Value("${booking.saga.hotel.timeout-ms:2000}") long timeoutMs;
    @Value("${booking.saga.hotel.retries:3}") int retries;
    @Value("${booking.saga.hotel.backoff-ms:200}") long backoff;

    public void confirm(Long roomId, LocalDate start, LocalDate end, String bookingUid, String requestId) {
        webClient.post()
                .uri("/api/rooms/{id}/confirm-availability", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("startDate", start, "endDate", end, "bookingUid", bookingUid, "requestId", requestId))
                .retrieve()
                .onStatus(s -> s.value()==409, resp -> resp.createException().map(ex -> new RuntimeException("conflict")))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(Retry.backoff(retries, Duration.ofMillis(backoff)))
                .block();
    }

    public void release(Long roomId, String requestId) {
        webClient.post()
                .uri("/api/rooms/{id}/release", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("requestId", requestId))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(Retry.backoff(retries, Duration.ofMillis(backoff)))
                .block();
    }
}