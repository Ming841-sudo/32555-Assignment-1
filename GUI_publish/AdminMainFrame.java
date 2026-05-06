import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

// 管理员主界面
public class AdminMainFrame {
    
    // 学生CSV文件保存路径
    private static final String CSV_FILE_PATH = "StudentInfo.csv";
    // 课程CSV文件保存路径
    private static final String SUBJECT_CSV_FILE_PATH = "SubjectInfo.csv";
    // 学生个人课程文件夹路径
    private static final String STUDENTS_FOLDER_PATH = "Students";
    
    // 表格模型，用于管理表格数据
    private static DefaultTableModel tableModel;
    // 学生信息表格组件，用于显示和交互
    private static JTable studentTable;
    // 管理员主窗口引用，用于其他方法访问
    private static JFrame adminFrame;
    
    // 打印当前工作目录
    private static void printWorkingDirectory() {
        // System.getProperty("user.dir") 获取Java虚拟机启动时的当前工作目录
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        System.out.println("Student CSV file path: " + new File(CSV_FILE_PATH).getAbsolutePath());
        System.out.println("Subject CSV file path: " + new File(SUBJECT_CSV_FILE_PATH).getAbsolutePath());
        System.out.println("Students folder path: " + new File(STUDENTS_FOLDER_PATH).getAbsolutePath());
    }
    
    // 获取学生的个人课程文件路径
    private static String getStudentCourseFilePath(String studentId) {
        return STUDENTS_FOLDER_PATH + File.separator + "Student-" + studentId + ".csv";
    }
    
    // 删除学生的个人课程文件
    private static boolean deleteStudentCourseFile(String studentId) {
        String filePath = getStudentCourseFilePath(studentId);
        File courseFile = new File(filePath);
        
        // 文件不存在，无需删除，视为成功
        if (!courseFile.exists()) {
            System.out.println("Student course file does not exist: " + filePath);
            return true;
        }
        
        // 执行删除操作
        boolean deleted = courseFile.delete();
        if (deleted) {
            System.out.println("Deleted student course file: " + filePath);
        } else {
            System.err.println("Failed to delete student course file: " + filePath);
        }
        return deleted;
    }
    
