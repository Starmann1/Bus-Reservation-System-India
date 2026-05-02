// File: src/com/busreservation/panels/CancellationPanel.java
package com.busreservation.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.busreservation.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class CancellationPanel extends JPanel {
    private int userId;
    private DefaultTableModel model;
    private JTable table;

    public CancellationPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        initComponents();
        loadUpcomingTickets();
    }

    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("Ticket Cancellation", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(
            new String[]{"Booking ID", "From", "To", "Date", "Bus", "Seat"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton viewBtn = new JButton("View & Cancel Selected Ticket");
        viewBtn.setBackground(new Color(204, 0, 0));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(0, 102, 204));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));

        buttonPanel.add(viewBtn);
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        viewBtn.addActionListener(event -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                // Check if it's the "No tickets" message row
                Object firstCell = model.getValueAt(selectedRow, 0);
                if (firstCell instanceof String && ((String)firstCell).contains("No upcoming tickets")) {
                    JOptionPane.showMessageDialog(this, "No tickets available to cancel!");
                    return;
                }
                int bookingId = (int) firstCell;
                showCancellationDialog(bookingId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a ticket to cancel!");
            }
        });

        refreshBtn.addActionListener(event -> loadUpcomingTickets());
    }

    private void loadUpcomingTickets() {
        model.setRowCount(0);
        
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            java.time.LocalDate today = java.time.LocalDate.now();
            String todayStr = today.toString();
            
            boolean hasTickets = false;
            for (Document doc : bookings.find(Filters.and(
                Filters.eq("user_id", userId),
                Filters.eq("status", "CONFIRMED"),
                Filters.gte("travel_date", todayStr)
            )).sort(new Document("travel_date", 1))) {
                hasTickets = true;
                model.addRow(new Object[]{
                    doc.getInteger("booking_id"),
                    doc.getString("from_location"),
                    doc.getString("to_location"),
                    doc.getString("travel_date"),
                    doc.getString("bus_number"),
                    doc.getString("seat_number")
                });
            }
            
            if (!hasTickets) {
                model.addRow(new Object[]{"No upcoming tickets to cancel", "", "", "", "", ""});
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading tickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showCancellationDialog(int bookingId) {
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            Document booking = bookings.find(Filters.eq("booking_id", bookingId)).first();

            if (booking != null) {
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                    "Ticket Details", true);
                dialog.setLayout(new BorderLayout());
                dialog.setSize(450, 450);
                dialog.setLocationRelativeTo(this);

                // Details Panel
                JPanel detailsPanel = new JPanel(new GridLayout(9, 2, 10, 10));
                detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                detailsPanel.setBackground(Color.WHITE);

                detailsPanel.add(createLabel("Booking ID:"));
                detailsPanel.add(createValueLabel(String.valueOf(bookingId)));
                
                detailsPanel.add(createLabel("From:"));
                detailsPanel.add(createValueLabel(booking.getString("from_location")));
                
                detailsPanel.add(createLabel("To:"));
                detailsPanel.add(createValueLabel(booking.getString("to_location")));
                
                detailsPanel.add(createLabel("Date:"));
                detailsPanel.add(createValueLabel(booking.getString("travel_date")));
                
                detailsPanel.add(createLabel("Time:"));
                detailsPanel.add(createValueLabel(booking.getString("travel_time")));
                
                detailsPanel.add(createLabel("Travel Name:"));
                detailsPanel.add(createValueLabel(booking.getString("travel_name")));
                
                detailsPanel.add(createLabel("Bus Number:"));
                detailsPanel.add(createValueLabel(booking.getString("bus_number")));
                
                detailsPanel.add(createLabel("Seat Number:"));
                detailsPanel.add(createValueLabel(booking.getString("seat_number")));
                
                detailsPanel.add(createLabel("Fare:"));
                detailsPanel.add(createValueLabel("₹" + booking.getInteger("fare")));

                dialog.add(detailsPanel, BorderLayout.CENTER);

                // Button Panel
                JPanel btnPanel = new JPanel(new FlowLayout());
                btnPanel.setBackground(Color.WHITE);

                JButton cancelBtn = new JButton("Cancel This Ticket");
                cancelBtn.setBackground(new Color(204, 0, 0));
                cancelBtn.setForeground(Color.WHITE);
                cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
                
                JButton closeBtn = new JButton("Close");
                closeBtn.setBackground(new Color(102, 102, 102));
                closeBtn.setForeground(Color.WHITE);
                closeBtn.setFont(new Font("Arial", Font.BOLD, 12));

                cancelBtn.addActionListener(event -> {
                    int confirm = JOptionPane.showConfirmDialog(dialog, 
                        "Are you sure you want to cancel this ticket?\nThis action cannot be undone!", 
                        "Confirm Cancellation", 
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        cancelTicket(bookingId);
                        dialog.dispose();
                        loadUpcomingTickets();
                    }
                });

                closeBtn.addActionListener(event -> dialog.dispose());

                btnPanel.add(cancelBtn);
                btnPanel.add(closeBtn);
                dialog.add(btnPanel, BorderLayout.SOUTH);

                dialog.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(51, 51, 51));
        return label;
    }

    private void cancelTicket(int bookingId) {
        try {
            MongoCollection<Document> bookings = DatabaseConnection.getDatabase().getCollection("bookings");
            bookings.updateOne(Filters.eq("booking_id", bookingId), Updates.set("status", "CANCELLED"));
            
            JOptionPane.showMessageDialog(this, 
                "Ticket Cancelled Successfully!\nBooking ID: " + bookingId,
                "Cancellation Confirmed",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cancellation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}