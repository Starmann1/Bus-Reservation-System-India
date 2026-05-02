-- File: database/schema.sql
-- Bus Reservation System Database Schema

-- Create database
CREATE DATABASE IF NOT EXISTS java_mini_project;
USE java_mini_project;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    from_location VARCHAR(100) NOT NULL,
    to_location VARCHAR(100) NOT NULL,
    travel_date DATE NOT NULL,
    travel_time VARCHAR(20) NOT NULL,
    travel_name VARCHAR(100),
    bus_type VARCHAR(50),
    bus_number VARCHAR(20),
    seat_number VARCHAR(10),
    fare INT DEFAULT 0,
    distance INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_travel_date (travel_date),
    INDEX idx_bus_number (bus_number),
    INDEX idx_status (status)
);

-- Insert sample users (Optional - for testing)
INSERT INTO users (first_name, last_name, age, gender, phone, email, username, password) 
VALUES 
('John', 'Doe', 30, 'Male', '9876543210', 'john.doe@email.com', 'john123', 'password123'),
('Jane', 'Smith', 25, 'Female', '9876543211', 'jane.smith@email.com', 'jane123', 'password123');

-- Insert sample bookings (Optional - for testing)
INSERT INTO bookings (user_id, from_location, to_location, travel_date, travel_time, status)
VALUES
(1, 'Chennai', 'Coimbatore', '2025-11-01', '09:00 AM', 'CONFIRMED'),
(1, 'Madurai', 'Trichy', '2025-11-05', '03:00 PM', 'CONFIRMED'),
(2, 'Salem', 'Chennai', '2025-11-10', '06:00 AM', 'CONFIRMED');

-- Query to view all users
-- SELECT * FROM users;

-- Query to view all bookings
-- SELECT * FROM bookings;

-- Query to view bookings with user details
-- SELECT b.*, u.first_name, u.last_name, u.phone, u.email 
-- FROM bookings b 
-- JOIN users u ON b.user_id = u.user_id;

-- Query to delete all data (use carefully!)
-- DELETE FROM bookings;
-- DELETE FROM users;

-- Query to drop tables (use carefully!)
-- DROP TABLE IF EXISTS bookings;
-- DROP TABLE IF EXISTS users;

-- Query to drop database (use carefully!)
-- DROP DATABASE IF EXISTS bus_reservation;