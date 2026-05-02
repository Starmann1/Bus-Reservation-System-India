// File: src/com/busreservation/utils/DatabaseHelper.java
package com.busreservation.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import com.busreservation.DatabaseConnection;

public class DatabaseHelper {
    
    /**
     * Validates if a username already exists in the database
     */
    public static boolean usernameExists(String username) {
        try {
            MongoCollection<Document> users = DatabaseConnection.getDatabase().getCollection("users");
            return users.countDocuments(Filters.eq("username", username)) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Gets user details by user ID
     */
    public static Document getUserById(int userId) {
        try {
            MongoCollection<Document> users = DatabaseConnection.getDatabase().getCollection("users");
            return users.find(Filters.eq("user_id", userId)).first();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Gets booking details by booking ID
     */
    public static Document getBookingById(int bookingId) {
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            return bookings.find(Filters.eq("booking_id", bookingId)).first();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Gets all bookings for a user
     */
    public static Iterable<Document> getUserBookings(int userId) {
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            return bookings.find(Filters.eq("user_id", userId)).sort(new Document("travel_date", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Gets count of upcoming bookings for a user
     */
    public static int getUpcomingBookingsCount(int userId) {
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            java.time.LocalDate today = java.time.LocalDate.now();
            return (int) bookings.countDocuments(Filters.and(
                Filters.eq("user_id", userId),
                Filters.eq("status", "CONFIRMED"),
                Filters.gte("travel_date", today.toString())
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    /**
     * Validates phone number format
     */
    public static boolean isValidPhone(String phone) {
        // Accepts 10 digit phone numbers
        String phoneRegex = "^[0-9]{10}$";
        return phone.matches(phoneRegex);
    }
    
    /**
     * Validates age
     */
    public static boolean isValidAge(int age) {
        return age >= 1 && age <= 120;
    }
    
    /**
     * Tests database connection
     */
    public static boolean testConnection() {
        try {
            return DatabaseConnection.getDatabase() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}