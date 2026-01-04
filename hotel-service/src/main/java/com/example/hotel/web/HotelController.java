package com.example.hotel.web;
import com.example.hotel.entity.Hotel;
import com.example.hotel.service.HotelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {
    private final HotelService hotelService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Hotel> list() {
        return hotelService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Hotel> get(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hotel> create(@Valid @RequestBody HotelRequest req) {
        Hotel hotel = hotelService.create(req.name, req.address);
        return ResponseEntity.ok(hotel);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hotel> update(@PathVariable Long id, @RequestBody HotelRequest req) {
        Hotel hotel = hotelService.update(id, req.name, req.address);
        return ResponseEntity.ok(hotel);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Data
    static class HotelRequest {
        @NotBlank(message = "Hotel name is required")
        String name;
        String address;
    }
}
