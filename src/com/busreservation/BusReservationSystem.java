// File: src/com/busreservation/BusReservationSystem.java
package com.busreservation;

import javax.swing.*;
import com.busreservation.panels.*;
import java.awt.*;

public class BusReservationSystem extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String loggedInUser;
    private int userId;

    public BusReservationSystem() {
        setTitle("Bus Reservation System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add panels
        LoginPanel loginPanel = new LoginPanel(this);
        SignupPanel signupPanel = new SignupPanel(this);
        
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(signupPanel, "SIGNUP");

        add(mainPanel);
        setVisible(true);
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public void setLoggedInUser(String username, int userId) {
        this.loggedInUser = username;
        this.userId = userId;
        
        // Create and add dashboard after successful login
        DashboardPanel dashboardPanel = new DashboardPanel(this, userId);
        mainPanel.add(dashboardPanel, "DASHBOARD");
        cardLayout.show(mainPanel, "DASHBOARD");
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public int getUserId() {
        return userId;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BusReservationSystem());
    }
}