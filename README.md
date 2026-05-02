# Bus Reservation System - India 🚌

A modern, desktop-based Bus Reservation System built using **Java Swing** and **MongoDB**. This application provides a comprehensive platform for users to search, book, and manage bus tickets across 50+ major Indian cities.

## 🚀 Features

- **User Authentication**: Secure Signup and Login system with data validation.
- **Dynamic Bus Search**: Search for buses between 50 major Indian cities including Mumbai, Delhi, Bangalore, Chennai, and more.
- **Interactive Seat Selection**: Visual seat map for selecting preferred seats (with real-time occupancy check).
- **Comprehensive Booking**: Calculates fare based on distance and bus type (AC, Sleeper, Volvo, etc.).
- **Ticket Management**:
    - **My Tickets**: View active and past bookings.
    - **Cancellation**: Easily cancel confirmed tickets with status updates.
- **Dashboard**: personalized user dashboard showing upcoming trip counts and quick navigation.
- **NoSQL Backend**: Robust data management using MongoDB with a custom auto-incrementing sequence generator for IDs.

## 🛠️ Tech Stack

- **Language**: Java (JDK 8+)
- **UI Framework**: Java Swing (Desktop)
- **Database**: MongoDB (NoSQL)
- **Dependencies**:
    - [MongoDB Java Driver 5.1.0](https://mongodb.github.io/mongo-java-driver/)
    - [JCalendar 1.4](https://toedter.com/jcalendar/) (for date selection)

## 📁 Project Structure

```text
src/com/busreservation/
├── BusReservationSystem.java    # Main Entry Point & Frame Controller
├── DatabaseConnection.java       # MongoDB Connection & ID Generator
├── panels/                       # UI Components (Panels)
│   ├── LoginPanel.java
│   ├── SignupPanel.java
│   ├── DashboardPanel.java
│   ├── BookingPanel.java
│   ├── MyTicketsPanel.java
│   ├── CancellationPanel.java
│   └── MyTripsPanel.java
└── utils/
    └── DatabaseHelper.java       # Data validation & Utility methods
```

## ⚙️ Setup Instructions

### Prerequisites
1. **Java JDK**: Ensure JDK 8 or higher is installed.
2. **MongoDB**: Install MongoDB Community Server and ensure it is running on `localhost:27017`.

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Starmann1/Bus-Reservation-System-India.git
   ```
2. Open the project in your favorite IDE (VS Code, IntelliJ, or Eclipse).
3. Add the JAR files located in the `lib` directory to your project's **Classpath**:
   - `bson-5.1.0.jar`
   - `mongodb-driver-core-5.1.0.jar`
   - `mongodb-driver-sync-5.1.0.jar`
   - `jcalendar-1.4.jar` (and its dependencies in the jcalendar lib folder)

### Database Configuration
The application automatically creates the database and required collections upon first run.
- **Database Name**: `java_mini_project`
- **Collections**: `users`, `bookings`, `counters` (for IDs)

## 🚦 How to Run
Run the `BusReservationSystem.java` file:
```bash
java -cp "bin;lib/*" com.busreservation.BusReservationSystem
```
*(Note: Adjust the classpath separator `;` for Windows or `:` for Linux/Mac)*

## 📸 City Coverage
The system supports travel between 50 major cities including:
- **North**: Delhi, Jaipur, Lucknow, Amritsar, Chandigarh, etc.
- **South**: Bangalore, Chennai, Hyderabad, Madurai, Vijayawada, etc.
- **West**: Mumbai, Pune, Ahmedabad, Surat, Rajkot, etc.
- **East**: Kolkata, Patna, Ranchi, Guwahati, etc.

## 📄 License
This project is for educational purposes as a Java Mini Project.
