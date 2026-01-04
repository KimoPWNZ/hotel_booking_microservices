package com.example.hotel.service;
import com.example.hotel.entity.Hotel;
import com.example.hotel.repo.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;

    public List<Hotel> listAll() {
        return hotelRepository.findAll();
    }

    public Hotel getById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found with id: " + id));
    }

    @Transactional
    public Hotel create(String name, String address) {
        Hotel hotel = Hotel.builder()
                .name(name)
                .address(address)
                .createdAt(Instant.now())
                .build();
        return hotelRepository.save(hotel);
    }

    @Transactional
    public Hotel update(Long id, String name, String address) {
        Hotel hotel = getById(id);
        if (name != null) hotel.setName(name);
        if (address != null) hotel.setAddress(address);
        return hotelRepository.save(hotel);
    }

    @Transactional
    public void delete(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new IllegalArgumentException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id);
    }
}
