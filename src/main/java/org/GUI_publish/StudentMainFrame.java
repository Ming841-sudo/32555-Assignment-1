package org.GUI_publish;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

// 学生主界面
public class StudentMainFrame {
    
    // 学生CSV文件保存路径（存储所有学生基本信息）
    private static final String STUDENT_CSV_FILE_PATH = "StudentInfo.csv";
    // 学生个人课程文件夹路径（每个学生独立存放课程记录）
    private static final String STUDENTS_FOLDER_PATH = "Students";
    // 当前学生的个人课程文件路径
    private static String currentStudentSubjectFilePath;

    // 当前登录的学生邮箱
    private static String currentStudentEmail;
    // 当前登录的学生ID（6位数字）
    private static String currentStudentId;
    // 当前登录的学生姓名
    private static String currentStudentName;
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
        // System.getProperty("user.dir") 获取Java虚拟机启动时的当前工作目录
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        System.out.println("Student CSV file path: " + new File(STUDENT_CSV_FILE_PATH).getAbsolutePath());
        System.out.println("Students folder path: " + new File(STUDENTS_FOLDER_PATH).getAbsolutePath());
    }
    
    // 加载当前登录学生的基本信息
    private static void loadCurrentStudentInfo() {
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(STUDENT_CSV_FILE_PATH);
            if (!csvFile.exists()) {
                return;
            }
            
            // 读取文件
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] row = parseCSVLine(line);
                
                // 跳过表头行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 确保行有足够的列数
                if (row.length >= 3) {
                    String csvEmail = cleanField(row[2]);
                    // 匹配邮箱（不区分大小写）
                    if (csvEmail.equalsIgnoreCase(currentStudentEmail)) {
                        currentStudentId = cleanField(row[0]);
                        currentStudentName = cleanField(row[1]);
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
    
    // 设置当前学生的个人课程文件路径
    private static void setCurrentStudentSubjectFilePath() {
        if (currentStudentId != null && !currentStudentId.isEmpty()) {
            currentStudentSubjectFilePath = STUDENTS_FOLDER_PATH + File.separator + "Student-" + currentStudentId + ".csv";
            System.out.println("Student subject file path: " + new File(currentStudentSubjectFilePath).getAbsolutePath());
        }
    }
    
    // 检测课程数据是否有变化并自动刷新
    private static void checkAndRefreshIfChanged() {
        int currentCount = getCurrentSubjectCount();
        
        if (lastSubjectCount == -1) {
            // 首次运行，只记录不刷新
            lastSubjectCount = currentCount;
            System.out.println("Initial subject count: " + currentCount);
        } else if (currentCount != lastSubjectCount) {
            // 课程数量发生变化，刷新表格
            System.out.println("Subject count changed from " + lastSubjectCount + " to " + currentCount + ", refreshing...");
            lastSubjectCount = currentCount;
            refreshSubjectTable();
        }
    }
    
    // 获取当前课程数量（用于检测变化）
    private static int getCurrentSubjectCount() {
        BufferedReader reader = null;
        int count = 0;
        
        try {
            File csvFile = new File(currentStudentSubjectFilePath);
            if (!csvFile.exists()) {
                return 0;
            }
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // 跳过表头行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 非空数据行计数
                count++;
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
        return count;
    }
    
    // 启动自动刷新定时器
    private static void startAutoRefreshTimer() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();  // 先停止已有定时器
        }
        
        // 创建定时器，延迟0毫秒，间隔1秒
        refreshTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainFrame != null && mainFrame.isDisplayable()) {
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
    
    // 显示学生主界面
    public static void showStudentMain(String studentEmail) {
        currentStudentEmail = studentEmail;
        
        // 加载学生基本信息
        loadCurrentStudentInfo();
        
        // 设置个人课程文件路径
        setCurrentStudentSubjectFilePath();
        
        // 打印当前工作目录和文件路径（用于调试）
        printWorkingDirectory();
        
        // 创建主窗口
        mainFrame = new JFrame("Student Main Page - " + currentStudentName);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // 关闭时销毁当前窗口
        mainFrame.setSize(1000, 600);
        mainFrame.setLayout(new BorderLayout());
        
        // 添加窗口关闭监听器，在窗口关闭时停止定时器
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                stopAutoRefreshTimer();
            }
        });
        
        // 创建顶部欢迎面板
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(Color.PINK);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentStudentName + "!", SwingConstants.CENTER);
        Font welcomeFont = new Font("Times New Roman", Font.BOLD, 36);
        welcomeLabel.setFont(welcomeFont);
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        
        mainFrame.add(welcomePanel, BorderLayout.NORTH);
        
        // 创建主内容面板
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        
        // 左侧课程信息表格（显示学生已注册的课程）
        String[] columnNames = {"Subject ID", "Subject Name", "Credits", "Score", "Grade"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止编辑表格单元格，保证数据完整性
            }
        };
        
        subjectTable = new JTable(tableModel);
        subjectTable.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        subjectTable.setRowHeight(30);
        // 设置表头样式
        subjectTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 18));
        subjectTable.getTableHeader().setBackground(new Color(0, 102, 204));
        subjectTable.getTableHeader().setForeground(Color.WHITE);
        subjectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // 单选模式
        
        // 加载学生的课程数据到表格
        loadStudentSubjectDataToTable();
        
        // 初始化课程数量记录
        lastSubjectCount = tableModel.getRowCount();
        System.out.println("Initial subject count: " + lastSubjectCount);
        
        // 创建滚动面板包裹表格
        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Subjects"));
        scrollPane.setPreferredSize(new Dimension(650, 450));
        
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 右侧操作按钮面板
        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new GridBagLayout());  // 使用网格包布局
        rightButtonPanel.setBackground(Color.WHITE);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;  // 所有按钮放在第0列
        gbc.insets = new Insets(15, 0, 15, 0);  // 按钮垂直间距
        gbc.fill = GridBagConstraints.HORIZONTAL;  // 水平填满
        
        // 按钮统一字体和大小
        Font buttonFont = new Font("Times New Roman", Font.BOLD, 18);
        Dimension buttonSize = new Dimension(180, 45);
        
        // 注册课程按钮
        JButton enrolButton = new JButton("Enrol Subject");
        enrolButton.setFont(buttonFont);
        enrolButton.setPreferredSize(buttonSize);
        enrolButton.setBackground(Color.ORANGE);
        enrolButton.setForeground(Color.BLACK);
        enrolButton.setFocusPainted(false);
        enrolButton.setBorderPainted(false);
        enrolButton.setOpaque(true);
        // 点击后打开选课窗口
        enrolButton.addActionListener(e -> {
            EnrolSubjectsFrame.showEnrolSubjects(currentStudentEmail);
        });
        gbc.gridy = 0;
        rightButtonPanel.add(enrolButton, gbc);
        
        // 删除课程按钮
        JButton deleteSubjectButton = new JButton("Delete Subject");
        deleteSubjectButton.setFont(buttonFont);
        deleteSubjectButton.setPreferredSize(buttonSize);
        deleteSubjectButton.setBackground(Color.YELLOW);
        deleteSubjectButton.setForeground(Color.BLACK);
        deleteSubjectButton.setFocusPainted(false);
        deleteSubjectButton.setBorderPainted(false);
        deleteSubjectButton.setOpaque(true);
        deleteSubjectButton.setEnabled(false);  // 初始禁用，选中课程后才启用
        
        deleteSubjectButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow >= 0) {
                // 获取选中课程的信息
                String subjectId = (String) tableModel.getValueAt(selectedRow, 0);
                String subjectName = (String) tableModel.getValueAt(selectedRow, 1);
                
                // 弹出确认对话框
                int confirm = JOptionPane.showConfirmDialog(mainFrame, 
                    "Are you sure you want to delete subject:\n" + subjectId + " - " + subjectName + "?",
                    "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // 从学生个人课程文件中删除该课程
                    boolean deleteSuccess = deleteSubjectFromStudentFile(subjectId);
                    if (deleteSuccess) {
                        // 刷新表格
                        loadStudentSubjectDataToTable();
                        JOptionPane.showMessageDialog(mainFrame, 
                            "Subject deleted successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        deleteSubjectButton.setEnabled(false);
                        // 更新记录数量
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
        
        // 修改密码按钮
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.setFont(buttonFont);
        changePasswordButton.setPreferredSize(buttonSize);
        changePasswordButton.setBackground(Color.CYAN);
        changePasswordButton.setForeground(Color.BLACK);
        changePasswordButton.setFocusPainted(false);
        changePasswordButton.setBorderPainted(false);
        changePasswordButton.setOpaque(true);
        changePasswordButton.addActionListener(e -> {
            showChangePasswordDialog(mainFrame);
        });
        gbc.gridy = 2;
        rightButtonPanel.add(changePasswordButton, gbc);
        
        mainContentPanel.add(rightButtonPanel, BorderLayout.EAST);
        mainFrame.add(mainContentPanel, BorderLayout.CENTER);
        
        // 底部按钮面板
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 20, 15));  // 右对齐布局
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        
        // 登出按钮
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
                stopAutoRefreshTimer();  // 停止定时器
                mainFrame.dispose();     // 关闭主界面
                // 重新打开登录选择界面
                LoginChooserFrame.main(new String[0]);
            }
        });
        
        bottomPanel.add(logoutButton);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);
        
        // 表格行选中监听器
        // 当选中一行时启用删除按钮，未选中时禁用
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {  // 防止事件重复触发
                boolean rowSelected = subjectTable.getSelectedRow() >= 0;
                deleteSubjectButton.setEnabled(rowSelected);
            }
        });
        
        // 显示窗口并启动定时器
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        startAutoRefreshTimer();
    }
    
    // 从学生的个人课程文件加载课程数据到表格
    private static void loadStudentSubjectDataToTable() {
        tableModel.setRowCount(0);  // 清空现有数据
        
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(currentStudentSubjectFilePath);
            if (!csvFile.exists()) {
                System.out.println("Student subject file does not exist: " + currentStudentSubjectFilePath);
                return;
            }
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] row = parseCSVLine(line);
                
                // 跳过表头行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 确保行有足够的列数
                if (row.length >= 5) {
                    Vector<String> rowData = new Vector<>();
                    rowData.add(cleanField(row[0]));
                    rowData.add(cleanField(row[1]));
                    rowData.add(cleanField(row[2]));
                    rowData.add(cleanField(row[3]));
                    rowData.add(cleanField(row[4]));
                    tableModel.addRow(rowData);
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
        
        System.out.println("Loaded " + tableModel.getRowCount() + " subjects for student: " + currentStudentId);
    }
    
    // 从学生的个人课程文件中删除指定课程
    private static boolean deleteSubjectFromStudentFile(String subjectId) {
        List<String[]> allRows = new ArrayList<>();
        BufferedReader reader = null;
        boolean found = false;
        boolean isFirstLine = true;
        String[] headerRow = null;
        
        try {
            File csvFile = new File(currentStudentSubjectFilePath);
            if (!csvFile.exists()) {
                System.out.println("Student subject file does not exist: " + currentStudentSubjectFilePath);
                return false;
            }
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            
            // 读取所有行
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    allRows.add(new String[0]);
                    continue;
                }
                
                String[] row = parseCSVLine(line);
                
                if (isFirstLine) {
                    // 保存表头行
                    headerRow = row;
                    isFirstLine = false;
                    allRows.add(row);
                    continue;
                }
                
                // 检查是否是要删除的课程
                if (row.length >= 1) {
                    String csvSubjectId = cleanField(row[0]);
                    if (csvSubjectId.equals(subjectId)) {
                        found = true;
                        System.out.println("Found subject to delete: " + subjectId);
                        continue;  // 跳过这一行（不添加到allRows中）
                    }
                }
                allRows.add(row);  // 保留非目标课程行
            }
            reader.close();
            
            if (!found) {
                System.out.println("Subject not found: " + subjectId);
                return false;
            }
            
            // 将剩余行写回文件
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            
            for (int i = 0; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                if (row.length == 0) {
                    writer.newLine();
                    continue;
                }
                
                StringBuilder lineBuilder = new StringBuilder();
                for (int j = 0; j < row.length; j++) {
                    if (j > 0) {
                        lineBuilder.append(",");
                    }
                    String field = row[j];
                    if (field == null) field = "";
                    // 对包含特殊字符的字段进行转义
                    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                        lineBuilder.append("\"").append(field.replace("\"", "\"\"")).append("\"");
                    } else {
                        lineBuilder.append(field);
                    }
                }
                writer.write(lineBuilder.toString());
                if (i < allRows.size() - 1) {
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

    // 显示修改密码对话框
    private static void showChangePasswordDialog(JFrame parentFrame) {
        // 创建模态对话框
        JDialog passwordDialog = new JDialog(parentFrame, "Change Password", true);
        passwordDialog.setSize(400, 300);
        passwordDialog.setLayout(new BorderLayout());
        passwordDialog.setLocationRelativeTo(parentFrame);
        
        // 创建表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        Font labelFont = new Font("Times New Roman", Font.PLAIN, 14);
        Font fieldFont = new Font("Times New Roman", Font.PLAIN, 14);
        
        // 当前密码输入行
        JLabel currentPwdLabel = new JLabel("Current Password:");
        currentPwdLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(currentPwdLabel, gbc);
        
        JPasswordField currentPwdField = new JPasswordField(15);
        currentPwdField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(currentPwdField, gbc);
        
        // 新密码输入行
        JLabel newPwdLabel = new JLabel("New Password:");
        newPwdLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(newPwdLabel, gbc);
        
        JPasswordField newPwdField = new JPasswordField(15);
        newPwdField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(newPwdField, gbc);
        
        // 确认新密码输入行
        JLabel confirmPwdLabel = new JLabel("Confirm New Password:");
        confirmPwdLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(confirmPwdLabel, gbc);
        
        JPasswordField confirmPwdField = new JPasswordField(15);
        confirmPwdField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(confirmPwdField, gbc);
        
        passwordDialog.add(formPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        // 确认按钮
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setBackground(Color.BLUE);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorderPainted(false);
        confirmButton.setOpaque(true);
        confirmButton.addActionListener(e -> {
            String currentPwd = new String(currentPwdField.getPassword());
            String newPwd = new String(newPwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());
            
            // 验证输入不能为空
            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                JOptionPane.showMessageDialog(passwordDialog, 
                    "Please fill in all fields!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证新密码长度至少6位
            if (newPwd.length() < 6) {
                JOptionPane.showMessageDialog(passwordDialog, 
                    "New password must be at least 6 characters long!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证两次新密码输入一致
            if (!newPwd.equals(confirmPwd)) {
                JOptionPane.showMessageDialog(passwordDialog, 
                    "New passwords do not match!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证当前密码是否正确
            if (!validateCurrentPassword(currentStudentEmail, currentPwd)) {
                JOptionPane.showMessageDialog(passwordDialog, 
                    "Current password is incorrect!\nPlease try again.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 更新CSV文件中的密码
            boolean updateSuccess = updatePasswordInCSV(currentStudentEmail, newPwd);
            
            if (updateSuccess) {
                JOptionPane.showMessageDialog(passwordDialog, 
                    "Password changed successfully!\nPlease login again with your new password.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                passwordDialog.dispose();
                parentFrame.dispose();
                LoginChooserFrame.main(new String[0]);
            } else {
                JOptionPane.showMessageDialog(passwordDialog, 
                    "Failed to change password!\nPlease contact the administrator.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // 取消按钮
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.ORANGE);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.addActionListener(e -> passwordDialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        passwordDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        passwordDialog.setVisible(true);
    }
    
    // 验证当前密码是否正确
    private static boolean validateCurrentPassword(String email, String password) {
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(STUDENT_CSV_FILE_PATH);
            if (!csvFile.exists()) {
                return false;
            }
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // 跳过表头
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] row = parseCSVLine(line);
                if (row.length >= 4) {
                    String csvEmail = cleanField(row[2]);
                    String csvPassword = cleanField(row[3]);
                    
                    if (csvEmail.equalsIgnoreCase(email)) {
                        return csvPassword.equals(password);
                    }
                }
            }
            return false;
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
    
    // 更新CSV文件中的密码
    private static boolean updatePasswordInCSV(String email, String newPassword) {
        List<String[]> allRows = new ArrayList<>();
        BufferedReader reader = null;
        boolean found = false;
        boolean isFirstLine = true;
        
        try {
            File csvFile = new File(STUDENT_CSV_FILE_PATH);
            if (!csvFile.exists()) {
                return false;
            }
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    allRows.add(new String[0]);
                    continue;
                }
                
                String[] row = parseCSVLine(line);
                
                if (isFirstLine) {
                    isFirstLine = false;
                    allRows.add(row);
                    continue;
                }
                
                if (row.length >= 4) {
                    String csvEmail = cleanField(row[2]);
                    if (csvEmail.equalsIgnoreCase(email)) {
                        found = true;
                        row[3] = newPassword;  // 更新密码列
                        System.out.println("Password updated for: " + email);
                    }
                }
                allRows.add(row);
            }
            reader.close();
            
            if (!found) {
                System.out.println("User not found for password update: " + email);
                return false;
            }
            
            // 写回文件
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            
            for (int i = 0; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                if (row.length == 0) {
                    writer.newLine();
                    continue;
                }
                
                StringBuilder lineBuilder = new StringBuilder();
                for (int j = 0; j < row.length; j++) {
                    if (j > 0) {
                        lineBuilder.append(",");
                    }
                    String field = row[j];
                    if (field == null) field = "";
                    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                        lineBuilder.append("\"").append(field.replace("\"", "\"\"")).append("\"");
                    } else {
                        lineBuilder.append(field);
                    }
                }
                writer.write(lineBuilder.toString());
                if (i < allRows.size() - 1) {
                    writer.newLine();
                }
            }
            writer.close();
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
    
    // 解析CSV行
    private static String[] parseCSVLine(String line) {
        java.util.ArrayList<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;  // 标记是否在引号内部
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 处理转义引号：两个连续的双引号表示一个双引号字符
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;  // 跳过下一个引号
                } else {
                    inQuotes = !inQuotes;  // 切换引号状态
                }
            } else if (c == ',' && !inQuotes) {
                // 不在引号内的逗号表示字段分隔符
                fields.add(currentField.toString());
                currentField.setLength(0);  // 清空StringBuilder
            } else {
                currentField.append(c);  // 添加普通字符
            }
        }
        fields.add(currentField.toString());  // 添加最后一个字段
        
        return fields.toArray(new String[0]);
    }
    
    // 清理字段值（去除首尾空格和引号）
    private static String cleanField(String field) {
        if (field == null) {
            return "";
        }
        String cleaned = field.trim();
        // 如果字段被引号包围，去除首尾的引号
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
    
    // 刷新学生主界面的课程表格
    public static void refreshSubjectTable() {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null && mainFrame.isDisplayable()) {
                loadStudentSubjectDataToTable();
                mainFrame.repaint();
                System.out.println("Subject table refreshed at: " + new java.util.Date());
            }
        });
    }
}