    // 显示管理员主界面
    public static void showAdminMain() {
        // 打印当前工作目录和文件路径
        printWorkingDirectory();
        
        // 创建主窗口
        adminFrame = new JFrame("Admin Main Page");
        adminFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭窗口时退出程序
        adminFrame.setSize(1000, 600);
        adminFrame.setLayout(new BorderLayout());  // 使用边界布局管理器
        
        // 创建顶部欢迎面板
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(Color.PINK);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 欢迎标签
        JLabel welcomeLabel = new JLabel("Welcome, Administrator!", SwingConstants.CENTER);
        Font welcomeFont = new Font("Times New Roman", Font.BOLD, 36);
        welcomeLabel.setFont(welcomeFont);
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        
        adminFrame.add(welcomePanel, BorderLayout.NORTH);  // 添加到窗口顶部
        
        // 创建主内容面板
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        
        // 左侧学生信息表格
        String[] columnNames = {"Student ID", "Student Name", "Email", "Password", "Registration Date"};
        
        // 创建表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止编辑表格单元格，保证数据完整性
            }
        };
        
        // 创建表格组件
        studentTable = new JTable(tableModel);
        studentTable.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        studentTable.setRowHeight(25);
        // 设置表头样式
        studentTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 18));
        studentTable.getTableHeader().setBackground(Color.BLUE);
        studentTable.getTableHeader().setForeground(Color.WHITE);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单选模式，一次只能选中一行
        
        // 从CSV文件加载数据到表格
        loadStudentDataToTable();
        
        // 添加鼠标双击监听器
        // 双击学生行时打开该学生的详情窗口
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // 检测双击事件
                    int selectedRow = studentTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        // 获取选中行的学生信息
                        String studentId = (String) tableModel.getValueAt(selectedRow, 0);
                        String studentName = (String) tableModel.getValueAt(selectedRow, 1);
                        String studentEmail = (String) tableModel.getValueAt(selectedRow, 2);
                        
                        // 打开学生详情窗口
                        AdminOverviewFrame.showStudentOverview(adminFrame, studentId, studentName, studentEmail);
                    }
                }
            }
        });
        
        // 创建滚动面板，包裹表格
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Student Information List (Double-click to view details)"));
        scrollPane.setPreferredSize(new Dimension(650, 450));
        
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 右侧操作按钮面板
        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new GridBagLayout());  // 使用网格包布局
        rightButtonPanel.setBackground(Color.WHITE);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
        
        // 创建布局约束对象
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;  // 所有按钮放在第0列
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;  // 按钮水平方向填满
        
        // 按钮统一字体和大小
        Font buttonFont = new Font("Times New Roman", Font.BOLD, 18);
        Dimension buttonSize = new Dimension(180, 45);
        
        // 删除学生
        JButton deleteStudentButton = new JButton("Delete Student");
        deleteStudentButton.setFont(buttonFont);
        deleteStudentButton.setPreferredSize(buttonSize);
        deleteStudentButton.setBackground(Color.ORANGE);
        deleteStudentButton.setForeground(Color.BLACK);
        deleteStudentButton.setFocusPainted(false);  // 禁用焦点边框
        deleteStudentButton.setBorderPainted(false); // 禁用默认边框
        deleteStudentButton.setOpaque(true);         // 设置不透明
        deleteStudentButton.setEnabled(false);       // 初始状态禁用，选中行后才启用
        
        // 删除学生按钮的点击事件
        deleteStudentButton.addActionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow >= 0) {
                // 获取选中行的学生信息
                String studentId = (String) tableModel.getValueAt(selectedRow, 0);
                String studentName = (String) tableModel.getValueAt(selectedRow, 1);
                
                // 弹出确认对话框，提示用户确认删除（将同时删除个人课程文件）
                int confirm = JOptionPane.showConfirmDialog(adminFrame, 
                    "Are you sure you want to delete student:\n" + studentId + " - " + studentName + "?\n\n" +
                    "This will also delete the student's course file from the Students folder.",
                    "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleteSuccess = deleteStudentFromCSV(studentId);
                    boolean fileDeleted = deleteStudentCourseFile(studentId);
                    
                    if (deleteSuccess) {
                        // 从表格中删除该行
                        tableModel.removeRow(selectedRow);
                        
                        if (fileDeleted) {
                            JOptionPane.showMessageDialog(adminFrame, 
                                "Student deleted successfully!\n\n" +
                                "Student record removed from StudentInfo.csv.\n" +
                                "Student course file removed from Students folder.",
                                "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(adminFrame, 
                                "Student deleted from StudentInfo.csv successfully!\n\n" +
                                "Warning: Student course file could not be deleted.\n" +
                                "You may need to manually delete: Students/Student-" + studentId + ".csv",
                                "Warning", 
                                JOptionPane.WARNING_MESSAGE);
                        }
                        deleteStudentButton.setEnabled(false); // 删除后禁用删除按钮
                    } else {
                        JOptionPane.showMessageDialog(adminFrame, 
                            "Failed to delete student from StudentInfo.csv!", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(adminFrame, 
                    "Please select a student to delete first!", 
                    "Warning", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        gbc.gridy = 0;
        rightButtonPanel.add(deleteStudentButton, gbc);
        
        // 清空所有数据
        JButton clearDataButton = new JButton("Clear Data");
        clearDataButton.setFont(buttonFont);
        clearDataButton.setPreferredSize(buttonSize);
        clearDataButton.setBackground(Color.YELLOW);
        clearDataButton.setForeground(Color.BLACK);
        clearDataButton.setFocusPainted(false);
        clearDataButton.setBorderPainted(false);
        clearDataButton.setOpaque(true);
        
        clearDataButton.addActionListener(e -> {
            // 警告对话框，需要用户二次确认
            int confirm = JOptionPane.showConfirmDialog(adminFrame, 
                "WARNING: This will delete ALL student data AND all student course files!\n\n" +
                "All student records in StudentInfo.csv and all files in Students folder will be deleted.\n" +
                "Are you absolutely sure?",
                "Confirm Clear All Data", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean clearSuccess = clearAllStudentData();
                boolean clearFolderSuccess = clearAllStudentCourseFiles();
                
                if (clearSuccess) {
                    // 清空表格中的所有行
                    tableModel.setRowCount(0);
                    
                    if (clearFolderSuccess) {
                        JOptionPane.showMessageDialog(adminFrame, 
                            "All student data has been cleared successfully!\n\n" +
                            "All student records removed from StudentInfo.csv.\n" +
                            "All student course files removed from Students folder.",
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(adminFrame, 
                            "Student data cleared from StudentInfo.csv!\n\n" +
                            "Warning: Some student course files could not be deleted.\n" +
                            "You may need to manually clear the Students folder.",
                            "Warning", 
                            JOptionPane.WARNING_MESSAGE);
                    }
                    deleteStudentButton.setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(adminFrame, 
                        "Failed to clear data!", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        gbc.gridy = 1;
        rightButtonPanel.add(clearDataButton, gbc);
        
        // 添加课程
        JButton addSubjectButton = new JButton("Add Subject");
        addSubjectButton.setFont(buttonFont);
        addSubjectButton.setPreferredSize(buttonSize);
        addSubjectButton.setBackground(Color.CYAN);
        addSubjectButton.setForeground(Color.BLACK);
        addSubjectButton.setFocusPainted(false);
        addSubjectButton.setBorderPainted(false);
        addSubjectButton.setOpaque(true);
        addSubjectButton.addActionListener(e -> {
            // 打开添加课程对话框
            AddSubjectFrame.showAddSubjectDialog(adminFrame);
        });
        gbc.gridy = 2;  // 第2行
        rightButtonPanel.add(addSubjectButton, gbc);
        
        // 按成绩排序
        JButton sortByGradeButton = new JButton("Sort by Grade");
        sortByGradeButton.setFont(buttonFont);
        sortByGradeButton.setPreferredSize(buttonSize);
        sortByGradeButton.setBackground(Color.GREEN);
        sortByGradeButton.setForeground(Color.BLACK);
        sortByGradeButton.setFocusPainted(false);
        sortByGradeButton.setBorderPainted(false);
        sortByGradeButton.setOpaque(true);
        sortByGradeButton.addActionListener(e -> {
            // 打开学生成绩排名窗口
            StudentRankFrame.showStudentRank(adminFrame);
        }); 
        gbc.gridy = 3;
        rightButtonPanel.add(sortByGradeButton, gbc);
        
        mainContentPanel.add(rightButtonPanel, BorderLayout.EAST);  // 添加到主内容面板右侧
        adminFrame.add(mainContentPanel, BorderLayout.CENTER);
        
        // ----- 4. 底部按钮面板（登出按钮）-----
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
            int confirm = JOptionPane.showConfirmDialog(adminFrame, 
                "Are you sure you want to logout?", 
                "Logout Confirmation", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                adminFrame.dispose();  // 关闭当前窗口
                // 重新打开登录选择界面
                LoginChooserFrame.main(new String[0]);
            }
        });
        
        bottomPanel.add(logoutButton);
        adminFrame.add(bottomPanel, BorderLayout.SOUTH);
        
        // 表格行选中监听器
        // 当用户在表格中选中一行时，启用删除按钮；没有选中时禁用删除按钮
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {  // 防止事件重复触发
                boolean rowSelected = studentTable.getSelectedRow() >= 0;
                deleteStudentButton.setEnabled(rowSelected);
            }
        });
        
        // 显示窗口
        adminFrame.setLocationRelativeTo(null);
        adminFrame.setVisible(true);
    }
    
    // 清空所有学生课程文件（删除Students文件夹中的所有文件）
    private static boolean clearAllStudentCourseFiles() {
        File studentsFolder = new File(STUDENTS_FOLDER_PATH);
        
        // 文件夹不存在，无需删除
        if (!studentsFolder.exists()) {
            System.out.println("Students folder does not exist, nothing to delete.");
            return true;
        }
        
        // 获取文件夹中的所有文件
        File[] files = studentsFolder.listFiles();
        if (files == null) {
            return true;
        }
        
        boolean allDeleted = true;
        // 遍历并删除每个文件
        for (File file : files) {
            if (file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("Deleted: " + file.getName());
                } else {
                    System.err.println("Failed to delete: " + file.getName());
                    allDeleted = false;
                }
            }
        }
        return allDeleted;
    }
    
    // 检查课程编号是否已存在
    public static boolean isSubjectIdExists(String subjectId) {
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            if (!csvFile.exists()) {
                return false;  // 文件不存在，ID肯定不存在
            }
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                isFirstLine = false;
                
                String[] row = parseCSVLine(line);
                if (row.length >= 1) {
                    String existingSubjectId = cleanField(row[0]);
                    if (existingSubjectId.equalsIgnoreCase(subjectId)) {
                        return true;  // 找到匹配的ID
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
    
    // 将课程信息保存到CSV文件（追加模式）
    public static boolean saveSubjectToCSV(String subjectName, String subjectId, int credits) {
        BufferedWriter writer = null;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            boolean isNewFile = !csvFile.exists();
            
            // 确保文件所在目录存在
            File parentDir = csvFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();  // 创建不存在的父目录
            }
            
            // 使用追加模式(true)写入文件，保留已有数据
            writer = new BufferedWriter(new FileWriter(csvFile, true));
            
            // 如果是新文件，先写入表头
            if (isNewFile) {
                writer.write("Subject ID,Subject Name,Credits,Added Date");
                writer.newLine();
            }
            
            // 获取当前时间戳
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            
            // 构建CSV行数据
            String line = String.format("%s,%s,%d,%s",
                escapeCSV(subjectId),
                escapeCSV(subjectName),
                credits,
                escapeCSV(timestamp));
            
            writer.write(line);
            writer.newLine();
            
            System.out.println("Subject saved: " + subjectId + " - " + subjectName);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 生成唯一的3位数课程ID
    public static String generateUniqueSubjectId() {
        // 获取所有已存在的课程ID
        Set<String> existingIds = getAllExistingSubjectIds();
        
        Random random = new Random();
        String newId;
        int attempts = 0;
        int maxAttempts = 10000; // 防止无限循环
        
        do {
            // 生成1到999之间的随机数
            int idNumber = random.nextInt(999) + 1; // nextInt(999)返回0-998，+1后变成1-999
            // 格式化为3位数，不足前面补0
            newId = String.format("%03d", idNumber);
            attempts++;
            
            // 如果随机尝试次数过多，改用顺序查找方式
            if (attempts > maxAttempts) {
                for (int i = 1; i <= 999; i++) {
                    String candidateId = String.format("%03d", i);
                    if (!existingIds.contains(candidateId)) {
                        return candidateId;
                    }
                }
                // 理论上不可能所有ID都被占用
                return String.format("%03d", System.currentTimeMillis() % 1000);
            }
        } while (existingIds.contains(newId));  // 如果ID已存在，继续循环生成新ID
        
        return newId;
    }
    
    // 获取CSV文件中所有已存在的课程ID
    public static Set<String> getAllExistingSubjectIds() {
        Set<String> existingIds = new HashSet<>();
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            if (!csvFile.exists()) {
                return existingIds;
            }
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                isFirstLine = false;
                
                String[] row = parseCSVLine(line);
                if (row.length >= 1) {
                    String subjectId = cleanField(row[0]);
                    if (!subjectId.isEmpty()) {
                        existingIds.add(subjectId);
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
        
        return existingIds;
    }
    
    // 从CSV文件加载学生数据到表格
    private static void loadStudentDataToTable() {
        tableModel.setRowCount(0);  // 清空现有数据
        
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(CSV_FILE_PATH);
            if (!csvFile.exists()) {
                System.out.println("Student CSV file does not exist: " + csvFile.getAbsolutePath());
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
    }
    
    // 从CSV文件中删除指定学生
    private static boolean deleteStudentFromCSV(String studentId) {
        List<String[]> allRows = new ArrayList<>();
        BufferedReader reader = null;
        boolean found = false;
        
        try {
            File csvFile = new File(CSV_FILE_PATH);
            if (!csvFile.exists()) {
                return false;
            }
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            String[] headerRow = null;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
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
                
                if (row.length >= 1) {
                    String csvStudentId = cleanField(row[0]);
                    if (csvStudentId.equals(studentId)) {
                        found = true;
                        continue;  // 找到要删除的学生，跳过这一行（不添加到allRows中）
                    }
                }
                allRows.add(row);  // 保留非目标学生行
            }
            reader.close();
            
            if (!found) {
                return false;  // 未找到要删除的学生
            }
            
            // 将剩余行写回文件
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            
            for (int i = 0; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
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
    
    // 清空所有学生数据（保留表头）
    private static boolean clearAllStudentData() {
        try {
            File csvFile = new File(CSV_FILE_PATH);
            
            // 只保留表头，删除所有数据行
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            writer.write("Student ID,Username,Email,Password,Registration Date");
            writer.newLine();
            writer.close();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 对CSV字段进行转义（处理逗号和引号）
    private static String escapeCSV(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    // 解析CSV行
    private static String[] parseCSVLine(String line) {
        java.util.ArrayList<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;  // 标记当前是否在引号内部
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 处理转义引号（两个连续的双引号表示一个双引号字符）
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
                currentField.append(c);
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
}