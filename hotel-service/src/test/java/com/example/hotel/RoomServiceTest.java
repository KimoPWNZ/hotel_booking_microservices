package com.example.hotel;
import com.example.hotel.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoomServiceTest {
    @Autowired RoomService roomService;
    @Test void recommendSorted() {
        var rec = roomService.recommend();
        for (int i=1;i<rec.size();i++) {
            assertTrue(rec.get(i-1).getTimesBooked() <= rec.get(i).getTimesBooked());
        }
    }
}