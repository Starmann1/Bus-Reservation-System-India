// File: src/com/busreservation/panels/LoginPanel.java
package com.busreservation.panels;

import javax.swing.*;
import java.awt.*;
import com.busreservation.BusReservationSystem;
import com.busreservation.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class LoginPanel extends JPanel {
    private BusReservationSystem mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private Image backgroundImage;

    public LoginPanel(BusReservationSystem mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/resources/images/login_bg.jpg")).getImage();
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }
        
        initComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            
            // Add a semi-transparent overlay to make text more readable
            g.setColor(new Color(255, 255, 255, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Bus Reservation System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Login Button
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(0, 153, 76));
        loginBtn.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(loginBtn, gbc);

        // Signup Button
        JButton signupBtn = new JButton("Create New Account");
        signupBtn.setBackground(new Color(0, 102, 204));
        signupBtn.setForeground(Color.WHITE);
        gbc.gridy = 4;
        add(signupBtn, gbc);

        // Action Listeners
        loginBtn.addActionListener(e -> handleLogin());
        signupBtn.addActionListener(e -> mainFrame.showPanel("SIGNUP"));
        
        // Enter key support
        passwordField.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!");
            return;
        }

        try {
            MongoCollection<Document> users = DatabaseConnection.getDatabase().getCollection("users");
            Document user = users.find(Filters.and(
                Filters.eq("username", username),
                Filters.eq("password", password)
            )).first();

            if (user != null) {
                int userId = user.getInteger("user_id");
                mainFrame.setLoggedInUser(username, userId);
                
                // Clear fields
                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}