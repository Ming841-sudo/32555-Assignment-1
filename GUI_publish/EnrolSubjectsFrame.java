import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

// 学生选课界面类
public class EnrolSubjectsFrame {
    
    // 课程CSV文件保存路径（存储系统中所有课程信息）
    private static final String SUBJECT_CSV_FILE_PATH = "SubjectInfo.csv";
    // 学生个人课程文件夹路径（每个学生独立存放注册课程记录）
    private static final String STUDENTS_FOLDER_PATH = "Students";
    // 当前学生的个人课程文件路径
    private static String currentStudentSubjectFilePath;
    
    // 当前登录的学生邮箱
    private static String currentStudentEmail;
    // 当前登录的学生ID（6位数字）
    private static String currentStudentId;
    // 当前登录的学生姓名
    private static String currentStudentName;
    // 表格模型，用于管理表格数据
    private static DefaultTableModel tableModel;
    // 课程表格组件，用于显示可选课程列表
    private static JTable subjectTable;
    // 选课窗口引用
    private static JFrame enrolFrame;
    // 定时器，用于自动刷新课程列表
    private static Timer refreshTimer;
    // 记录上一次的课程数量，用于检测变化
    private static int lastSubjectCount = -1;
    
    // 打印当前工作目录
    private static void printWorkingDirectory() {
        // System.getProperty("user.dir") 获取Java虚拟机启动时的当前工作目录
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        System.out.println("Subject CSV file path: " + new File(SUBJECT_CSV_FILE_PATH).getAbsolutePath());
        System.out.println("Students folder path: " + new File(STUDENTS_FOLDER_PATH).getAbsolutePath());
    }
    
