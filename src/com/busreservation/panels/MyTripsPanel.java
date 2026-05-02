// File: src/com/busreservation/panels/MyTripsPanel.java
package com.busreservation.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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

        // Custom Tab Panel (Buttons)
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tabHeader.setOpaque(false);
        JButton upcomingBtn = new JButton("Upcoming Bookings");
        JButton previousBtn = new JButton("Previous Bookings");
        
        styleTabButton(upcomingBtn, true);
        styleTabButton(previousBtn, false);
        
        tabHeader.add(upcomingBtn);
        tabHeader.add(previousBtn);

        // Content Panel with CardLayout
        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Upcoming Bookings Tab
        upcomingModel = new DefaultTableModel(
            new String[]{"Booking ID", "From", "To", "Date", "Bus Number", "Seat", "Fare"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        upcomingTable = new JTable(upcomingModel);
        setupTransparentTable(upcomingTable);
        
        JScrollPane upcomingScroll = new JScrollPane(upcomingTable);
        setupTransparentScrollPane(upcomingScroll);

        // Previous Bookings Tab
        previousModel = new DefaultTableModel(
            new String[]{"Booking ID", "From", "To", "Date", "Bus Number", "Seat", "Fare"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        previousTable = new JTable(previousModel);
        setupTransparentTable(previousTable);
        
        JScrollPane previousScroll = new JScrollPane(previousTable);
        setupTransparentScrollPane(previousScroll);

        contentPanel.add(upcomingScroll, "UPCOMING");
        contentPanel.add(previousScroll, "PREVIOUS");

        // Main Container for Center
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(tabHeader, BorderLayout.NORTH);
        centerPanel.add(contentPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Refresh Button Panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(0, 102, 204));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners
        upcomingBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "UPCOMING");
            styleTabButton(upcomingBtn, true);
            styleTabButton(previousBtn, false);
        });
        
        previousBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "PREVIOUS");
            styleTabButton(upcomingBtn, false);
            styleTabButton(previousBtn, true);
        });

        refreshBtn.addActionListener(e -> loadTrips());
    }

    private void styleTabButton(JButton btn, boolean active) {
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        if (active) {
            btn.setBackground(new Color(0, 102, 204));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(255, 255, 255, 100));
            btn.setForeground(Color.BLACK);
        }
    }

    private void setupTransparentTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setOpaque(false);
        table.setBackground(new Color(0, 0, 0, 0));
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(new Color(150, 150, 150, 150)); // More visible grid lines
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    ((JComponent)c).setOpaque(false);
                    c.setBackground(new Color(0, 0, 0, 0));
                }
                return c;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void setupTransparentScrollPane(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        // Adding a subtle border to define the table area
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150, 100), 1));
        scrollPane.setBackground(new Color(0, 0, 0, 0));
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