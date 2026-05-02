// File: src/com/busreservation/panels/DashboardPanel.java
package com.busreservation.panels;

import javax.swing.*;
import java.awt.*;
import com.busreservation.BusReservationSystem;

public class DashboardPanel extends JPanel {
    private BusReservationSystem mainFrame;
    private int userId;
    private CardLayout contentLayout;
    private JPanel contentPanel;

    public DashboardPanel(BusReservationSystem mainFrame, int userId) {
        this.mainFrame = mainFrame;
        this.userId = userId;
        
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Split Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);

        // Left Menu Panel
        JPanel menuPanel = createMenuPanel();
        
        // Content Panel with CardLayout
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        
        // Add all content panels
        contentPanel.add(new BookingPanel(userId), "Booking Tickets");
        contentPanel.add(new MyTripsPanel(userId), "My Trips");
        contentPanel.add(new CancellationPanel(userId), "Ticket Cancellation");
        contentPanel.add(new MyTicketsPanel(userId), "My Tickets");

        splitPane.setLeftComponent(menuPanel);
        splitPane.setRightComponent(contentPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(51, 51, 51));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        String[] menuItems = {"Booking Tickets", "My Trips", "Ticket Cancellation", "My Tickets"};

        for (String menuItem : menuItems) {
            JButton menuButton = new JButton(menuItem);
            menuButton.setMaximumSize(new Dimension(180, 40));
            menuButton.setBackground(new Color(0, 102, 204));
            menuButton.setForeground(Color.WHITE);
            menuButton.setFocusPainted(false);
            menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            menuButton.addActionListener(e -> {
                contentLayout.show(contentPanel, menuItem);
            });
            
            menuPanel.add(menuButton);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Logout Button
        menuPanel.add(Box.createVerticalGlue());
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setMaximumSize(new Dimension(180, 40));
        logoutBtn.setBackground(new Color(204, 0, 0));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", 
                "Logout", 
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                mainFrame.showPanel("LOGIN");
            }
        });
        
        menuPanel.add(logoutBtn);

        return menuPanel;
    }
}