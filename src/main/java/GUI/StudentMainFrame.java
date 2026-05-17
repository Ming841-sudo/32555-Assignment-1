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

// Student Main Interface
public class StudentMainFrame {
    
    // Path to student data file
    private static final String STUDENT_DATA_FILE_PATH = "students.data";
    // Maximum allowed number of enrolled subjects
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
    // Course table component, displays student's enrolled courses
    private static JTable subjectTable;
    // Reference to main window
    private static JFrame mainFrame;
    // Timer to automatically refresh course list every 1 second
    private static Timer refreshTimer;
    // Record previous course count to detect data changes
    private static int lastSubjectCount = -1;
    
    // Display student main interface
    public static void showStudentMain(String studentEmail) {
        currentStudentEmail = studentEmail;
        
        // Load student basic information
        loadCurrentStudentInfo();
        
        // Create main window
        mainFrame = new JFrame("Student Main Page - " + currentStudentName);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setSize(1000, 600);
        mainFrame.setLayout(new BorderLayout());
        
        // Add window close listener to stop timer when window is closed
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
        
        mainFrame.add(welcomePanel, BorderLayout.NORTH);
        
        // Create main content panel
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        
        // Left side course information table
        String[] columnNames = {"Subject ID", "Subject Name", "Credits", "Score", "Grade"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        subjectTable = new JTable(tableModel);
        subjectTable.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        subjectTable.setRowHeight(30);
        subjectTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 18));
        subjectTable.getTableHeader().setBackground(new Color(0, 102, 204));
        subjectTable.getTableHeader().setForeground(Color.WHITE);
        subjectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Load student's course data into table
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
        rightButtonPanel.setLayout(new GridBagLayout());
        rightButtonPanel.setBackground(Color.WHITE);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
        
        // Create layout constraint object
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Unified button font and size
        Font buttonFont = new Font("Times New Roman", Font.BOLD, 18);
        Dimension buttonSize = new Dimension(180, 45);
        
        // Random enrol button
        JButton enrolButton = new JButton("Random Enrol");
        enrolButton.setFont(buttonFont);
        enrolButton.setPreferredSize(buttonSize);
        enrolButton.setBackground(Color.ORANGE);
        enrolButton.setForeground(Color.BLACK);
        enrolButton.addActionListener(e -> randomEnrolSubject());
        gbc.gridy = 0;
        rightButtonPanel.add(enrolButton, gbc);
        
        // Delete subject button
        JButton deleteSubjectButton = new JButton("Delete Subject");
        deleteSubjectButton.setFont(buttonFont);
        deleteSubjectButton.setPreferredSize(buttonSize);
        deleteSubjectButton.setBackground(Color.CYAN);
        deleteSubjectButton.setForeground(Color.BLACK);
        deleteSubjectButton.setEnabled(false);
        
        deleteSubjectButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow >= 0) {
                String subjectId = (String) tableModel.getValueAt(selectedRow, 0);
                String subjectName = (String) tableModel.getValueAt(selectedRow, 1);
                
                int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Are you sure you want to delete subject:\n" + subjectId + " - " + subjectName + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleteSuccess = deleteSubjectFromDataFile(subjectId);
                    if (deleteSuccess) {
                        reloadCurrentStudentData();
                        loadStudentSubjectDataToTable();
                        JOptionPane.showMessageDialog(mainFrame,
                            "Subject deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        deleteSubjectButton.setEnabled(false);
                        lastSubjectCount = tableModel.getRowCount();
                    } else {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Failed to delete subject!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        gbc.gridy = 1;
        rightButtonPanel.add(deleteSubjectButton, gbc);
        
        mainContentPanel.add(rightButtonPanel, BorderLayout.EAST);
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
                stopAutoRefreshTimer();
                mainFrame.dispose();
                StudentLoginFrame.showStudentLogin(null);
            }
        });
        
        bottomPanel.add(logoutButton);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);
        
        // Table row selection listener
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = subjectTable.getSelectedRow() >= 0;
            deleteSubjectButton.setEnabled(rowSelected);
        });
        
        // Display window and start timer
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        startAutoRefreshTimer();
    }
    
    // Load course data from current student's line data into table
    private static void loadStudentSubjectDataToTable() {
        tableModel.setRowCount(0);
        
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
        
        String[] subjectsArray = subjectsStr.split(";");
        for (String subjectInfo : subjectsArray) {
            if (subjectInfo.trim().isEmpty()) {
                continue;
            }
            
            String[] subjectData = subjectInfo.split(":");
            if (subjectData.length >= 3) {
                Vector<String> rowData = new Vector<>();
                String subjectId = cleanField(subjectData[0]);
                rowData.add(subjectId);
                rowData.add(generateSubjectName(subjectId));
                rowData.add("3");
                rowData.add(cleanField(subjectData[1]));
                rowData.add(cleanField(subjectData[2]));
                tableModel.addRow(rowData);
            }
        }
        
        System.out.println("Loaded " + tableModel.getRowCount() + " subjects for student: " + currentStudentId);
    }

    // Randomly enrol in a subject
    private static void randomEnrolSubject() {
        int currentSubjectCount = getCurrentSubjectCount();
        if (currentSubjectCount >= MAX_SUBJECTS) {
            JOptionPane.showMessageDialog(mainFrame, 
                "You have already enrolled in " + MAX_SUBJECTS + " subjects!\nMaximum limit reached.",
                "Cannot Enrol", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String newSubjectId = generateRandomSubjectId();
        String newSubjectName = generateSubjectName(newSubjectId);
        
        if (isSubjectAlreadyEnrolled(newSubjectId)) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Randomly generated subject " + newSubjectId + " is already enrolled!\nPlease try again.",
                "Already Enrolled", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int randomScore = generateRandomScore();
        String gradeLetter = getGradeLetter(randomScore);
        
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
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        boolean saveSuccess = saveSubjectToStudentFile(newSubjectId, newSubjectName, randomScore, gradeLetter);
        
        if (saveSuccess) {
            reloadCurrentStudentData();
            loadStudentSubjectDataToTable();
            JOptionPane.showMessageDialog(mainFrame, 
                "Subject enrolled successfully!\n\nSubject ID: " + newSubjectId + 
                "\nSubject Name: " + newSubjectName +
                "\nScore: " + randomScore +
                "\nGrade: " + gradeLetter,
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            lastSubjectCount = tableModel.getRowCount();
        } else {
            JOptionPane.showMessageDialog(mainFrame, 
                "Failed to enrol subject!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Delete specified subject from data file
    private static boolean deleteSubjectFromDataFile(String subjectId) {
        List<String> allLines = new ArrayList<>();
        boolean found = false;
        
        File dataFile = new File(STUDENT_DATA_FILE_PATH);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    allLines.add(line);
                    continue;
                }
                
                String[] studentInfo = mainParts[0].split(",");
                if (studentInfo.length >= 4) {
                    String email = studentInfo[2];
                    
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        StringBuilder subjectsBuilder = new StringBuilder();
                        
                        if (mainParts.length > 1 && mainParts[1] != null && !mainParts[1].trim().isEmpty()) {
                            String[] subjectsArray = mainParts[1].split(";");
                            for (String subjectInfo : subjectsArray) {
                                if (subjectInfo.trim().isEmpty()) {
                                    continue;
                                }
                                
                                String[] subjectData = subjectInfo.split(":");
                                if (subjectData.length >= 1) {
                                    String existingSubjectId = cleanField(subjectData[0]);
                                    if (existingSubjectId.equals(subjectId)) {
                                        found = true;
                                        System.out.println("Found subject to delete: " + subjectId);
                                        continue;
                                    }
                                }
                                
                                if (subjectsBuilder.length() > 0) {
                                    subjectsBuilder.append(";");
                                }
                                subjectsBuilder.append(subjectInfo);
                            }
                        }
                        
                        StringBuilder newLine = new StringBuilder();
                        newLine.append(mainParts[0]);
                        if (subjectsBuilder.length() > 0) {
                            newLine.append("|").append(subjectsBuilder.toString());
                        } else {
                            newLine.append("|");
                        }
                        
                        allLines.add(newLine.toString());
                        if (found) {
                            currentStudentLineData = newLine.toString();
                        }
                        
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
                            for (int i = 0; i < allLines.size(); i++) {
                                writer.write(allLines.get(i));
                                if (i < allLines.size() - 1) {
                                    writer.newLine();
                                }
                            }
                        }
                        
                        System.out.println("Subject deleted successfully: " + subjectId);
                        return true;
                    }
                }
                allLines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        return false;
    }
    
    // Load basic information of currently logged in student
    private static void loadCurrentStudentInfo() {
        File dataFile = new File(STUDENT_DATA_FILE_PATH);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    continue;
                }
                
                String[] studentInfo = mainParts[0].split(",");
                if (studentInfo.length >= 4) {
                    String studentId = studentInfo[0];
                    String studentName = studentInfo[1];
                    String email = studentInfo[2];
                    
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        currentStudentId = studentId;
                        currentStudentName = studentName;
                        currentStudentLineData = line;
                        System.out.println("Loaded student info - ID: " + currentStudentId + ", Name: " + currentStudentName);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Get current subject count
    private static int getCurrentSubjectCount() {
        String[] mainParts = currentStudentLineData.split("\\|");
        if (mainParts.length <= 1) {
            return 0;
        }
        
        String subjectsStr = mainParts[1];
        if (subjectsStr == null || subjectsStr.trim().isEmpty()) {
            return 0;
        }
        
        String[] subjectsArray = subjectsStr.split(";");
        return subjectsArray.length;
    }
    
    // Check if course data has changed and auto-refresh if needed
    private static void checkAndRefreshIfChanged() {
        int currentCount = getCurrentSubjectCount();
        
        if (lastSubjectCount == -1) {
            lastSubjectCount = currentCount;
            System.out.println("Initial subject count: " + currentCount);
        } else if (currentCount != lastSubjectCount) {
            System.out.println("Subject count changed from " + lastSubjectCount + " to " + currentCount + ", refreshing...");
            lastSubjectCount = currentCount;
            refreshSubjectTable();
        }
    }

    // Refresh the course table on student main interface
    public static void refreshSubjectTable() {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null && mainFrame.isDisplayable()) {
                reloadCurrentStudentData();
                loadStudentSubjectDataToTable();
                mainFrame.repaint();
                System.out.println("Subject table refreshed at: " + new java.util.Date());
            }
        });
    }
    
    // Start auto-refresh timer
    private static void startAutoRefreshTimer() {
        refreshTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainFrame != null && mainFrame.isDisplayable()) {
                    reloadCurrentStudentData();
                    checkAndRefreshIfChanged();
                }
            }
        });
        refreshTimer.start();
        System.out.println("Auto-refresh timer started (interval: 1 second)");
    }
    
    // Stop auto-refresh timer
    private static void stopAutoRefreshTimer() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
            System.out.println("Auto-refresh timer stopped");
        }
    }
    
    // Reload current student's data
    private static void reloadCurrentStudentData() {
        File dataFile = new File(STUDENT_DATA_FILE_PATH);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    continue;
                }
                
                String[] studentInfo = mainParts[0].split(",");
                if (studentInfo.length >= 4) {
                    String email = studentInfo[2];
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        currentStudentLineData = line;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Generate random score (25-100)
    private static int generateRandomScore() {
        Random random = new Random();
        return 25 + random.nextInt(76);
    }
    
    // Return grade letter based on score
    private static String getGradeLetter(int score) {
        if (score >= 85) return "HD";
        if (score >= 75) return "D";
        if (score >= 65) return "C";
        if (score >= 50) return "P";
        return "Z";
    }
    
    // Generate random subject ID
    private static String generateRandomSubjectId() {
        Random random = new Random();
        int idNumber = random.nextInt(999) + 1;
        return String.format("%03d", idNumber);
    }
    
    // Generate random subject name
    private static String generateSubjectName(String subjectId) {
        return "Subject-" + subjectId;
    }
    
    // Check if student is already enrolled in the subject
    private static boolean isSubjectAlreadyEnrolled(String subjectId) {
        String[] mainParts = currentStudentLineData.split("\\|");
        if (mainParts.length <= 1) {
            return false;
        }
        
        String subjectsStr = mainParts[1];
        if (subjectsStr == null || subjectsStr.trim().isEmpty()) {
            return false;
        }
        
        String[] subjectsArray = subjectsStr.split(";");
        for (String subjectInfo : subjectsArray) {
            if (subjectInfo.trim().isEmpty()) {
                continue;
            }
            String[] subjectData = subjectInfo.split(":");
            if (subjectData.length >= 1) {
                String existingSubjectId = cleanField(subjectData[0]);
                if (existingSubjectId.equals(subjectId)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Save course information to student data file
    private static boolean saveSubjectToStudentFile(String subjectId, String subjectName, int score, String grade) {
        List<String> allLines = new ArrayList<>();
        
        File dataFile = new File(STUDENT_DATA_FILE_PATH);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    allLines.add(line);
                    continue;
                }
                
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    allLines.add(line);
                    continue;
                }
                
                String[] studentInfo = mainParts[0].split(",");
                if (studentInfo.length >= 4) {
                    String email = studentInfo[2];
                    
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        StringBuilder newLine = new StringBuilder();
                        newLine.append(mainParts[0]);
                        
                        StringBuilder subjectsBuilder = new StringBuilder();
                        
                        if (mainParts.length > 1 && mainParts[1] != null && !mainParts[1].trim().isEmpty()) {
                            subjectsBuilder.append(mainParts[1]);
                        }
                        
                        if (subjectsBuilder.length() > 0) {
                            subjectsBuilder.append(";");
                        }
                        subjectsBuilder.append(subjectId).append(":").append(score).append(":").append(grade);
                        
                        newLine.append("|").append(subjectsBuilder.toString());
                        allLines.add(newLine.toString());
                        currentStudentLineData = newLine.toString();
                        continue;
                    }
                }
                allLines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            for (int i = 0; i < allLines.size(); i++) {
                writer.write(allLines.get(i));
                if (i < allLines.size() - 1) {
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        System.out.println("Course saved to student file: " + subjectId + " - " + subjectName);
        return true;
    }

    // Clean field value (remove surrounding quotes if present)
    private static String cleanField(String field) {
        if (field == null) {
            return "";
        }
        String cleaned = field;
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
}