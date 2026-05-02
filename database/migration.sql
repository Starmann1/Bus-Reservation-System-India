-- File: database/migration.sql
-- Run this script if you already have the old database structure
-- This will add the new columns to existing bookings table

USE java_mini_project;

-- Add new columns to bookings table
ALTER TABLE bookings 
ADD COLUMN travel_name VARCHAR(100) AFTER travel_time,
ADD COLUMN bus_type VARCHAR(50) AFTER travel_name,
ADD COLUMN bus_number VARCHAR(20) AFTER bus_type,
ADD COLUMN seat_number VARCHAR(10) AFTER bus_number,
ADD COLUMN fare INT DEFAULT 0 AFTER seat_number,
ADD COLUMN distance INT DEFAULT 0 AFTER fare;

-- Add index for bus_number for faster seat availability checks
ALTER TABLE bookings 
ADD INDEX idx_bus_number (bus_number);

-- Update existing records with default values (optional)
UPDATE bookings 
SET 
    travel_name = 'Default Travels',
    bus_type = 'AC Seater',
    bus_number = CONCAT('TN', FLOOR(10 + RAND() * 90), 'AB', FLOOR(1000 + RAND() * 9000)),
    seat_number = FLOOR(1 + RAND() * 40),
    fare = 500,
    distance = 45
WHERE travel_name IS NULL;

-- Verify the changes
DESCRIBE bookings;

-- Check updated data
SELECT * FROM bookings LIMIT 5;