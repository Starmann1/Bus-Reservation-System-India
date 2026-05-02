// File: src/com/busreservation/panels/MyTicketsPanel.java
package com.busreservation.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.busreservation.DatabaseConnection;
import com.busreservation.utils.DatabaseHelper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class MyTicketsPanel extends JPanel {
    private int userId;
    private DefaultTableModel model;
    private JTable table;

    private Image backgroundImage;

    public MyTicketsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/resources/images/tickets_bg.jpg")).getImage();
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }
        
        initComponents();
        loadMyTickets();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            
            // Add a semi-transparent overlay
            g.setColor(new Color(255, 255, 255, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("My Tickets", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Table with more columns
        model = new DefaultTableModel(
            new String[]{"Booking ID", "From", "To", "Date", "Bus", "Seat", "Fare", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        
        // Custom renderer for status column
        table.getColumnModel().getColumn(7).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value.toString());
            label.setOpaque(true);
            
            if (isSelected) {
                label.setBackground(tbl.getSelectionBackground());
                label.setForeground(tbl.getSelectionForeground());
            } else {
                label.setBackground(Color.WHITE);
                if ("CONFIRMED".equals(value.toString())) {
                    label.setForeground(new Color(0, 153, 76));
                } else if ("CANCELLED".equals(value.toString())) {
                    label.setForeground(new Color(204, 0, 0));
                } else {
                    label.setForeground(Color.BLACK);
                }
            }
            
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        });
        
        // Fare column renderer
        table.getColumnModel().getColumn(6).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel("₹" + value.toString());
            label.setOpaque(true);
            
            if (isSelected) {
                label.setBackground(tbl.getSelectionBackground());
                label.setForeground(tbl.getSelectionForeground());
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(0, 102, 204));
            }
            
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(0, 102, 204));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));

        JButton viewBtn = new JButton("View Full Details");
        viewBtn.setBackground(new Color(0, 153, 76));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Arial", Font.BOLD, 12));

        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        refreshBtn.addActionListener(event -> loadMyTickets());
        
        viewBtn.addActionListener(event -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Object val = model.getValueAt(selectedRow, 0);
                if (val instanceof Integer) {
                    showTicketDetails((int) val);
                } else {
                    JOptionPane.showMessageDialog(this, "No valid ticket selected!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a ticket to view details!");
            }
        });
        
        // Double-click to view details
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        Object val = model.getValueAt(row, 0);
                        if (val instanceof Integer) {
                            showTicketDetails((int) val);
                        }
                    }
                }
            }
        });
    }

    private void loadMyTickets() {
        model.setRowCount(0);
        
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            java.time.LocalDate today = java.time.LocalDate.now();
            String todayStr = today.toString();
            
            for (Document doc : bookings.find(Filters.and(
                Filters.eq("user_id", userId),
                Filters.eq("status", "CONFIRMED"),
                Filters.gte("travel_date", todayStr)
            )).sort(new Document("travel_date", 1))) {
                model.addRow(new Object[]{
                    doc.getInteger("booking_id"),
                    doc.getString("from_location"),
                    doc.getString("to_location"),
                    doc.getString("travel_date"),
                    doc.getString("bus_number"),
                    doc.getString("seat_number"),
                    doc.getInteger("fare"),
                    doc.getString("status")
                });
            }
            
            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"No tickets available", "", "", "", "", "", "", ""});
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading tickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showTicketDetails(int bookingId) {
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            Document booking = bookings.find(Filters.eq("booking_id", bookingId)).first();

            if (booking != null) {
                Document user = DatabaseHelper.getUserById(booking.getInteger("user_id"));
                
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                    "Ticket Details", true);
                dialog.setLayout(new BorderLayout());
                dialog.setSize(550, 600);
                dialog.setLocationRelativeTo(this);

                // Header Panel
                JPanel headerPanel = new JPanel();
                headerPanel.setBackground(new Color(0, 102, 204));
                headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
                
                JLabel headerLabel = new JLabel("🎫 Bus Ticket");
                headerLabel.setFont(new Font("Arial", Font.BOLD, 26));
                headerLabel.setForeground(Color.WHITE);
                headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                JLabel bookingIdLabel = new JLabel("Booking ID: " + bookingId);
                bookingIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                bookingIdLabel.setForeground(new Color(200, 220, 255));
                bookingIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                headerPanel.add(headerLabel);
                headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                headerPanel.add(bookingIdLabel);
                dialog.add(headerPanel, BorderLayout.NORTH);

                // Main Details Panel
                JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                mainPanel.setBackground(Color.WHITE);
                mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

                // Passenger Details Section
                mainPanel.add(createSectionTitle("👤 Passenger Details"));
                if (user != null) {
                    mainPanel.add(createDetailRow("Name:", user.getString("first_name") + " " + user.getString("last_name")));
                    mainPanel.add(createDetailRow("Phone:", user.getString("phone")));
                    mainPanel.add(createDetailRow("Email:", user.getString("email")));
                } else {
                    mainPanel.add(createDetailRow("Passenger:", "Unknown"));
                }
                mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

                // Journey Details Section
                mainPanel.add(createSectionTitle("🚌 Journey Details"));
                mainPanel.add(createDetailRow("From:", booking.getString("from_location")));
                mainPanel.add(createDetailRow("To:", booking.getString("to_location")));
                mainPanel.add(createDetailRow("Date:", booking.getString("travel_date")));
                mainPanel.add(createDetailRow("Time:", booking.getString("travel_time")));
                mainPanel.add(createDetailRow("Distance:", booking.getInteger("distance") + " km"));
                mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

                // Bus Details Section
                mainPanel.add(createSectionTitle("🚍 Bus Details"));
                mainPanel.add(createDetailRow("Travel Name:", booking.getString("travel_name")));
                mainPanel.add(createDetailRow("Bus Type:", booking.getString("bus_type")));
                mainPanel.add(createDetailRow("Bus Number:", booking.getString("bus_number")));
                mainPanel.add(createDetailRow("Seat Number:", booking.getString("seat_number")));
                mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

                // Fare Details Section
                mainPanel.add(createSectionTitle("💰 Fare Details"));
                JPanel farePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                farePanel.setBackground(Color.WHITE);
                JLabel fareLabel = new JLabel("Total Fare: ₹" + booking.getInteger("fare"));
                fareLabel.setFont(new Font("Arial", Font.BOLD, 24));
                fareLabel.setForeground(new Color(0, 153, 76));
                farePanel.add(fareLabel);
                mainPanel.add(farePanel);
                
                JLabel rateLabel = new JLabel("(@ ₹11 per km)");
                rateLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                rateLabel.setForeground(new Color(100, 100, 100));
                JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                ratePanel.setBackground(Color.WHITE);
                ratePanel.add(rateLabel);
                mainPanel.add(ratePanel);
                mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                // Status
                JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                statusPanel.setBackground(new Color(200, 255, 200));
                statusPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 153, 76), 2));
                JLabel statusLabel = new JLabel("✓ " + booking.getString("status"));
                statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
                statusLabel.setForeground(new Color(0, 153, 76));
                statusPanel.add(statusLabel);
                mainPanel.add(statusPanel);

                JScrollPane scrollPane = new JScrollPane(mainPanel);
                dialog.add(scrollPane, BorderLayout.CENTER);

                // Button Panel
                JPanel btnPanel = new JPanel(new FlowLayout());
                btnPanel.setBackground(Color.WHITE);
                
                JButton closeBtn = new JButton("Close");
                closeBtn.setBackground(new Color(102, 102, 102));
                closeBtn.setForeground(Color.WHITE);
                closeBtn.setFont(new Font("Arial", Font.BOLD, 12));
                closeBtn.addActionListener(e -> dialog.dispose());
                
                btnPanel.add(closeBtn);
                dialog.add(btnPanel, BorderLayout.SOUTH);

                dialog.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(new Color(0, 102, 204));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 13));
        lblLabel.setPreferredSize(new Dimension(150, 25));
        
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        panel.add(lblLabel, BorderLayout.WEST);
        panel.add(valLabel, BorderLayout.CENTER);
        
        return panel;
    }
}