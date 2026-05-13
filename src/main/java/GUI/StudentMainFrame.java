package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

// Student main interface
public class StudentMainFrame {
    
    // Path to student data file
    private static final String STUDENT_DATA_FILE_PATH = "students.data";
    // Maximum allowed subjects
    private static final int MAX_SUBJECTS = 4;
    // Currently logged in student email
    private static String currentStudentEmail;
    // Currently logged in student ID
    private static String currentStudentId;
    // Currently logged in student name
    private static String currentStudentName;
    // Complete line data for the current student
    private static String currentStudentLineData;
    // Table model for managing course table data
    private static DefaultTableModel tableModel;
    // Course table component displaying student's enrolled courses
    private static JTable subjectTable;
    // Main window reference
    private static JFrame mainFrame;
    // Timer to automatically refresh course list every 1 second
    private static Timer refreshTimer;
    // Record previous subject count to detect data changes
    private static int lastSubjectCount = -1;
    
    // Load basic information of the currently logged in student
    private static void loadCurrentStudentInfo() {
        BufferedReader reader = null;  // Buffered reader for efficient file reading
        
        try {
            // Create file object
            File dataFile = new File(STUDENT_DATA_FILE_PATH);
            
            // Check if file exists
            if (!dataFile.exists()) {
                System.err.println("Data file does not exist: " + dataFile.getAbsolutePath());
                return;
            }
            
            // Create file reader
            reader = new BufferedReader(new FileReader(dataFile));
            String line;  // Store each line read
            
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Split by vertical bar, escape
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    continue;
                }
                
                // Get student basic information part
                String studentInfoStr = mainParts[0];
                // Split student basic information by comma
                String[] studentInfo = studentInfoStr.split(",");
                
                // Ensure there are enough fields (at least 4: ID, Name, Email, Password)
                if (studentInfo.length >= 4) {
                    String studentId = studentInfo[0];      // Student ID
                    String studentName = studentInfo[1];    // Student name
                    String email = studentInfo[2];          // Email
                    
                    // Match email (case insensitive)
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        // Found currently logged in student, save their information
                        currentStudentId = studentId;
                        currentStudentName = studentName;
                        currentStudentLineData = line;  // Save entire line data for subsequent modification
                        System.out.println("Loaded student info - ID: " + currentStudentId + ", Name: " + currentStudentName);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  // Print exception stack trace
        } finally {
            try {
                // Close file reader, release resources
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Get current subject count
    private static int getCurrentSubjectCount() {
        // If no student data is loaded, return 0
        if (currentStudentLineData == null || currentStudentLineData.isEmpty()) {
            return 0;
        }
        
        // Course information
        String[] mainParts = currentStudentLineData.split("\\|");
        if (mainParts.length <= 1) {
            return 0;  // No vertical bar or no content after vertical bar, meaning no courses
        }
        
        // Get course information part
        String subjectsStr = mainParts[1];
        if (subjectsStr == null || subjectsStr.trim().isEmpty()) {
            return 0;  // Course information is empty
        }
        
        // Split multiple courses by semicolon, return number of courses
        String[] subjectsArray = subjectsStr.split(";");
        return subjectsArray.length;
    }
    
    // Detect if course data has changed and auto refresh
    private static void checkAndRefreshIfChanged() {
        int currentCount = getCurrentSubjectCount();  // Get current subject count
        
        if (lastSubjectCount == -1) {
            // First run, only record without refreshing
            lastSubjectCount = currentCount;
            System.out.println("Initial subject count: " + currentCount);
        } else if (currentCount != lastSubjectCount) {
            // Subject count changed, refresh table
            System.out.println("Subject count changed from " + lastSubjectCount + " to " + currentCount + ", refreshing...");
            lastSubjectCount = currentCount;
            refreshSubjectTable();  // Call refresh method
        }
    }
    
    // Start auto-refresh timer
    private static void startAutoRefreshTimer() {
        
        // Create timer with 1 second interval
        refreshTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If main window still exists and is visible
                if (mainFrame != null && mainFrame.isDisplayable()) {
                    reloadCurrentStudentData();  // Reload data
                    checkAndRefreshIfChanged();  // Check for changes
                }
            }
        });
        refreshTimer.start();  // Start timer
        System.out.println("Auto-refresh timer started (interval: 1 second)");
    }
    
