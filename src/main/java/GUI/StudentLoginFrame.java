package GUI;

import javax.swing.*;
import java.awt.*;
import java.io.*;

// Student Login Interface
public class StudentLoginFrame {
    
    // Path to student data file (students.data in current directory)
    private static final String STUDENT_DATA_FILE_PATH = "students.data";
    
    // Program entry point
    public static void main(String[] args) {
        showStudentLogin(null);
    }
    
    // Display the student login interface
    public static void showStudentLogin(JFrame parentFrame) {
        
        // Create login window
        JFrame studentFrame = new JFrame("Student Login");
        // Critical setting: exit the entire program when window is closed
        studentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        studentFrame.setSize(500, 400);
        
        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Create title label
        JLabel titleLabel = new JLabel("Student Login", SwingConstants.CENTER);
        Font titleFont = new Font("Times New Roman", Font.BOLD, 36);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.PINK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
        // Add title label to the top of main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create form panel
        JPanel formPanel = new JPanel();
        // Grid layout
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        // Create layout constraint object
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Set fonts
        Font labelFont = new Font("Times New Roman", Font.PLAIN, 18);
        Font fieldFont = new Font("Times New Roman", Font.PLAIN, 18);
        
        // Email input row
        // Create email label
        JLabel accountLabel = new JLabel("Email Account:");
        accountLabel.setFont(labelFont);
        accountLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        // Right alignment
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(accountLabel, gbc);
        
        // Create email input field
        JTextField accountField = new JTextField(15);
        accountField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 0;
        // Left alignment
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(accountField, gbc);
        
        // Password input row
        // Create password label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(passwordLabel, gbc);
        
        // Create password input field
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);
        
        // Add form panel to the center of main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        // Flow layout, components arranged left to right, centered
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        buttonPanel.setBackground(Color.WHITE);
        
        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setBackground(Color.ORANGE);
        loginButton.setForeground(Color.WHITE);
        
        // Login button click event: validate email and password
        loginButton.addActionListener(e -> {
            String email = accountField.getText();
            String password = new String(passwordField.getPassword());
            
            // Validate that inputs are not empty
            if (email.isEmpty() || password.isEmpty()) {
                // Show error dialog
                JOptionPane.showMessageDialog(studentFrame, 
                    "Please enter both email and password!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validate user from data file
            String validationResult = validateUserFromDataFile(email, password);
            
            // Process validation result
            if (validationResult.equals("SUCCESS")) {
                // Login successful, close current login window
                studentFrame.dispose();
                // Open student main interface, passing the logged-in email
                StudentMainFrame.showStudentMain(email);
            } else if (validationResult.equals("PASSWORD_ERROR")) {
                // Password error: show error message and clear password field
                JOptionPane.showMessageDialog(studentFrame, 
                    "Incorrect password!\nPlease try again.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");  // Clear password field
            } else if (validationResult.equals("EMAIL_NOT_FOUND")) {
                // Email not registered: prompt user to contact administrator
                JOptionPane.showMessageDialog(studentFrame, 
                    "Email account not found!\n\nPlease contact the administrator to register.",
                    "Account Not Found", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Other errors
                JOptionPane.showMessageDialog(studentFrame, 
                    "Login failed!\nPlease contact the administrator.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Add login button to button panel
        buttonPanel.add(loginButton);
        
        // Add button panel to the bottom of main panel
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set main panel as the content pane of the window
        studentFrame.setContentPane(mainPanel);
        // Center the window on screen
        studentFrame.setLocationRelativeTo(null);
        // Make the window visible
        studentFrame.setVisible(true);
    }
    
    // Validate user email and password from students.data file
    private static String validateUserFromDataFile(String email, String password) {
        BufferedReader reader = null;
        
        try {
            // Create file object
            File dataFile = new File(STUDENT_DATA_FILE_PATH);
            
            // Check if file exists
            if (!dataFile.exists()) {
                System.err.println("Data file does not exist: " + dataFile.getAbsolutePath());
                return "EMAIL_NOT_FOUND";
            }
            
            // Create file reader
            reader = new BufferedReader(new FileReader(dataFile));
            String line;
            
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                
                // Split by vertical bar
                String[] parts = line.split("\\|");
                if (parts.length == 0) {
                    continue;
                }
                
                // Get basic student information
                String[] studentInfo = parts[0].split(",");
                // Ensure at least 4 fields (student ID, name, email, password)
                if (studentInfo.length >= 4) {
                    String Name = studentInfo[1];   // Student name (column 2)
                    String Email = studentInfo[2];      // Email (column 3)
                    String Password = studentInfo[3];   // Password (column 4)
                    
                    // Match email (case-insensitive)
                    if (Email.equalsIgnoreCase(email)) {
                        // Verify password
                        if (Password.equals(password)) {
                            // Login successful
                            System.out.println("Login successful: " + Name + " (" + email + ")");
                            return "SUCCESS";
                        } else {
                            // Password error
                            System.out.println("Password error for: " + email);
                            return "PASSWORD_ERROR";
                        }
                    }
                }
            }
            
            // No matching email found after reading all lines
            System.out.println("Email not found: " + email);
            return "EMAIL_NOT_FOUND";
            
        } catch (FileNotFoundException e) {
            // File not found exception
            System.err.println("Data file not found: " + STUDENT_DATA_FILE_PATH);
            return "EMAIL_NOT_FOUND";
        } catch (IOException e) {
            // IO exception
            System.err.println("Error reading data file: " + e.getMessage());
            e.printStackTrace();
            return "FILE_ERROR";
        } finally {
            // Ensure the file reader is closed to release resources
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}