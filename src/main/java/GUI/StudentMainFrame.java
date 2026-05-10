package org.GUI_publish;

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

// 学生主界面
public class StudentMainFrame {
    
    // 学生数据文件保存路径（存储所有学生基本信息和课程信息）
    private static final String STUDENT_DATA_FILE_PATH = "students.data";
    // 最大允许选课数量
    private static final int MAX_SUBJECTS = 4;
    // 当前登录的学生邮箱
    private static String currentStudentEmail;
    // 当前登录的学生ID（6位数字）
    private static String currentStudentId;
    // 当前登录的学生姓名
    private static String currentStudentName;
    // 当前学生对应的完整行数据（用于保存修改）
    private static String currentStudentLineData;
    // 表格模型，用于管理课程表格数据
    private static DefaultTableModel tableModel;
    // 课程表格组件，用于显示学生已注册的课程
    private static JTable subjectTable;
    // 主窗口引用
    private static JFrame mainFrame;
    // 定时器，用于自动刷新课程列表
    private static Timer refreshTimer;
    // 记录上一次的课程数量，用于检测变化
    private static int lastSubjectCount = -1;
    
    // 打印当前工作目录
    private static void printWorkingDirectory() {
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        System.out.println("Student data file path: " + new File(STUDENT_DATA_FILE_PATH).getAbsolutePath());
    }
    
