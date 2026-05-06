import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

// 学校教务系统的登录选择界面，包含学生登录和管理员登录两个入口
public class LoginChooserFrame {
    
    // 主窗口引用，用于在登录成功时关闭
    private static JFrame mainFrame;
    
    // 获取背景图片
    private static ImageIcon getBackgroundImage() {
        // 背景图片文件名
        String fileName = "BackgroundImage.png";
        
        // 直接在当前目录查找
        try {
            File imageFile = new File(fileName);
            if (imageFile.exists()) {
                System.out.println("Background image found: " + imageFile.getAbsolutePath());
                return new ImageIcon(imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error loading from current file: " + e.getMessage());
        }
        
        System.err.println("Background image 'BackgroundImage.png' not found!");
        return null;
    }
    
    public static void main(String[] args) {
        // 初始化主窗口
        mainFrame = new JFrame("Login Selection");
        // 设置关闭按钮行为：点击关闭按钮时退出整个应用程序
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 600);
        
        // 设置背景面板
        ImageIcon backgroundIcon = getBackgroundImage();
        BackgroundPanel backgroundPanel;
        
        if (backgroundIcon != null) {
            backgroundPanel = new BackgroundPanel(backgroundIcon);
        } else {
            backgroundPanel = new BackgroundPanel((ImageIcon)null);
        }
        
        backgroundPanel.setLayout(new BorderLayout());
        
        // 创建并配置顶部标题
        JLabel titleLabel = new JLabel("School Academic System", SwingConstants.CENTER);
        Font titleFont = new Font("Times New Roman", Font.BOLD, 44);
        // 将标题字体应用到标签上
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.PINK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(80, 0, 60, 0));
        // 将标题标签添加到背景面板的顶部
        backgroundPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建并配置按钮面板
        JPanel buttonPanel = new JPanel();
        // 设置按钮面板为透明
        buttonPanel.setOpaque(false);
        // 控制组件的位置
        buttonPanel.setLayout(new GridBagLayout());
        // 设置每个组件的布局参数
        GridBagConstraints gbc = new GridBagConstraints();
        Font buttonFont = new Font("Times New Roman", Font.BOLD, 22);
        
        // 创建学生登录按钮
        TransparentButton studentButton = new TransparentButton("Student Login");
        studentButton.setFont(buttonFont);
        studentButton.setPreferredSize(new Dimension(200, 60));
        studentButton.setBackgroundColor(new Color(255, 255, 255, 191)); // 白色半透明
        studentButton.setForeground(Color.BLUE);
        // 设置登录成功回调，用于关闭登录选择窗口
        StudentLoginFrame.setLoginSuccessCallback(() -> {
            if (mainFrame != null && mainFrame.isDisplayable()) {
                mainFrame.dispose();
            }
        });
        // 为按钮添加点击事件监听器
        studentButton.addActionListener(e -> {
            // 调用学生登录界面
            StudentLoginFrame.showStudentLogin();
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 60, 0);
        buttonPanel.add(studentButton, gbc);
        
        // 创建管理员登录按钮
        TransparentButton adminButton = new TransparentButton("Admin Login");
        adminButton.setFont(buttonFont);
        adminButton.setPreferredSize(new Dimension(200, 60));
        adminButton.setBackgroundColor(new Color(255, 255, 255, 191));
        adminButton.setForeground(Color.ORANGE);
        // 为按钮添加点击事件监听器
        adminButton.addActionListener(e -> {
            // 显示管理员密钥输入对话框
            showAdminKeyDialog(mainFrame);
        });
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(adminButton, gbc);
        
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 80, 0));
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // 完成窗口配置并显示
        mainFrame.setContentPane(backgroundPanel);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
    
    // 显示管理员密钥输入对话框
    private static void showAdminKeyDialog(JFrame parentFrame) {
        // 创建自定义对话框
        JDialog keyDialog = new JDialog(parentFrame, "Admin Authentication", true);
        keyDialog.setSize(400, 200);
        keyDialog.setLayout(new BorderLayout());
        keyDialog.setLocationRelativeTo(parentFrame);
        
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // 创建提示标签
        JLabel promptLabel = new JLabel("Please enter the admin key:", SwingConstants.CENTER);
        Font promptFont = new Font("Times New Roman", Font.PLAIN, 22);
        promptLabel.setFont(promptFont);
        promptLabel.setForeground(Color.BLACK);
        promptLabel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        mainPanel.add(promptLabel, BorderLayout.NORTH);
        
        // 创建密码输入面板
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.setBackground(Color.WHITE);
        
        JPasswordField keyField = new JPasswordField(15);
        keyField.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        
        inputPanel.add(keyField);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        
        // 创建信息标签（用于显示错误信息）
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        messageLabel.setForeground(Color.RED);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(messageLabel, BorderLayout.SOUTH);
        
        keyDialog.add(mainPanel);
        
        // 为密码输入框添加键盘事件监听器，按Enter键自动验证
        keyField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String enteredKey = new String(keyField.getPassword());
                    
                    // 验证管理员密钥（密码为：Elysia）
                    if ("Elysia".equals(enteredKey)) {
                        // 密码正确，关闭对话框和主窗口，打开管理员主界面
                        keyDialog.dispose();
                        parentFrame.dispose();
                        AdminMainFrame.showAdminMain();
                    } else {
                        // 密码错误，显示错误信息并清空密码框
                        messageLabel.setText("Invalid admin key! Please try again.");
                        keyField.setText("");
                        // 让错误信息3秒后自动消失
                        Timer timer = new Timer(3000, ev -> messageLabel.setText(" "));
                        timer.setRepeats(false);
                        timer.start();
                    }
                }
            }
        });
        
        keyDialog.setVisible(true);
    }
}

// 透明按钮
class TransparentButton extends JButton {
    private Color backgroundColor;
    
    public TransparentButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
    }
    
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (backgroundColor != null) {
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        }
        
        super.paintComponent(g2d);
        g2d.dispose();
    }
}

// 背景面板
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(ImageIcon icon) {
        if (icon != null) {
            backgroundImage = icon.getImage();
        }
        setLayout(new BorderLayout());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            // 绘制背景图片，缩放至面板大小
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // 默认白色背景
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}