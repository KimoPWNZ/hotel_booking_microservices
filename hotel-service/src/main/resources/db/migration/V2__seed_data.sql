-- Insert seed data for hotels
INSERT INTO hotels(id, name, address) VALUES 
    (1, 'Grand Hotel', 'Main st'),
    (2, 'Lake View', 'Lake st'),
    (3, 'City Center Inn', 'Downtown Ave');

-- Insert seed data for rooms with varied timesBooked
INSERT INTO rooms(id, hotel_id, number, available, times_booked, created_at) VALUES
    -- Grand Hotel rooms
    (1, 1, '101', true, 5, CURRENT_TIMESTAMP()),
    (2, 1, '102', true, 0, CURRENT_TIMESTAMP()),
    (3, 1, '103', true, 3, CURRENT_TIMESTAMP()),
    (4, 1, '104', true, 7, CURRENT_TIMESTAMP()),
    (5, 1, '105', true, 2, CURRENT_TIMESTAMP()),
    -- Lake View rooms
    (6, 2, '201', true, 4, CURRENT_TIMESTAMP()),
    (7, 2, '202', true, 1, CURRENT_TIMESTAMP()),
    (8, 2, '203', true, 6, CURRENT_TIMESTAMP()),
    -- City Center Inn rooms
    (9, 3, '301', true, 0, CURRENT_TIMESTAMP()),
    (10, 3, '302', true, 2, CURRENT_TIMESTAMP());