    // 加载当前登录学生的基本信息
    // 文件格式：学生ID,姓名,邮箱,密码|课程ID:成绩:等级;课程ID:成绩:等级;...
    private static void loadCurrentStudentInfo() {
        BufferedReader reader = null;
        
        try {
            File dataFile = new File(STUDENT_DATA_FILE_PATH);
            if (!dataFile.exists()) {
                System.err.println("Data file does not exist: " + dataFile.getAbsolutePath());
                return;
            }
            
            reader = new BufferedReader(new FileReader(dataFile));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // 按竖线分割，取第一部分为学生基本信息
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    continue;
                }
                
                String studentInfoStr = mainParts[0];
                String[] studentInfo = studentInfoStr.split(",");
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
    
    // 获取当前课程数量
    private static int getCurrentSubjectCount() {
        if (currentStudentLineData == null || currentStudentLineData.isEmpty()) {
            return 0;
        }
        
        String[] mainParts = currentStudentLineData.split("\\|");
        if (mainParts.length <= 1) {
            return 0;
        }
        
        String subjectsStr = mainParts[1];
        if (subjectsStr == null || subjectsStr.trim().isEmpty()) {
            return 0;
        }
        
        // 按分号分隔多门课程
        String[] subjectsArray = subjectsStr.split(";");
        return subjectsArray.length;
    }
    
    // 检测课程数据是否有变化并自动刷新
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
    
    // 启动自动刷新定时器
    private static void startAutoRefreshTimer() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
        
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
    
    // 停止自动刷新定时器
    private static void stopAutoRefreshTimer() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
            System.out.println("Auto-refresh timer stopped");
        }
    }
    
    // 重新加载当前学生的数据
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
    
    // 生成随机分数（25-100）
    private static int generateRandomScore() {
        Random random = new Random();
        return 25 + random.nextInt(76);
    }
    
    // 根据分数返回等级
    private static String getGradeLetter(int score) {
        if (score >= 85) return "HD";
        if (score >= 75) return "D";
        if (score >= 65) return "C";
        if (score >= 50) return "P";
        return "Z";
    }
    
    // 生成随机课程ID（1-999，3位数字格式）
    private static String generateRandomSubjectId() {
        Random random = new Random();
        int idNumber = random.nextInt(999) + 1;
        return String.format("%03d", idNumber);
    }
    
    // 生成随机课程名称（基于课程ID）
    private static String generateSubjectName(String subjectId) {
        return "Subject-" + subjectId;
    }
    
    // 检查学生是否已经注册过该课程
    private static boolean isSubjectAlreadyEnrolled(String subjectId) {
        if (currentStudentLineData == null || currentStudentLineData.isEmpty()) {
            return false;
        }
        
        String[] mainParts = currentStudentLineData.split("\\|");
        if (mainParts.length <= 1) {
            return false;
        }
        
        String subjectsStr = mainParts[1];
        if (subjectsStr == null || subjectsStr.trim().isEmpty()) {
            return false;
        }
        
        // 按分号分隔多门课程
        String[] subjectsArray = subjectsStr.split(";");
        for (String subjectInfo : subjectsArray) {
            if (subjectInfo.trim().isEmpty()) {
                continue;
            }
            // 按冒号分隔课程信息
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
    
    // 将课程信息写入学生数据文件
    private static boolean saveSubjectToStudentFile(String subjectId, String subjectName, int score, String grade) {
        List<String> allLines = new ArrayList<>();
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
                        found = true;
                        StringBuilder newLine = new StringBuilder();
                        newLine.append(mainParts[0]); // 学生基本信息
                        
                        // 构建课程信息部分
                        StringBuilder subjectsBuilder = new StringBuilder();
                        
                        // 添加原有课程
                        if (mainParts.length > 1 && mainParts[1] != null && !mainParts[1].trim().isEmpty()) {
                            subjectsBuilder.append(mainParts[1]);
                        }
                        
                        // 添加新课程（格式：课程ID:成绩:等级）
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
            reader.close();
            
            if (!found) {
                System.err.println("Student not found: " + currentStudentEmail);
                return false;
            }
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
            
            for (int i = 0; i < allLines.size(); i++) {
                writer.write(allLines.get(i));
                if (i < allLines.size() - 1) {
                    writer.newLine();
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
    
    // 随机注册课程
    private static void randomEnrolSubject() {
        // 检查选课数量是否已达上限
        int currentSubjectCount = getCurrentSubjectCount();
        if (currentSubjectCount >= MAX_SUBJECTS) {
            JOptionPane.showMessageDialog(mainFrame, 
                "You have already enrolled in " + MAX_SUBJECTS + " subjects!\nMaximum limit reached.",
                "Cannot Enrol", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 生成随机课程ID
        String newSubjectId = generateRandomSubjectId();
        String newSubjectName = generateSubjectName(newSubjectId);
        
        // 检查是否已注册
        if (isSubjectAlreadyEnrolled(newSubjectId)) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Randomly generated subject " + newSubjectId + " is already enrolled!\nPlease try again.",
                "Already Enrolled", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 生成随机分数和等级
        int randomScore = generateRandomScore();
        String gradeLetter = getGradeLetter(randomScore);
        
        // 确认注册
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
        
        // 保存到文件
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
    
    // 显示学生主界面
    public static void showStudentMain(String studentEmail) {
        currentStudentEmail = studentEmail;
        
        loadCurrentStudentInfo();
        printWorkingDirectory();
        
        // 创建主窗口
        mainFrame = new JFrame("Student Main Page - " + currentStudentName);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setSize(1000, 600);
        mainFrame.setLayout(new BorderLayout());
        
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                stopAutoRefreshTimer();
            }
        });
        
        // 顶部欢迎面板
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(Color.PINK);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentStudentName + "!", SwingConstants.CENTER);
        Font welcomeFont = new Font("Times New Roman", Font.BOLD, 36);
        welcomeLabel.setFont(welcomeFont);
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        
        mainFrame.add(welcomePanel, BorderLayout.NORTH);
        
        // 主内容面板
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        
        // 左侧课程信息表格
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
        
        loadStudentSubjectDataToTable();
        lastSubjectCount = tableModel.getRowCount();
        System.out.println("Initial subject count: " + lastSubjectCount);
        
        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Subjects"));
        scrollPane.setPreferredSize(new Dimension(650, 450));
        
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 右侧操作按钮面板
        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new GridBagLayout());
        rightButtonPanel.setBackground(Color.WHITE);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        Font buttonFont = new Font("Times New Roman", Font.BOLD, 18);
        Dimension buttonSize = new Dimension(180, 45);
        
        // 随机注册课程按钮
        JButton enrolButton = new JButton("Random Enrol");
        enrolButton.setFont(buttonFont);
        enrolButton.setPreferredSize(buttonSize);
        enrolButton.setBackground(Color.ORANGE);
        enrolButton.setForeground(Color.BLACK);
        enrolButton.setFocusPainted(false);
        enrolButton.setBorderPainted(false);
        enrolButton.setOpaque(true);
        enrolButton.addActionListener(e -> {
            randomEnrolSubject();
        });
        gbc.gridy = 0;
        rightButtonPanel.add(enrolButton, gbc);
        
        // 删除课程按钮
        JButton deleteSubjectButton = new JButton("Delete Subject");
        deleteSubjectButton.setFont(buttonFont);
        deleteSubjectButton.setPreferredSize(buttonSize);
        deleteSubjectButton.setBackground(Color.CYAN);
        deleteSubjectButton.setForeground(Color.BLACK);
        deleteSubjectButton.setFocusPainted(false);
        deleteSubjectButton.setBorderPainted(false);
        deleteSubjectButton.setOpaque(true);
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
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                    "Please select a subject to delete first!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        gbc.gridy = 1;
        rightButtonPanel.add(deleteSubjectButton, gbc);
        
        mainContentPanel.add(rightButtonPanel, BorderLayout.EAST);
        mainFrame.add(mainContentPanel, BorderLayout.CENTER);
        
        // 底部按钮面板
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                stopAutoRefreshTimer();
                mainFrame.dispose();
                StudentLoginFrame.showStudentLogin();
            }
        });
        
        bottomPanel.add(logoutButton);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);
        
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = subjectTable.getSelectedRow() >= 0;
                deleteSubjectButton.setEnabled(rowSelected);
            }
        });
        
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        startAutoRefreshTimer();
    }
    
    // 从当前学生的行数据中加载课程数据到表格
    private static void loadStudentSubjectDataToTable() {
        tableModel.setRowCount(0);
        
        if (currentStudentLineData == null || currentStudentLineData.isEmpty()) {
            System.out.println("No student data loaded");
            return;
        }
        
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
        
        // 按分号分隔多门课程
        String[] subjectsArray = subjectsStr.split(";");
        for (String subjectInfo : subjectsArray) {
            if (subjectInfo.trim().isEmpty()) {
                continue;
            }
            
            // 按冒号分隔课程信息（格式：课程ID:成绩:等级）
            String[] subjectData = subjectInfo.split(":");
            if (subjectData.length >= 3) {
                Vector<String> rowData = new Vector<>();
                String subjectId = cleanField(subjectData[0]);
                rowData.add(subjectId);                                    // Subject ID
                rowData.add(generateSubjectName(subjectId));               // Subject Name
                rowData.add("3");                                        // Credits (默认3学分)
                rowData.add(cleanField(subjectData[1]));                  // Score
                rowData.add(cleanField(subjectData[2]));                  // Grade
                tableModel.addRow(rowData);
            } else if (subjectData.length >= 1) {
                // 兼容旧格式（只有课程ID）
                Vector<String> rowData = new Vector<>();
                String subjectId = cleanField(subjectData[0]);
                rowData.add(subjectId);
                rowData.add(generateSubjectName(subjectId));
                rowData.add("3");
                rowData.add("Not graded");
                rowData.add("N/A");
                tableModel.addRow(rowData);
            }
        }
        
        System.out.println("Loaded " + tableModel.getRowCount() + " subjects for student: " + currentStudentId);
    }
    
    // 从数据文件中删除指定课程
    private static boolean deleteSubjectFromDataFile(String subjectId) {
        List<String> allLines = new ArrayList<>();
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
                
                String[] mainParts = line.split("\\|");
                if (mainParts.length == 0) {
                    allLines.add(line);
                    continue;
                }
                
                String[] studentInfo = mainParts[0].split(",");
                if (studentInfo.length >= 4) {
                    String email = studentInfo[2];
                    
                    if (email.equalsIgnoreCase(currentStudentEmail)) {
                        // 找到当前学生，重建课程部分（跳过要删除的课程）
                        StringBuilder subjectsBuilder = new StringBuilder();
                        
                        if (mainParts.length > 1 && mainParts[1] != null && !mainParts[1].trim().isEmpty()) {
                            String[] subjectsArray = mainParts[1].split(";");
                            for (int i = 0; i < subjectsArray.length; i++) {
                                String subjectInfo = subjectsArray[i];
                                if (subjectInfo.trim().isEmpty()) {
                                    continue;
                                }
                                
                                String[] subjectData = subjectInfo.split(":");
                                if (subjectData.length >= 1) {
                                    String existingSubjectId = cleanField(subjectData[0]);
                                    if (existingSubjectId.equals(subjectId)) {
                                        found = true;
                                        System.out.println("Found subject to delete: " + subjectId);
                                        continue; // 跳过要删除的课程
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
    
    // 清理字段值（去除首尾空格和引号）
    private static String cleanField(String field) {
        if (field == null) {
            return "";
        }
        String cleaned = field.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
    
    // 刷新学生主界面的课程表格
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
}