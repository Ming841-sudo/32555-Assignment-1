import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

// 学生注册界面
public class StudentRegisterFrame {
    
    // 学生信息CSV文件保存路径（存储所有学生基本信息）
    private static final String CSV_FILE_PATH = "StudentInfo.csv";
    // 学生个人课程文件夹路径（每个学生独立存放课程记录）
    private static final String STUDENTS_FOLDER_PATH = "Students";
    
    // 显示学生注册界面
    public static void showStudentRegister() {
        // 显示当前工作目录
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        System.out.println("CSV file path: " + new File(CSV_FILE_PATH).getAbsolutePath());
        System.out.println("Students folder path: " + new File(STUDENTS_FOLDER_PATH).getAbsolutePath());
        
        // 创建注册窗口
        JFrame registerFrame = new JFrame("Student Register");
        registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // 关闭时销毁当前窗口
        registerFrame.setSize(600, 500);
        
        // 创建主面板（边界布局）
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // 创建标题标签
        JLabel titleLabel = new JLabel("Student Registration", SwingConstants.CENTER);
        Font titleFont = new Font("Times New Roman", Font.BOLD, 36);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.PINK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());  // 使用网格包布局
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        
        // 设置字体
        Font labelFont = new Font("Times New Roman", Font.PLAIN, 18);
        Font fieldFont = new Font("Times New Roman", Font.PLAIN, 18);
        
        // 用户名输入行
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;  // 右对齐
        formPanel.add(usernameLabel, gbc);
        
        JTextField usernameField = new JTextField(15);
        usernameField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;  // 左对齐
        formPanel.add(usernameField, gbc);
        
        // 邮箱输入行
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(emailLabel, gbc);
        
        JTextField emailField = new JTextField(15);
        emailField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(emailField, gbc);
        
        // 邮箱格式提示标签
        JLabel emailHintLabel = new JLabel("<html><font color='gray' size='2'>Email must end with @university.com</font></html>");
        emailHintLabel.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(emailHintLabel, gbc);
        
