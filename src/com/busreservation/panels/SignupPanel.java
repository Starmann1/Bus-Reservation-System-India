// File: src/com/busreservation/panels/SignupPanel.java
package com.busreservation.panels;

import javax.swing.*;
import java.awt.*;
import com.busreservation.BusReservationSystem;
import com.busreservation.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class SignupPanel extends JPanel {
    private BusReservationSystem mainFrame;
    private JTextField firstNameField, lastNameField, ageField, phoneField, emailField, usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> genderBox;

    public SignupPanel(BusReservationSystem mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 248, 255));
        
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // First Name
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("First Name:"), gbc);
        firstNameField = new JTextField(20);
        gbc.gridx = 1;
        add(firstNameField, gbc);

        // Last Name
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Last Name:"), gbc);
        lastNameField = new JTextField(20);
        gbc.gridx = 1;
        add(lastNameField, gbc);

        // Age
        gbc.gridy = 3;
        gbc.gridx = 0;
        add(new JLabel("Age:"), gbc);
        ageField = new JTextField(20);
        gbc.gridx = 1;
        add(ageField, gbc);

        // Gender
        gbc.gridy = 4;
        gbc.gridx = 0;
        add(new JLabel("Gender:"), gbc);
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        gbc.gridx = 1;
        add(genderBox, gbc);

        // Phone Number
        gbc.gridy = 5;
        gbc.gridx = 0;
        add(new JLabel("Phone Number:"), gbc);
        phoneField = new JTextField(20);
        gbc.gridx = 1;
        add(phoneField, gbc);

        // Email ID
        gbc.gridy = 6;
        gbc.gridx = 0;
        add(new JLabel("Email ID:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        add(emailField, gbc);

        // Username
        gbc.gridy = 7;
        gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Password
        gbc.gridy = 8;
        gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Register Button
        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(0, 153, 76));
        registerBtn.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        add(registerBtn, gbc);

        // Back Button
        JButton backBtn = new JButton("Back to Login");
        backBtn.setBackground(new Color(102, 102, 102));
        backBtn.setForeground(Color.WHITE);
        gbc.gridy = 10;
        add(backBtn, gbc);

        // Action Listeners
        registerBtn.addActionListener(e -> handleRegistration());
        backBtn.addActionListener(e -> mainFrame.showPanel("LOGIN"));
    }

    private void handleRegistration() {
        try {
            // Validation
            if (firstNameField.getText().trim().isEmpty() || 
                lastNameField.getText().trim().isEmpty() ||
                usernameField.getText().trim().isEmpty() ||
                passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields!");
                return;
            }

            int age = Integer.parseInt(ageField.getText().trim());
            if (age < 1 || age > 120) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age!");
                return;
            }

            // Check if username already exists
            MongoCollection<Document> users = DatabaseConnection.getDatabase().getCollection("users");
            if (users.countDocuments(Filters.eq("username", usernameField.getText().trim())) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
                return;
            }

            // Insert into database
            int nextId = DatabaseConnection.getNextSequence("userid");
            Document user = new Document("user_id", nextId)
                    .append("first_name", firstNameField.getText().trim())
                    .append("last_name", lastNameField.getText().trim())
                    .append("age", age)
                    .append("gender", (String) genderBox.getSelectedItem())
                    .append("phone", phoneField.getText().trim())
                    .append("email", emailField.getText().trim())
                    .append("username", usernameField.getText().trim())
                    .append("password", new String(passwordField.getPassword()))
                    .append("created_at", new java.util.Date());
            
            users.insertOne(user);
            
            JOptionPane.showMessageDialog(this, "Registration Successful!");
            clearFields();
            mainFrame.showPanel("LOGIN");
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid age!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        ageField.setText("");
        phoneField.setText("");
        emailField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        genderBox.setSelectedIndex(0);
    }
}