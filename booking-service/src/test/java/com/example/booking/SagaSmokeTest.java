package com.example.booking;
import com.example.booking.service.SagaOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SagaSmokeTest {
    @Autowired SagaOrchestrator saga;
    @Test
    void contextLoads() { }
}