        // 密码输入行 
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(passwordLabel, gbc);
        
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);
        
        // 确认密码输入行
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(labelFont);
        confirmPasswordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(confirmPasswordLabel, gbc);
        
        JPasswordField confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(confirmPasswordField, gbc);
        
        // ----- 4.6 密码格式提示标签 -----
        JLabel passwordHintLabel = new JLabel("<html><font color='gray' size='2'>Password format: Uppercase letter + at least 5 letters + at least 3 digits</font></html>");
        passwordHintLabel.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordHintLabel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        buttonPanel.setBackground(Color.WHITE);
        
        // 提交注册按钮
        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        submitButton.setPreferredSize(new Dimension(120, 40));
        submitButton.setBackground(Color.BLUE);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);   // 取消焦点边框
        submitButton.setBorderPainted(false);  // 取消按钮边框
        submitButton.setOpaque(true);          // 设置不透明
        
        // 提交按钮点击事件
        submitButton.addActionListener(e -> {
            // 获取用户输入
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            // 表单验证
            // 验证用户名不能为空
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Please enter your username!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证邮箱不能为空
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Please enter your email!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证邮箱格式（以 @university.com 结尾）
            if (!isValidUniversityEmail(email)) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Invalid email format!\n\nEmail must end with @university.com\n" +
                    "Example: student@university.com", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证密码不能为空
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Please enter your password!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证密码格式
            if (!isValidPasswordFormat(password)) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Invalid password format!\n\n" +
                    "Password requirements:\n" +
                    "1. Must start with an uppercase letter\n" +
                    "2. Must contain at least 5 letters (total)\n" +
                    "3. Must contain at least 3 digits\n\n" +
                    "Example: Abcdef123, Xyzabc456, Hello12345",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 验证两次输入的密码一致
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Passwords do not match!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 7. 检查邮箱是否已注册
            if (isEmailExists(email)) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "This email is already registered!\nPlease use a different email or login.", 
                    "Registration Failed", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 生成唯一的6位数学生ID
            String generatedStudentId = generateUniqueStudentId();
            
            // 将学生信息保存到CSV文件
            boolean saveSuccess = saveStudentInfoToCSV(username, email, generatedStudentId, password);
            
            // 为学生创建个人课程CSV文件
            boolean createStudentFileSuccess = createStudentSubjectFile(generatedStudentId, username);
            
            // 显示注册结果
            if (saveSuccess && createStudentFileSuccess) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Registration Successful!\n\nUsername: " + username + 
                    "\nEmail: " + email + 
                    "\nStudent ID: " + generatedStudentId +
                    "\nPassword: " + password +
                    "\n\nPlease remember your Student ID for login!\n" +
                    "Personal subject file created: Students/Student-" + generatedStudentId + ".csv",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Registration Successful!\n\nUsername: " + username + 
                    "\nEmail: " + email + 
                    "\nStudent ID: " + generatedStudentId +
                    "\nPassword: " + password +
                    "\n\nWarning: Failed to create personal subject file.\nPlease remember your Student ID!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            }
            
            // 关闭注册窗口
            registerFrame.dispose();
        });
        
        // 重置按钮
        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        resetButton.setPreferredSize(new Dimension(120, 40));
        resetButton.setBackground(Color.ORANGE);
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.setOpaque(true);
        
        // 重置按钮点击事件：清空所有输入框
        resetButton.addActionListener(e -> {
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
        });
        
        buttonPanel.add(submitButton);
        buttonPanel.add(resetButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置内容面板并显示窗口
        registerFrame.setContentPane(mainPanel);
        registerFrame.setLocationRelativeTo(null);  // 窗口居中显示
        registerFrame.setVisible(true);
    }
    
    // 验证邮箱格式（以 @university.com 结尾）
    private static boolean isValidUniversityEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // 转换为小写后检查是否以@university.com结尾
        return email.toLowerCase().endsWith("@university.com");
    }
    
    // 验证密码格式
    private static boolean isValidPasswordFormat(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        // 检查是否以大写字母开头
        if (!Character.isUpperCase(password.charAt(0))) {
            return false;
        }
        
        // 统计字母和数字的数量
        int letterCount = 0;
        int digitCount = 0;
        
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                letterCount++;
            } else if (Character.isDigit(c)) {
                digitCount++;
            }
        }
        
        // 至少5个字母和至少3个数字
        return letterCount >= 5 && digitCount >= 3;
    }
    
    // 检查邮箱是否已存在于CSV文件中
    private static boolean isEmailExists(String email) {
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(CSV_FILE_PATH);
            if (!csvFile.exists()) {
                return false;
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
                
                // 跳过表头
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 检查邮箱是否匹配
                if (row.length >= 3) {
                    String csvEmail = cleanField(row[2]);
                    if (csvEmail.equalsIgnoreCase(email)) {
                        return true;
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
    
    // 生成唯一的6位数学生ID
    private static String generateUniqueStudentId() {
        // 获取所有已存在的学生ID
        Set<String> existingIds = getAllExistingStudentIds();
        
        Random random = new Random();
        String newId;
        int attempts = 0;
        int maxAttempts = 1000; // 防止无限循环
        
        do {
            // 生成1到999999之间的随机数
            int idNumber = random.nextInt(999999) + 1;
            // 格式化为6位数，不足前面补0（如：000001）
            newId = String.format("%06d", idNumber);
            attempts++;
            
            // 如果随机尝试次数过多，改用顺序查找方式
            if (attempts > maxAttempts) {
                for (int i = 1; i <= 999999; i++) {
                    String candidateId = String.format("%06d", i);
                    if (!existingIds.contains(candidateId)) {
                        return candidateId;
                    }
                }
                // 理论上的兜底
                return "000000";
            }
        } while (existingIds.contains(newId));  // 如果ID已存在，继续生成
        
        return newId;
    }
    
    // 获取CSV文件中所有已存在的学生ID
    private static Set<String> getAllExistingStudentIds() {
        Set<String> existingIds = new HashSet<>();
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(CSV_FILE_PATH);
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
                
                String[] row = parseCSVLine(line);
                
                // 跳过表头
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 读取第1列（学生ID）
                if (row.length >= 1) {
                    String studentId = cleanField(row[0]);
                    if (!studentId.isEmpty()) {
                        existingIds.add(studentId);
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
    
    // 将学生信息保存到CSV文件中
    private static boolean saveStudentInfoToCSV(String username, String email, String studentId, String password) {
        List<String[]> allRows = new ArrayList<>();
        BufferedReader reader = null;
        boolean isFirstLine = true;
        
        try {
            File csvFile = new File(CSV_FILE_PATH);
            boolean isNewFile = !csvFile.exists();
            
            // 如果文件存在，读取所有现有行
            if (!isNewFile) {
                reader = new BufferedReader(new FileReader(csvFile));
                String line;
                
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        allRows.add(new String[0]);
                        continue;
                    }
                    
                    String[] row = parseCSVLine(line);
                    allRows.add(row);
                    isFirstLine = false;
                }
                reader.close();
            }
            
            // 准备新学生数据
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String[] newStudentRow = {
                studentId,
                username,
                email,
                password,
                timestamp
            };
            
            // 如果是新文件，需要先添加表头
            if (isNewFile) {
                String[] headerRow = {"Student ID", "Username", "Email", "Password", "Registration Date"};
                allRows.add(headerRow);
            }
            
            // 添加新学生数据
            allRows.add(newStudentRow);
            
            // 写回文件（覆盖写入）
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
                    
                    // 如果字段包含逗号、引号或换行符，用双引号包围并转义
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
            
            System.out.println("Student info saved: " + studentId + " - " + username);
            System.out.println("Total rows in CSV: " + allRows.size());
            return true;
            
        } catch (Exception e) {
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
    
    // 为学生创建个人课程CSV文件
    private static boolean createStudentSubjectFile(String studentId, String username) {
        BufferedWriter writer = null;
        
        try {
            // 创建Students文件夹（如果不存在）
            File studentsFolder = new File(STUDENTS_FOLDER_PATH);
            if (!studentsFolder.exists()) {
                boolean created = studentsFolder.mkdirs();
                if (created) {
                    System.out.println("Created Students folder: " + studentsFolder.getAbsolutePath());
                } else {
                    System.err.println("Failed to create Students folder!");
                    return false;
                }
            }
            
            // 构建学生个人CSV文件路径
            String studentFileName = "Student-" + studentId + ".csv";
            File studentFile = new File(studentsFolder, studentFileName);
            
            // 如果文件已存在，不覆盖（理论上不应该存在）
            if (studentFile.exists()) {
                System.out.println("Student subject file already exists: " + studentFile.getAbsolutePath());
                return true;
            }
            
            // 创建新文件并写入表头
            writer = new BufferedWriter(new FileWriter(studentFile));
            
            // 写入表头
            String[] headers = {"Subject ID", "Subject Name", "Credits", "Score", "Grade"};
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) {
                    writer.write(",");
                }
                writer.write(headers[i]);
            }
            writer.newLine();
            
            writer.close();
            
            System.out.println("Student subject file created: " + studentFile.getAbsolutePath());
            return true;
            
        } catch (IOException e) {
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
}