    // 加载当前登录学生的基本信息
    private static void loadCurrentStudentInfo() {
        BufferedReader reader = null;
        
        try {
            File csvFile = new File("StudentInfo.csv");
            if (!csvFile.exists()) {
                System.err.println("StudentInfo.csv not found!");
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
    
    // 确保学生的个人课程文件存在，如果不存在则创建
    private static void ensureStudentSubjectFileExists() {
        File subjectFile = new File(currentStudentSubjectFilePath);
        
        // 确保Students文件夹存在
        File studentsFolder = new File(STUDENTS_FOLDER_PATH);
        if (!studentsFolder.exists()) {
            studentsFolder.mkdirs();
        }
        
        // 如果文件不存在，创建并写入表头
        if (!subjectFile.exists()) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(subjectFile));
                writer.write("Subject ID,Subject Name,Credits,Score,Grade");
                writer.newLine();
                writer.close();
                System.out.println("Created student subject file: " + subjectFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 检测课程数据是否有变化并自动刷新
    private static void checkAndRefreshIfChanged() {
        int currentCount = getCurrentSubjectCountInSystem();
        
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
    
    // 获取系统中课程数量
    private static int getCurrentSubjectCountInSystem() {
        BufferedReader reader = null;
        int count = 0;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
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
                if (enrolFrame != null && enrolFrame.isDisplayable()) {
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
    
    // 获取学生已注册的课程数量
    private static int getStudentSubjectCount() {
        BufferedReader reader = null;
        int count = 0;
        
        try {
            File subjectFile = new File(currentStudentSubjectFilePath);
            if (!subjectFile.exists()) {
                return 0;
            }
            
            reader = new BufferedReader(new FileReader(subjectFile));
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
    
    // 检查学生是否已经注册过该课程
    private static boolean isSubjectAlreadyEnrolled(String subjectId) {
        BufferedReader reader = null;
        
        try {
            File subjectFile = new File(currentStudentSubjectFilePath);
            if (!subjectFile.exists()) {
                return false;
            }
            
            reader = new BufferedReader(new FileReader(subjectFile));
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
                
                String[] row = parseCSVLine(line);
                if (row.length >= 1) {
                    String existingSubjectId = cleanField(row[0]);
                    if (existingSubjectId.equals(subjectId)) {
                        return true;  // 找到匹配的课程ID
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
    
    // 将课程信息写入学生个人CSV文件的第一个空行
    private static boolean saveSubjectToStudentFile(String subjectId, String subjectName, int credits, int score, String grade) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        List<String> allLines = new ArrayList<>();
        
        try {
            File subjectFile = new File(currentStudentSubjectFilePath);
            if (!subjectFile.exists()) {
                System.err.println("Student subject file does not exist: " + currentStudentSubjectFilePath);
                return false;
            }
            
            // 读取所有行
            reader = new BufferedReader(new FileReader(subjectFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
                isFirstLine = false;
            }
            reader.close();
            
            // 跳过表头，寻找第一个空行位置
            int emptyRowIndex = -1;
            for (int i = 1; i < allLines.size(); i++) {
                if (allLines.get(i).trim().isEmpty()) {
                    emptyRowIndex = i;
                    break;
                }
            }
            
            // 构建新课程数据行
            String newSubjectLine = String.format("%s,%s,%d,%d,%s",
                escapeCSV(subjectId),
                escapeCSV(subjectName),
                credits,
                score,
                escapeCSV(grade));
            
            if (emptyRowIndex != -1) {
                // 找到空行，替换该空行
                allLines.set(emptyRowIndex, newSubjectLine);
            } else {
                // 没有找到空行，追加到文件末尾
                allLines.add(newSubjectLine);
            }
            
            // 写回文件（覆盖写入）
            writer = new BufferedWriter(new FileWriter(subjectFile));
            
            for (int i = 0; i < allLines.size(); i++) {
                writer.write(allLines.get(i));
                if (i < allLines.size() - 1) {
                    writer.newLine();
                }
            }
            writer.close();
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 保存课程信息
    private static boolean saveNewSubjectToSystem(String subjectName, String subjectId, int credits) {
        BufferedWriter writer = null;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            boolean isNewFile = !csvFile.exists();
            
            // 确保文件所在目录存在
            File parentDir = csvFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 使用追加模式写入文件
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
            writer.close();
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 刷新课程表格
    private static void refreshSubjectTable() {
        SwingUtilities.invokeLater(() -> {
            if (enrolFrame != null && enrolFrame.isDisplayable()) {
                loadSubjectDataToTable();
                enrolFrame.repaint();
                System.out.println("Subject table refreshed at: " + new java.util.Date());
            }
        });
    }
    
    // 加载课程数据到表格
    private static void loadSubjectDataToTable() {
        tableModel.setRowCount(0);  // 清空现有数据
        
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            if (!csvFile.exists()) {
                System.out.println("Subject CSV file does not exist: " + SUBJECT_CSV_FILE_PATH);
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
                if (row.length >= 3) {
                    Vector<String> rowData = new Vector<>();
                    rowData.add(cleanField(row[0])); // Subject ID
                    rowData.add(cleanField(row[1])); // Subject Name
                    rowData.add(cleanField(row[2])); // Credits
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
        
        System.out.println("Loaded " + tableModel.getRowCount() + " subjects from system");
    }
    
    // 显示学生选课界面
    public static void showEnrolSubjects(String studentEmail) {
        printWorkingDirectory();
        
        currentStudentEmail = studentEmail;
        
        // 加载学生基本信息
        loadCurrentStudentInfo();
        // 设置个人课程文件路径
        setCurrentStudentSubjectFilePath();
        // 确保个人课程文件存在
        ensureStudentSubjectFileExists();
        
        // 创建选课窗口
        enrolFrame = new JFrame("Enrol Subjects - " + currentStudentName);
        enrolFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        enrolFrame.setSize(1000, 600);
        enrolFrame.setLayout(new BorderLayout());
        
        // 添加窗口关闭监听器，停止定时器并刷新主界面
        enrolFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopAutoRefreshTimer();
                refreshStudentMainFrame();
            }
        });
        
        // 顶部欢迎面板
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(Color.PINK);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Enrol New Subjects", SwingConstants.CENTER);
        Font welcomeFont = new Font("Times New Roman", Font.BOLD, 36);
        welcomeLabel.setFont(welcomeFont);
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        
        enrolFrame.add(welcomePanel, BorderLayout.NORTH);
        
        // 主内容面板
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        
        // 左侧课程信息表格
        String[] columnNames = {"Subject ID", "Subject Name", "Credits"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // 禁止编辑表格单元格
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
        
        // 加载课程数据
        loadSubjectDataToTable();
        lastSubjectCount = tableModel.getRowCount();
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Subjects"));
        scrollPane.setPreferredSize(new Dimension(650, 450));
        
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 右侧操作按钮面板
        JPanel rightButtonPanel = new JPanel();
        rightButtonPanel.setLayout(new GridBagLayout());
        rightButtonPanel.setBackground(Color.WHITE);
        rightButtonPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;  // 所有按钮放在第0列
        gbc.insets = new Insets(15, 0, 15, 0);  // 按钮垂直间距
        gbc.fill = GridBagConstraints.HORIZONTAL;  // 水平填满
        
        Font buttonFont = new Font("Times New Roman", Font.BOLD, 18);
        Dimension buttonSize = new Dimension(180, 45);
        
        // 注册课程按钮
        JButton enrolButton = new JButton("Enrol Selected");
        enrolButton.setFont(buttonFont);
        enrolButton.setPreferredSize(buttonSize);
        enrolButton.setBackground(Color.ORANGE);
        enrolButton.setForeground(Color.BLACK);
        enrolButton.setFocusPainted(false);
        enrolButton.setBorderPainted(false);
        enrolButton.setOpaque(true);
        enrolButton.setEnabled(false);  // 初始禁用，选中课程后才启用
        
        enrolButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow >= 0) {
                // 获取选中课程的信息
                String subjectId = (String) tableModel.getValueAt(selectedRow, 0);
                String subjectName = (String) tableModel.getValueAt(selectedRow, 1);
                String creditsStr = (String) tableModel.getValueAt(selectedRow, 2);
                int credits = 3;
                try {
                    credits = Integer.parseInt(creditsStr);
                } catch (NumberFormatException ex) {
                    credits = 3;
                }
                
                // 检查选课数量是否已达上限
                int currentSubjectCount = getStudentSubjectCount();
                if (currentSubjectCount >= 4) {
                    JOptionPane.showMessageDialog(enrolFrame, 
                        "You have already enrolled in 4 subjects!\nMaximum limit reached.",
                        "Cannot Enrol", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // 检查是否已注册过该课程
                if (isSubjectAlreadyEnrolled(subjectId)) {
                    JOptionPane.showMessageDialog(enrolFrame, 
                        "You have already enrolled in this subject!",
                        "Already Enrolled", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // 生成随机分数和等级
                int randomScore = generateRandomScore();
                String gradeLetter = getGradeLetter(randomScore);
                
                // 保存到学生个人课程文件
                boolean saveSuccess = saveSubjectToStudentFile(subjectId, subjectName, credits, randomScore, gradeLetter);
                
                if (saveSuccess) {
                    refreshStudentMainFrame();  // 刷新主界面
                    JOptionPane.showMessageDialog(enrolFrame, 
                        "Subject enrolled successfully!\n\nSubject ID: " + subjectId + 
                        "\nSubject Name: " + subjectName +
                        "\nCredits: " + credits +
                        "\nScore: " + randomScore +
                        "\nGrade: " + gradeLetter,
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(enrolFrame, 
                        "Failed to enrol subject!", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(enrolFrame, 
                    "Please select a subject to enrol first!", 
                    "Warning", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        gbc.gridy = 0;
        rightButtonPanel.add(enrolButton, gbc);
        
        // 随机注册课程按钮
        JButton randomEnrolButton = new JButton("Random Enrol");
        randomEnrolButton.setFont(buttonFont);
        randomEnrolButton.setPreferredSize(buttonSize);
        randomEnrolButton.setBackground(Color.CYAN);
        randomEnrolButton.setForeground(Color.BLACK);
        randomEnrolButton.setFocusPainted(false);
        randomEnrolButton.setBorderPainted(false);
        randomEnrolButton.setOpaque(true);
        
        randomEnrolButton.addActionListener(e -> {
            // 检查选课数量是否已达上限
            int currentSubjectCount = getStudentSubjectCount();
            if (currentSubjectCount >= 4) {
                JOptionPane.showMessageDialog(enrolFrame, 
                    "You have already enrolled in 4 subjects!\nMaximum limit reached.",
                    "Cannot Enrol", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 生成随机课程ID（1-999）
            Random random = new Random();
            int idNumber = random.nextInt(999) + 1;
            String newId = String.format("%03d", idNumber);
            
            // 检查课程是否已存在于系统中
            String existingName = getSubjectNameById(newId);
            boolean exists = !existingName.equals("Unknown Subject");
            int existingCredits = exists ? getSubjectCreditsById(newId) : 3;
            
            String newName;
            if (exists) {
                newName = existingName;
            } else {
                newName = "Subject-" + newId;
                saveNewSubjectToSystem(newName, newId, 3);  // 将新课程添加到系统
            }
            
            // 检查是否已注册过该课程
            if (isSubjectAlreadyEnrolled(newId)) {
                JOptionPane.showMessageDialog(enrolFrame, 
                    "You have already enrolled in this subject!\nSubject ID: " + newId,
                    "Already Enrolled", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 生成随机分数和等级
            int randomScore = generateRandomScore();
            String gradeLetter = getGradeLetter(randomScore);
            
            // 保存到学生个人课程文件
            boolean saveSuccess = saveSubjectToStudentFile(newId, newName, existingCredits, randomScore, gradeLetter);
            
            if (saveSuccess) {
                refreshStudentMainFrame();  // 刷新主界面
                JOptionPane.showMessageDialog(enrolFrame, 
                    "Random subject enrolled successfully!\n\nSubject ID: " + newId + 
                    "\nSubject Name: " + newName +
                    "\nCredits: " + existingCredits +
                    "\nScore: " + randomScore +
                    "\nGrade: " + gradeLetter,
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                refreshSubjectTable();  // 刷新课程列表
            } else {
                JOptionPane.showMessageDialog(enrolFrame, 
                    "Failed to enrol random subject!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridy = 1;
        rightButtonPanel.add(randomEnrolButton, gbc);
        
        mainContentPanel.add(rightButtonPanel, BorderLayout.EAST);
        enrolFrame.add(mainContentPanel, BorderLayout.CENTER);
        
        // 底部按钮面板
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.setBackground(Color.RED);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(true);
        closeButton.addActionListener(e -> enrolFrame.dispose());
        
        bottomPanel.add(closeButton);
        enrolFrame.add(bottomPanel, BorderLayout.SOUTH);
        
        // 表格行选中监听器
        // 当用户选中一行时启用"Enrol Selected"按钮
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = subjectTable.getSelectedRow() >= 0;
                enrolButton.setEnabled(rowSelected);
            }
        });
        
        // 显示窗口并启动定时器
        enrolFrame.setLocationRelativeTo(null);
        enrolFrame.setVisible(true);
        
        startAutoRefreshTimer();
    }
    
    // 刷新学生主界面
    private static void refreshStudentMainFrame() {
        SwingUtilities.invokeLater(() -> {
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window instanceof JFrame && window.isVisible()) {
                    String title = ((JFrame) window).getTitle();
                    if (title != null && title.startsWith("Student Main Page - ")) {
                        try {
                            // 尝试调用主界面的刷新方法
                            java.lang.reflect.Method method = window.getClass().getMethod("refreshSubjectTable");
                            method.invoke(null);
                        } catch (Exception ex) {
                            window.repaint();  // 刷新失败则重绘窗口
                        }
                        break;
                    }
                }
            }
        });
    }
    
    // 获取课程名称
    private static String getSubjectNameById(String subjectId) {
        Map<String, String> subjectMap = getSubjectNameMap();
        String subjectName = subjectMap.get(subjectId);
        return subjectName != null ? subjectName : "Unknown Subject";
    }
    
    // 获取课程学分
    private static int getSubjectCreditsById(String subjectId) {
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            if (!csvFile.exists()) return 3;
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                isFirstLine = false;
                
                String[] row = parseCSVLine(line);
                if (row.length >= 3 && cleanField(row[0]).equals(subjectId)) {
                    try {
                        return Integer.parseInt(cleanField(row[2]));
                    } catch (NumberFormatException e) {
                        return 3;
                    }
                }
            }
            return 3;
        } catch (IOException e) {
            return 3;
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException e) {}
        }
    }
    
    // 获取课程名称映射
    private static Map<String, String> getSubjectNameMap() {
        Map<String, String> subjectMap = new HashMap<>();
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            if (!csvFile.exists()) return subjectMap;
            
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                isFirstLine = false;
                
                String[] row = parseCSVLine(line);
                if (row.length >= 2) {
                    subjectMap.put(cleanField(row[0]), cleanField(row[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException e) {}
        }
        return subjectMap;
    }
    
    // 生成随机分数
    private static int generateRandomScore() {
        Random random = new Random();
        return 25 + random.nextInt(76);  // 25 + [0,75] = 25~100
    }
    
    // 根据分数返回等级
    private static String getGradeLetter(int score) {
        if (score >= 85) return "HD";
        if (score >= 75) return "D";
        if (score >= 65) return "C";
        if (score >= 50) return "P";
        return "Z";
    }
    
    // 对CSV字段进行转义（处理逗号和引号）
    private static String escapeCSV(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    // 解析CSV行
    private static String[] parseCSVLine(String line) {
        java.util.ArrayList<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;  // 标记是否在引号内部
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 处理转义引号：两个连续双引号表示一个双引号字符
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;  // 跳过下一个引号
                } else {
                    inQuotes = !inQuotes;  // 切换引号状态
                }
            } else if (c == ',' && !inQuotes) {
                // 不在引号内的逗号表示字段分隔符
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());  // 添加最后一个字段
        
        return fields.toArray(new String[0]);
    }
    
    // 清理字段值（去除首尾空格和引号）
    private static String cleanField(String field) {
        if (field == null) return "";
        String cleaned = field.trim();
        // 如果字段被引号包围，去除首尾的引号
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
}