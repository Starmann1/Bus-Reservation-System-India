// File: src/com/busreservation/panels/MyTripsPanel.java
package com.busreservation.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import com.busreservation.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class MyTripsPanel extends JPanel {
    private int userId;
    private DefaultTableModel upcomingModel;
    private DefaultTableModel previousModel;
    private JTable upcomingTable;
    private JTable previousTable;

    private Image backgroundImage;

    public MyTripsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/resources/images/trips_bg.jpg")).getImage();
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }
        
        initComponents();
        loadTrips();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            
            // Add a lighter semi-transparent overlay
            g.setColor(new Color(255, 255, 255, 120));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("My Trips", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);

        // Upcoming Bookings Tab
        upcomingModel = new DefaultTableModel(
            new String[]{"Booking ID", "From", "To", "Date", "Bus Number", "Seat", "Fare"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        upcomingTable = new JTable(upcomingModel);
        upcomingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        upcomingTable.setRowHeight(25);
        upcomingTable.setOpaque(false);
        upcomingTable.setBackground(new Color(0, 0, 0, 0));
        
        JScrollPane upcomingScroll = new JScrollPane(upcomingTable);
        upcomingScroll.setOpaque(false);
        upcomingScroll.getViewport().setOpaque(false);
        upcomingScroll.setBorder(BorderFactory.createEmptyBorder());

        // Previous Bookings Tab
        previousModel = new DefaultTableModel(
            new String[]{"Booking ID", "From", "To", "Date", "Bus Number", "Seat", "Fare"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        previousTable = new JTable(previousModel);
        previousTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        previousTable.setRowHeight(25);
        previousTable.setOpaque(false);
        previousTable.setBackground(new Color(0, 0, 0, 0));
        
        JScrollPane previousScroll = new JScrollPane(previousTable);
        previousScroll.setOpaque(false);
        previousScroll.getViewport().setOpaque(false);
        previousScroll.setBorder(BorderFactory.createEmptyBorder());

        tabbedPane.addTab("Upcoming Bookings", upcomingScroll);
        tabbedPane.addTab("Previous Bookings", previousScroll);

        add(tabbedPane, BorderLayout.CENTER);

        // Refresh Button Panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(0, 102, 204));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadTrips());
    }

    private void loadTrips() {
        upcomingModel.setRowCount(0);
        previousModel.setRowCount(0);
        
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            java.time.LocalDate today = java.time.LocalDate.now();
            String todayStr = today.toString();
            
            for (Document doc : bookings.find(Filters.and(
                Filters.eq("user_id", userId),
                Filters.eq("status", "CONFIRMED")
            )).sort(new Document("travel_date", -1))) {
                
                String travelDateStr = doc.getString("travel_date");
                Object[] row = {
                    doc.getInteger("booking_id"),
                    doc.getString("from_location"),
                    doc.getString("to_location"),
                    travelDateStr,
                    doc.getString("bus_number"),
                    doc.getString("seat_number"),
                    "₹" + doc.getInteger("fare")
                };
                
                if (travelDateStr.compareTo(todayStr) >= 0) {
                    upcomingModel.addRow(row);
                } else {
                    previousModel.addRow(row);
                }
            }
            
            if (upcomingModel.getRowCount() == 0) {
                upcomingModel.addRow(new Object[]{"No upcoming trips", "", "", "", "", "", ""});
            }
            
            if (previousModel.getRowCount() == 0) {
                previousModel.addRow(new Object[]{"No previous trips", "", "", "", "", "", ""});
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading trips: " + e.getMessage());
            e.printStackTrace();
        }
    }
}