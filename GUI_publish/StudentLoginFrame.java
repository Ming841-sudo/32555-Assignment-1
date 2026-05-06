import javax.swing.*;
import java.awt.*;
import java.io.*;

// 学生登录界面类
public class StudentLoginFrame {

    // CSV文件保存路径
    private static final String CSV_FILE_PATH = "StudentInfo.csv";

    // 登录成功回调接口
    private static LoginSuccessCallback successCallback;
    
    // 登录成功回调接口
    public interface LoginSuccessCallback {
        void onLoginSuccess();
    }
    
    // 设置登录成功回调
    public static void setLoginSuccessCallback(LoginSuccessCallback callback) {
        successCallback = callback;
    }
    
    // 显示学生登录界面
    public static void showStudentLogin() {
        showStudentLogin(null);
    }
    
    // 显示学生登录界面
    public static void showStudentLogin(JFrame loginChooserFrame) {
        // 显示当前工作目录，便于调试
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        System.out.println("CSV file path: " + new File(CSV_FILE_PATH).getAbsolutePath());
        
        // 保存登录选择窗口的引用（用于登录成功后关闭）
        JFrame loginChooserRef = loginChooserFrame;
        
        // 创建登录窗口
        JFrame studentFrame = new JFrame("Student Login");
        studentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // 关闭时销毁当前窗口
        studentFrame.setSize(500, 400);
        
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // 创建标题标签
        JLabel titleLabel = new JLabel("Student Login", SwingConstants.CENTER);
        Font titleFont = new Font("Times New Roman", Font.BOLD, 36);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.PINK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());  // 使用网格包布局
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 设置字体
        Font labelFont = new Font("Times New Roman", Font.PLAIN, 18);
        Font fieldFont = new Font("Times New Roman", Font.PLAIN, 18);
        
        // 账号输入行
        JLabel accountLabel = new JLabel("Email Account:");
        accountLabel.setFont(labelFont);
        accountLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;  // 右对齐
        formPanel.add(accountLabel, gbc);
        
        JTextField accountField = new JTextField(15);
        accountField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;  // 左对齐
        formPanel.add(accountField, gbc);
        
        // 密码输入行
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(passwordLabel, gbc);
        
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        buttonPanel.setBackground(Color.WHITE);
        
        // 注册按钮
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.setBackground(Color.BLUE);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);   // 取消焦点边框
        registerButton.setBorderPainted(false);  // 取消按钮边框
        registerButton.setOpaque(true);          // 设置不透明
        // 注册按钮点击事件：打开注册窗口
        registerButton.addActionListener(e -> {
            StudentRegisterFrame.showStudentRegister();
        });
        
        // 登录按钮
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setBackground(Color.ORANGE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);
        
        // 登录按钮点击事件：验证邮箱和密码
        loginButton.addActionListener(e -> {
            // 获取用户输入的邮箱和密码
            String email = accountField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            // 验证输入是否为空
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(studentFrame, 
                    "Please enter both email and password!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 从CSV文件中验证用户
            String validationResult = validateUserFromCSV(email, password);
            
            // 根据验证结果处理
            if (validationResult.equals("SUCCESS")) {
                // 关闭登录选择窗口
                if (loginChooserRef != null && loginChooserRef.isDisplayable()) {
                    loginChooserRef.dispose();
                }
                // 执行回调
                if (successCallback != null) {
                    successCallback.onLoginSuccess();
                }
                // 关闭当前登录窗口
                studentFrame.dispose();
                // 打开学生主界面
                StudentMainFrame.showStudentMain(email);
            } else if (validationResult.equals("PASSWORD_ERROR")) {
                // 密码错误：显示错误信息并清空密码框
                JOptionPane.showMessageDialog(studentFrame, 
                    "Incorrect password!\nPlease try again.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");  // 清空密码框
            } else if (validationResult.equals("EMAIL_NOT_FOUND")) {
                // 邮箱未注册：询问是否跳转到注册页面
                int choice = JOptionPane.showConfirmDialog(studentFrame, 
                    "Email account not found!\n\nDo you want to register now?",
                    "Account Not Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    StudentRegisterFrame.showStudentRegister();
                }
            } else {
                // 其他错误（如文件读取错误）
                JOptionPane.showMessageDialog(studentFrame, 
                    "Login failed!\nPlease contact the administrator.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置内容面板并显示窗口
        studentFrame.setContentPane(mainPanel);
        studentFrame.setLocationRelativeTo(null);  // 窗口居中显示
        studentFrame.setVisible(true);
    }
    
    // 从CSV文件中验证用户邮箱和密码
    private static String validateUserFromCSV(String email, String password) {
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(CSV_FILE_PATH);
            
            // 检查文件是否存在
            if (!csvFile.exists()) {
                System.err.println("CSV file does not exist: " + csvFile.getAbsolutePath());
                return "EMAIL_NOT_FOUND";
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
                if (row.length >= 4) {
                    String username = cleanField(row[1]);
                    String csvEmail = cleanField(row[2]);
                    String csvPassword = cleanField(row[3]);
                    
                    // 匹配邮箱（不区分大小写）
                    if (csvEmail.equalsIgnoreCase(email)) {
                        // 验证密码
                        if (csvPassword.equals(password)) {
                            System.out.println("Login successful: " + username + " (" + email + ")");
                            return "SUCCESS";
                        } else {
                            System.out.println("Password error for: " + email);
                            return "PASSWORD_ERROR";
                        }
                    }
                }
            }
            
            System.out.println("Email not found: " + email);
            return "EMAIL_NOT_FOUND";
            
        } catch (FileNotFoundException e) {
            System.err.println("CSV file not found: " + CSV_FILE_PATH);
            return "EMAIL_NOT_FOUND";
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
            return "FILE_ERROR";
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
                    currentField.append('"');  // 添加转义后的双引号
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