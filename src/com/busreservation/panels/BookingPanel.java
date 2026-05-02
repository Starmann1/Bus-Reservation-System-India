// File: src/com/busreservation/panels/BookingPanel.java
package com.busreservation.panels;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import com.toedter.calendar.JDateChooser;
import com.busreservation.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class BookingPanel extends JPanel {
    private int userId;
    private JComboBox<String> fromBox, toBox;
    private JDateChooser dateChooser;
    private JPanel busListPanel;
    private Map<String, Integer> distanceMap;
    private String[] travelNames = {
        "KPN Travels", "SRS Travels", "VRL Travels", "Parveen Travels",
        "Jabbar Travels", "Orange Travels", "National Travels", "Red Bus Travels",
        "Green Line Travels", "Royal Travels", "Metro Travels", "Express Travels"
    };
    
    private String[] busTypes = {
        "AC Sleeper", "Non-AC Sleeper", "AC Seater", "Non-AC Seater",
        "Volvo AC", "Multi-Axle AC", "Semi Sleeper"
    };

    public BookingPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        initDistanceMap();
        initComponents();
    }

    private void initDistanceMap() {
        distanceMap = new HashMap<>();
        // Distance in KM between cities (approximate)
        distanceMap.put("Mumbai-Delhi", 1415);
        distanceMap.put("Mumbai-Bangalore", 984);
        distanceMap.put("Mumbai-Pune", 148);
        distanceMap.put("Delhi-Bangalore", 2174);
        distanceMap.put("Delhi-Jaipur", 281);
        distanceMap.put("Bangalore-Hyderabad", 569);
        distanceMap.put("Chennai-Bangalore", 346);
        distanceMap.put("Chennai-Hyderabad", 627);
        distanceMap.put("Kolkata-Delhi", 1554);
        distanceMap.put("Ahmedabad-Mumbai", 524);
        // Add more as needed or use random for others
    }

    private int getDistance(String from, String to) {
        String key1 = from + "-" + to;
        String key2 = to + "-" + from;
        
        if (distanceMap.containsKey(key1)) {
            return distanceMap.get(key1);
        } else if (distanceMap.containsKey(key2)) {
            return distanceMap.get(key2);
        }
        // Default random distance if not in map (between 200 and 1500 km)
        return 200 + new Random().nextInt(1300);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Book Your Ticket");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        topPanel.add(titleLabel, gbc);

        // 50 Indian Cities
        String[] cities = {
            "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Ahmedabad", "Chennai", "Kolkata", "Surat", "Pune", "Jaipur",
            "Lucknow", "Kanpur", "Nagpur", "Indore", "Thane", "Bhopal", "Visakhapatnam", "Pimpri-Chinchwad", "Patna", "Vadodara",
            "Ghaziabad", "Ludhiana", "Agra", "Nashik", "Faridabad", "Meerut", "Rajkot", "Kalyan-Dombivli", "Vasai-Virar", "Varanasi",
            "Srinagar", "Aurangabad", "Dhanbad", "Amritsar", "Navi Mumbai", "Prayagraj", "Howrah", "Ranchi", "Jabalpur", "Gwalior",
            "Vijayawada", "Jodhpur", "Madurai", "Raipur", "Kota", "Guwahati", "Chandigarh", "Solapur", "Hubballi-Dharwad", "Bareilly"
        };
        Arrays.sort(cities);

        gbc.gridwidth = 1;
        
        // From
        gbc.gridy = 1;
        gbc.gridx = 0;
        topPanel.add(new JLabel("From:"), gbc);
        fromBox = new JComboBox<>(cities);
        gbc.gridx = 1;
        topPanel.add(fromBox, gbc);

        // To
        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(new JLabel("To:"), gbc);
        toBox = new JComboBox<>(cities);
        gbc.gridx = 1;
        topPanel.add(toBox, gbc);

        // Departure Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        topPanel.add(new JLabel("Departure Date:"), gbc);
        dateChooser = new JDateChooser();
        dateChooser.setMinSelectableDate(new Date());
        gbc.gridx = 1;
        topPanel.add(dateChooser, gbc);

        // Show Buses Button
        JButton showBusesBtn = new JButton("Show Available Buses");
        showBusesBtn.setBackground(new Color(0, 102, 204));
        showBusesBtn.setForeground(Color.WHITE);
        showBusesBtn.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        topPanel.add(showBusesBtn, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Bus List Panel
        busListPanel = new JPanel();
        busListPanel.setLayout(new BoxLayout(busListPanel, BoxLayout.Y_AXIS));
        busListPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(busListPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Action Listener
        showBusesBtn.addActionListener(event -> showAvailableBuses());
    }

    private void showAvailableBuses() {
        Date selectedDate = (Date) dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a date!");
            return;
        }

        String from = (String) fromBox.getSelectedItem();
        String to = (String) toBox.getSelectedItem();
        
        if (from.equals(to)) {
            JOptionPane.showMessageDialog(this, "Source and destination cannot be the same!");
            return;
        }

        busListPanel.removeAll();
        int distance = getDistance(from, to);
        int fare = distance * 11; // ₹11 per km

        // Generate 3 random buses
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            String travelName = travelNames[random.nextInt(travelNames.length)];
            String busType = busTypes[random.nextInt(busTypes.length)];
            String busNumber = "IN" + (10 + random.nextInt(90)) + "AB" + (1000 + random.nextInt(9000));
            String departureTime = String.format("%02d:%02d %s", 
                random.nextInt(12) + 1, 
                random.nextInt(60), 
                random.nextBoolean() ? "AM" : "PM");
            
            JPanel busCard = createBusCard(travelName, busType, busNumber, 
                departureTime, distance, fare, from, to, selectedDate);
            busListPanel.add(busCard);
            busListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        busListPanel.revalidate();
        busListPanel.repaint();
    }

    private JPanel createBusCard(String travelName, String busType, String busNumber,
                                  String departureTime, int distance, int fare,
                                  String from, String to, Date date) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Bus Details Panel
        JPanel detailsPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        detailsPanel.setBackground(Color.WHITE);
        
        detailsPanel.add(createBoldLabel("Travel Name:"));
        detailsPanel.add(createNormalLabel(travelName));
        
        detailsPanel.add(createBoldLabel("Bus Type:"));
        detailsPanel.add(createNormalLabel(busType));
        
        detailsPanel.add(createBoldLabel("Bus Number:"));
        detailsPanel.add(createNormalLabel(busNumber));
        
        detailsPanel.add(createBoldLabel("Departure Time:"));
        detailsPanel.add(createNormalLabel(departureTime));

        card.add(detailsPanel, BorderLayout.CENTER);

        // Right Panel (Fare and Button)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        
        JPanel farePanel = new JPanel();
        farePanel.setLayout(new BoxLayout(farePanel, BoxLayout.Y_AXIS));
        farePanel.setBackground(Color.WHITE);
        
        JLabel distanceLabel = new JLabel(distance + " km");
        distanceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        distanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel fareLabel = new JLabel("₹" + fare);
        fareLabel.setFont(new Font("Arial", Font.BOLD, 18));
        fareLabel.setForeground(new Color(0, 153, 76));
        fareLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        farePanel.add(distanceLabel);
        farePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        farePanel.add(fareLabel);
        
        JButton selectBtn = new JButton("Select Seat");
        selectBtn.setBackground(new Color(0, 153, 76));
        selectBtn.setForeground(Color.WHITE);
        selectBtn.setFont(new Font("Arial", Font.BOLD, 12));
        selectBtn.addActionListener(event -> 
            showSeatSelection(travelName, busType, busNumber, departureTime, 
                            distance, fare, from, to, date));
        
        rightPanel.add(farePanel, BorderLayout.CENTER);
        rightPanel.add(selectBtn, BorderLayout.SOUTH);
        
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private void showSeatSelection(String travelName, String busType, String busNumber,
                                   String departureTime, int distance, int fare,
                                   String from, String to, Date date) {
        JDialog seatDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Select Your Seat", true);
        seatDialog.setLayout(new BorderLayout());
        seatDialog.setSize(600, 700);
        seatDialog.setLocationRelativeTo(this);

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(4, 1));
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLbl = new JLabel(travelName + " - " + busType);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);
        
        JLabel routeLbl = new JLabel(from + " → " + to);
        routeLbl.setFont(new Font("Arial", Font.PLAIN, 14));
        routeLbl.setForeground(Color.WHITE);
        
        JLabel busNumLbl = new JLabel("Bus: " + busNumber + " | Time: " + departureTime);
        busNumLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        busNumLbl.setForeground(Color.WHITE);
        
        JLabel fareLbl = new JLabel("Fare: ₹" + fare + " (" + distance + " km)");
        fareLbl.setFont(new Font("Arial", Font.BOLD, 14));
        fareLbl.setForeground(new Color(255, 255, 153));
        
        headerPanel.add(titleLbl);
        headerPanel.add(routeLbl);
        headerPanel.add(busNumLbl);
        headerPanel.add(fareLbl);
        
        seatDialog.add(headerPanel, BorderLayout.NORTH);

        // Seat Layout Panel
        JPanel seatPanel = new JPanel(new GridBagLayout());
        seatPanel.setBackground(Color.WHITE);
        seatPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Get occupied seats
        Set<String> occupiedSeats = getOccupiedSeats(busNumber, date);
        final String[] selectedSeat = {null};

        // Legend
        JPanel legendPanel = new JPanel(new FlowLayout());
        legendPanel.setBackground(Color.WHITE);
        legendPanel.add(createLegendItem("Available", new Color(200, 230, 200)));
        legendPanel.add(createLegendItem("Occupied", new Color(200, 200, 200)));
        legendPanel.add(createLegendItem("Selected", new Color(100, 200, 100)));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        seatPanel.add(legendPanel, gbc);

        // Driver seat
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel driverLabel = new JLabel("🚗 Driver");
        driverLabel.setFont(new Font("Arial", Font.BOLD, 12));
        seatPanel.add(driverLabel, gbc);

        // Create 40 seats (10 rows x 4 columns)
        int seatNumber = 1;
        List<JButton> seatButtons = new ArrayList<>();
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 4; col++) {
                if (col == 1) continue; // Aisle space
                
                String seat = String.valueOf(seatNumber);
                JButton seatBtn = new JButton(seat);
                seatBtn.setPreferredSize(new Dimension(60, 60));
                seatBtn.setFont(new Font("Arial", Font.BOLD, 14));
                
                if (occupiedSeats.contains(seat)) {
                    seatBtn.setBackground(new Color(200, 200, 200));
                    seatBtn.setEnabled(false);
                    seatBtn.setText(seat + "\n❌");
                } else {
                    seatBtn.setBackground(new Color(200, 230, 200));
                    seatBtn.setForeground(Color.BLACK);
                }
                
                seatBtn.addActionListener(e -> {
                    // Deselect all other seats
                    for (JButton btn : seatButtons) {
                        if (btn.isEnabled() && !btn.getText().equals(seatBtn.getText())) {
                            btn.setBackground(new Color(200, 230, 200));
                        }
                    }
                    // Select this seat
                    seatBtn.setBackground(new Color(100, 200, 100));
                    selectedSeat[0] = seatBtn.getText().replaceAll("[^0-9]", "");
                });
                
                seatButtons.add(seatBtn);
                
                gbc.gridx = col < 2 ? col : col + 1;
                gbc.gridy = row + 2;
                seatPanel.add(seatBtn, gbc);
                
                seatNumber++;
            }
        }

        JScrollPane scrollPane = new JScrollPane(seatPanel);
        seatDialog.add(scrollPane, BorderLayout.CENTER);

        // Confirm Button
        JButton confirmBtn = new JButton("Confirm Booking");
        confirmBtn.setBackground(new Color(0, 153, 76));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 14));
        confirmBtn.addActionListener(event -> {
            if (selectedSeat[0] == null) {
                JOptionPane.showMessageDialog(seatDialog, "Please select a seat!");
                return;
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(date);
            
            if (bookTicket(from, to, dateStr, departureTime, travelName, busType, 
                          busNumber, selectedSeat[0], fare, distance)) {
                JOptionPane.showMessageDialog(seatDialog, 
                    "Ticket Booked Successfully!\n\n" +
                    "Seat Number: " + selectedSeat[0] + "\n" +
                    "Bus: " + busNumber + "\n" +
                    "Fare: ₹" + fare,
                    "Booking Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);
                seatDialog.dispose();
                busListPanel.removeAll();
                busListPanel.revalidate();
                busListPanel.repaint();
            }
        });

        seatDialog.add(confirmBtn, BorderLayout.SOUTH);
        seatDialog.setVisible(true);
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        
        JLabel colorBox = new JLabel("   ");
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        panel.add(colorBox);
        panel.add(new JLabel(text));
        
        return panel;
    }

    private Set<String> getOccupiedSeats(String busNumber, Date date) {
        Set<String> occupied = new HashSet<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(date);
            
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            for (Document doc : bookings.find(Filters.and(
                Filters.eq("bus_number", busNumber),
                Filters.eq("travel_date", dateStr),
                Filters.eq("status", "CONFIRMED")
            ))) {
                occupied.add(doc.getString("seat_number"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return occupied;
    }

    private boolean bookTicket(String from, String to, String date, String time,
                                String travelName, String busType, String busNumber,
                                String seatNumber, int fare, int distance) {
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            int nextId = DatabaseConnection.getNextSequence("bookingid");
            Document booking = new Document("booking_id", nextId)
                    .append("user_id", userId)
                    .append("from_location", from)
                    .append("to_location", to)
                    .append("travel_date", date)
                    .append("travel_time", time)
                    .append("travel_name", travelName)
                    .append("bus_type", busType)
                    .append("bus_number", busNumber)
                    .append("seat_number", seatNumber)
                    .append("fare", fare)
                    .append("distance", distance)
                    .append("status", "CONFIRMED")
                    .append("booking_date", new java.util.Date());
            
            bookings.insertOne(booking);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Booking Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        return label;
    }

    private JLabel createNormalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        return label;
    }
}