    // Stop auto-refresh timer
    private static void stopAutoRefreshTimer() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
            System.out.println("Auto-refresh timer stopped");
        }
    }
    
    // Reload current student data
    private static void reloadCurrentStudentData() {
        BufferedReader reader = null;
        
        try {
            File dataFile = new File(STUDENT_DATA_FILE_PATH);
            if (!dataFile.exists()) {
                return;
            }
            
            reader = new BufferedReader(new FileReader(dataFile));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Parse student basic information
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    continue;
                }
                
                String[] studentInfo = mainParts[0].split(",");
                if (studentInfo.length >= 4) {
                    String email = studentInfo[2];  // 3rd field is email
                    // Match current student
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        currentStudentLineData = line;  // Update cached data
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Generate random score (25-100)
    private static int generateRandomScore() {
        Random random = new Random();
        // nextInt(76) generates random number between 0-75, add 25 to get 25-100
        return 25 + random.nextInt(76);
    }
    
    // Return grade letter based on score
    private static String getGradeLetter(int score) {
        if (score >= 85) return "HD";
        if (score >= 75) return "D";
        if (score >= 65) return "C";
        if (score >= 50) return "P";
        return "F";
    }
    
    // Generate random subject ID
    private static String generateRandomSubjectId() {
        Random random = new Random();
        int idNumber = random.nextInt(999) + 1;  // nextInt(999) generates 0-998, +1 gives 1-999
        return String.format("%03d", idNumber);
    }
    
    // Generate random subject name
    private static String generateSubjectName(String subjectId) {
        return "Subject-" + subjectId;
    }
    
    // Check if student is already enrolled in the subject
    private static boolean isSubjectAlreadyEnrolled(String subjectId) {
        // If no student data, return false
        if (currentStudentLineData == null || currentStudentLineData.isEmpty()) {
            return false;
        }
        
        // Split by vertical bar to read courses
        String[] mainParts = currentStudentLineData.split("\\|");
        if (mainParts.length <= 1) {
            return false;
        }
        
        String subjectsStr = mainParts[1];
        if (subjectsStr == null || subjectsStr.trim().isEmpty()) {
            return false;
        }
        
        // Split multiple courses by semicolon, iterate through check
        String[] subjectsArray = subjectsStr.split(";");
        for (String subjectInfo : subjectsArray) {
            if (subjectInfo.trim().isEmpty()) {
                continue;
            }
            // Split course information by colon, get subject ID
            String[] subjectData = subjectInfo.split(":");
            if (subjectData.length >= 1) {
                String existingSubjectId = cleanField(subjectData[0]);
                if (existingSubjectId.equals(subjectId)) {
                    return true;  // Found matching subject ID
                }
            }
        }
        return false;
    }
    
    // Save course information to student data file
    private static boolean saveSubjectToStudentFile(String subjectId, String subjectName, int score, String grade) {
        List<String> allLines = new ArrayList<>();  // Store all lines of data
        BufferedReader reader = null;
        boolean found = false;
        
        try {
            File dataFile = new File(STUDENT_DATA_FILE_PATH);
            if (!dataFile.exists()) {
                System.err.println("Student data file does not exist: " + dataFile.getAbsolutePath());
                return false;
            }
            
            reader = new BufferedReader(new FileReader(dataFile));
            String line;
            
            // Read all lines
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    allLines.add(line);
                    continue;
                }
                
                // Split by vertical bar
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    allLines.add(line);
                    continue;
                }
                
                // Parse student basic information
                String[] studentInfo = mainParts[0].split(",");
                if (studentInfo.length >= 4) {
                    String email = studentInfo[2];
                    
                    // Find currently logged in student
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        found = true;
                        StringBuilder newLine = new StringBuilder();
                        newLine.append(mainParts[0]);  // Keep student basic information
                        
                        // Build course information part
                        StringBuilder subjectsBuilder = new StringBuilder();
                        
                        // Add existing courses
                        if (mainParts.length > 1 && mainParts[1] != null && !mainParts[1].trim().isEmpty()) {
                            subjectsBuilder.append(mainParts[1]);
                        }
                        
                        // Add new course (format: SubjectID:Score:Grade)
                        if (subjectsBuilder.length() > 0) {
                            subjectsBuilder.append(";");
                        }
                        subjectsBuilder.append(subjectId).append(":").append(score).append(":").append(grade);
                        
                        newLine.append("|").append(subjectsBuilder.toString());
                        allLines.add(newLine.toString());
                        currentStudentLineData = newLine.toString();  // Update memory cache
                        continue;
                    }
                }
                allLines.add(line);
            }
            reader.close();
            
            if (!found) {
                System.err.println("Student not found: " + currentStudentEmail);
                return false;
            }
            
            // Write back to file (overwrite)
            BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
            
            for (int i = 0; i < allLines.size(); i++) {
                writer.write(allLines.get(i));
                if (i < allLines.size() - 1) {
                    writer.newLine();  // Add newline after each line except the last
                }
            }
            writer.close();
            
            System.out.println("Course saved to student file: " + subjectId + " - " + subjectName);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Random course enrolment
    private static void randomEnrolSubject() {
        // Check if subject count has reached the limit
        int currentSubjectCount = getCurrentSubjectCount();
        if (currentSubjectCount >= MAX_SUBJECTS) {
            JOptionPane.showMessageDialog(mainFrame, 
                "You have already enrolled in " + MAX_SUBJECTS + " subjects!\nMaximum limit reached.",
                "Cannot Enrol", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Generate random subject ID and name
        String newSubjectId = generateRandomSubjectId();
        String newSubjectName = generateSubjectName(newSubjectId);
        
        // Check if already enrolled
        if (isSubjectAlreadyEnrolled(newSubjectId)) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Randomly generated subject " + newSubjectId + " is already enrolled!\nPlease try again.",
                "Already Enrolled", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Generate random score and grade
        int randomScore = generateRandomScore();
        String gradeLetter = getGradeLetter(randomScore);
        
        // Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
            "Random subject generated:\n\n" +
            "Subject ID: " + newSubjectId + "\n" +
            "Subject Name: " + newSubjectName + "\n" +
            "Score: " + randomScore + "\n" +
            "Grade: " + gradeLetter + "\n\n" +
            "Do you want to enrol this subject?",
            "Confirm Enrolment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        // If user cancels, do not save
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Save to file and refresh
        boolean saveSuccess = saveSubjectToStudentFile(newSubjectId, newSubjectName, randomScore, gradeLetter);
        
        if (saveSuccess) {
            reloadCurrentStudentData();      // Reload data
            loadStudentSubjectDataToTable(); // Refresh table display
            JOptionPane.showMessageDialog(mainFrame, 
                "Subject enrolled successfully!\n\nSubject ID: " + newSubjectId + 
                "\nSubject Name: " + newSubjectName +
                "\nScore: " + randomScore +
                "\nGrade: " + gradeLetter,
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            lastSubjectCount = tableModel.getRowCount();  // Update recorded count
        } else {
            JOptionPane.showMessageDialog(mainFrame, 
                "Failed to enrol subject!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Display student main interface
    public static void showStudentMain(String studentEmail) {
        currentStudentEmail = studentEmail;  // Set currently logged in student email
        
        // Load student basic information
        loadCurrentStudentInfo();
        
        // Create main window
        mainFrame = new JFrame("Student Main Page - " + currentStudentName);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Destroy current window when closing
        mainFrame.setSize(1000, 600);
        mainFrame.setLayout(new BorderLayout());  // Use border layout manager
        
        // Add window close listener to stop timer when window closes
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                stopAutoRefreshTimer();
            }
        });
        
        // Create top welcome panel
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(Color.PINK);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentStudentName + "!", SwingConstants.CENTER);
        Font welcomeFont = new Font("Times New Roman", Font.BOLD, 36);
        welcomeLabel.setFont(welcomeFont);
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        
        mainFrame.add(welcomePanel, BorderLayout.NORTH);  // Add to top of window
        
        // Create main content panel
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        
        // Left side course information table
        String[] columnNames = {"Subject ID", "Subject Name", "Credits", "Score", "Grade"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Disable editing of table cells to ensure data integrity
            }
        };
        
        subjectTable = new JTable(tableModel);
        subjectTable.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        subjectTable.setRowHeight(30);
        subjectTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 18));
        subjectTable.getTableHeader().setBackground(new Color(0, 102, 204));
        subjectTable.getTableHeader().setForeground(Color.WHITE);
        subjectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // Single selection mode, only one row at a time
        
        // Load student course data into table
        loadStudentSubjectDataToTable();
        lastSubjectCount = tableModel.getRowCount();
        System.out.println("Initial subject count: " + lastSubjectCount);
        
        // Create scroll pane to wrap table
        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Subjects"));
        scrollPane.setPreferredSize(new Dimension(650, 450));
        
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Right side operation button panel
        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new GridBagLayout());  // Use grid bag layout
        rightButtonPanel.setBackground(Color.WHITE);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
        
        // Create layout constraints object
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Buttons fill horizontally
        
        // Uniform button font and size
        Font buttonFont = new Font("Times New Roman", Font.BOLD, 18);
        Dimension buttonSize = new Dimension(180, 45);
        
        // Random enrolment button
        JButton enrolButton = new JButton("Random Enrol");
        enrolButton.setFont(buttonFont);
        enrolButton.setPreferredSize(buttonSize);
        enrolButton.setBackground(Color.ORANGE);
        enrolButton.setForeground(Color.BLACK);
        enrolButton.addActionListener(e -> {
            randomEnrolSubject();  // Execute random enrolment on click
        });
        gbc.gridy = 0;
        rightButtonPanel.add(enrolButton, gbc);
        
        // Delete course button
        JButton deleteSubjectButton = new JButton("Delete Subject");
        deleteSubjectButton.setFont(buttonFont);
        deleteSubjectButton.setPreferredSize(buttonSize);
        deleteSubjectButton.setBackground(Color.CYAN);
        deleteSubjectButton.setForeground(Color.BLACK);
        deleteSubjectButton.setEnabled(false);  // Initially disabled, enabled after a course is selected
        
        deleteSubjectButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();  // Get selected row index
            if (selectedRow >= 0) {
                // Get selected course information
                String subjectId = (String) tableModel.getValueAt(selectedRow, 0);
                String subjectName = (String) tableModel.getValueAt(selectedRow, 1);
                
                // Show confirmation dialog
                int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Are you sure you want to delete subject:\n" + subjectId + " - " + subjectName + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // Delete the course from data file
                    boolean deleteSuccess = deleteSubjectFromDataFile(subjectId);
                    if (deleteSuccess) {
                        reloadCurrentStudentData();               // Reload data
                        loadStudentSubjectDataToTable();          // Refresh table
                        JOptionPane.showMessageDialog(mainFrame,
                            "Subject deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        deleteSubjectButton.setEnabled(false);    // Disable delete button
                        lastSubjectCount = tableModel.getRowCount();  // Update recorded count
                    } else {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Failed to delete subject!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                    "Please select a subject to delete first!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        gbc.gridy = 1;
        rightButtonPanel.add(deleteSubjectButton, gbc);
        
        mainContentPanel.add(rightButtonPanel, BorderLayout.EAST);  // Add to right side of main content panel
        mainFrame.add(mainContentPanel, BorderLayout.CENTER);
        
        // Bottom button panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        
        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                stopAutoRefreshTimer();  // Stop timer
                mainFrame.dispose();      // Close main interface
                StudentLoginFrame.showStudentLogin(null);  // Reopen student login interface
            }
        });
        
        bottomPanel.add(logoutButton);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);  // Add to bottom of window
        
        // Table row selection listener
        // Enable delete button when a row is selected, disable when no row is selected
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {  // Prevent duplicate event triggering
                boolean rowSelected = subjectTable.getSelectedRow() >= 0;
                deleteSubjectButton.setEnabled(rowSelected);
            }
        });
        
        // Display window and start timer
        mainFrame.setLocationRelativeTo(null);  // Center window on screen
        mainFrame.setVisible(true);              // Make window visible
        
        startAutoRefreshTimer();  // Start auto-refresh timer
    }
    
    // Load course data from current student's line data into table
    private static void loadStudentSubjectDataToTable() {
        tableModel.setRowCount(0);  // Clear existing data
        
        // If no student data, return
        if (currentStudentLineData == null || currentStudentLineData.isEmpty()) {
            System.out.println("No student data loaded");
            return;
        }
        
        // Split by vertical bar, take course part
        String[] mainParts = currentStudentLineData.split("\\|");
        if (mainParts.length <= 1) {
            System.out.println("No subjects found for student");
            return;
        }
        
        String subjectsStr = mainParts[1];
        if (subjectsStr == null || subjectsStr.trim().isEmpty()) {
            System.out.println("No subjects found for student");
            return;
        }
        
        // Split multiple courses by semicolon
        String[] subjectsArray = subjectsStr.split(";");
        for (String subjectInfo : subjectsArray) {
            if (subjectInfo.trim().isEmpty()) {
                continue;
            }
            
            // Split course information by colon (format: SubjectID:Score:Grade)
            String[] subjectData = subjectInfo.split(":");
            if (subjectData.length >= 3) {
                Vector<String> rowData = new Vector<>();
                String subjectId = cleanField(subjectData[0]);
                rowData.add(subjectId);                                    // Subject ID
                rowData.add(generateSubjectName(subjectId));               // Subject Name
                rowData.add("3");                                          // Credits (default 3 credits)
                rowData.add(cleanField(subjectData[1]));                  // Score
                rowData.add(cleanField(subjectData[2]));                  // Grade
                tableModel.addRow(rowData);
            }
        }
        
        System.out.println("Loaded " + tableModel.getRowCount() + " subjects for student: " + currentStudentId);
    }
    
    // Delete specified course from data file
    private static boolean deleteSubjectFromDataFile(String subjectId) {
        List<String> allLines = new ArrayList<>();  // Store all lines of data
        BufferedReader reader = null;
        boolean found = false;
        
        try {
            File dataFile = new File(STUDENT_DATA_FILE_PATH);
            if (!dataFile.exists()) {
                System.out.println("Data file does not exist: " + dataFile.getAbsolutePath());
                return false;
            }
            
            reader = new BufferedReader(new FileReader(dataFile));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    allLines.add(line);
                    continue;
                }
                
                // Split by vertical bar
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    allLines.add(line);
                    continue;
                }
                
                // Parse student basic information
                String[] studentInfo = mainParts[0].split(",");
                if (studentInfo.length >= 4) {
                    String email = studentInfo[2];
                    
                    // Find currently logged in student
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        // Rebuild course part, skip the course to be deleted
                        StringBuilder subjectsBuilder = new StringBuilder();
                        
                        if (mainParts.length > 1 && mainParts[1] != null && !mainParts[1].trim().isEmpty()) {
                            String[] subjectsArray = mainParts[1].split(";");
                            for (int i = 0; i < subjectsArray.length; i++) {
                                String subjectInfo = subjectsArray[i];
                                if (subjectInfo.trim().isEmpty()) {
                                    continue;
                                }
                                
                                // Parse course information
                                String[] subjectData = subjectInfo.split(":");
                                if (subjectData.length >= 1) {
                                    String existingSubjectId = cleanField(subjectData[0]);
                                    if (existingSubjectId.equals(subjectId)) {
                                        found = true;
                                        System.out.println("Found subject to delete: " + subjectId);
                                        continue;  // Skip the course to be deleted, do not add to result
                                    }
                                }
                                
                                // Keep courses that are not to be deleted
                                if (subjectsBuilder.length() > 0) {
                                    subjectsBuilder.append(";");
                                }
                                subjectsBuilder.append(subjectInfo);
                            }
                        }
                        
                        // Build new line
                        StringBuilder newLine = new StringBuilder();
                        newLine.append(mainParts[0]);  // Student basic information
                        if (subjectsBuilder.length() > 0) {
                            newLine.append("|").append(subjectsBuilder.toString());
                        } else {
                            newLine.append("|");  // When no courses, vertical bar followed by empty
                        }
                        
                        allLines.add(newLine.toString());
                        if (found) {
                            currentStudentLineData = newLine.toString();  // Update memory cache
                        }
                        continue;
                    }
                }
                allLines.add(line);
            }
            reader.close();
            
            if (!found) {
                System.out.println("Subject not found: " + subjectId);
                return false;
            }
            
            // Write back to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
            
            for (int i = 0; i < allLines.size(); i++) {
                writer.write(allLines.get(i));
                if (i < allLines.size() - 1) {
                    writer.newLine();
                }
            }
            writer.close();
            
            System.out.println("Subject deleted successfully: " + subjectId);
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Clean field value
    private static String cleanField(String field) {
        if (field == null) {
            return "";
        }
        String cleaned = field;
        // If field is surrounded by quotes, remove leading and trailing quotes
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
    
    // Refresh the course table in student main interface
    public static void refreshSubjectTable() {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null && mainFrame.isDisplayable()) {
                reloadCurrentStudentData();      // Reload data
                loadStudentSubjectDataToTable(); // Reload table
                mainFrame.repaint();             // Repaint window
                System.out.println("Subject table refreshed at: " + new java.util.Date());
            }
        });
    }
}