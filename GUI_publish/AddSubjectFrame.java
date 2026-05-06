import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// 添加课程窗口
public class AddSubjectFrame {
    
    // 课程CSV文件保存路径
    private static final String SUBJECT_CSV_FILE_PATH = "SubjectInfo.csv";
    
    // 显示添加课程对话框
    public static void showAddSubjectDialog(JFrame parentFrame) {
        // 创建自定义模态对话框
        JDialog subjectDialog = new JDialog(parentFrame, "Add New Subject", true);
        subjectDialog.setSize(450, 320);
        subjectDialog.setLayout(new BorderLayout());  // 使用边界布局管理器
        subjectDialog.setLocationRelativeTo(parentFrame);  // 相对于父窗口居中显示
        
        // 创建主面板，用于放置表单输入组件
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());  // 使用网格包布局
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // 创建网格包布局约束对象，用于控制每个组件的位置和样式
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;  // 组件水平方向填充可用空间
        
        // 设置字体
        Font labelFont = new Font("Times New Roman", Font.PLAIN, 14);
        Font fieldFont = new Font("Times New Roman", Font.PLAIN, 14);
        Font displayFont = new Font("Times New Roman", Font.BOLD, 14);
        
        // 创建课程ID标签
        JLabel subjectIdLabel = new JLabel("Subject ID:");
        subjectIdLabel.setFont(labelFont);
        subjectIdLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(subjectIdLabel, gbc);
        
        // 调用AdminMainFrame的静态方法，生成唯一的3位数课程ID（如001、012、123）
        String generatedSubjectId = AdminMainFrame.generateUniqueSubjectId();
        
        // 创建课程ID显示框
        JTextField subjectIdDisplayField = new JTextField(15);
        subjectIdDisplayField.setFont(displayFont);
        subjectIdDisplayField.setText(generatedSubjectId);
        subjectIdDisplayField.setEditable(false);  // 设置为只读，无法修改
        subjectIdDisplayField.setBackground(Color.WHITE);
        subjectIdDisplayField.setForeground(Color.BLACK);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(subjectIdDisplayField, gbc);
        
        // 创建课程名称标签
        JLabel subjectNameLabel = new JLabel("Subject Name:");
        subjectNameLabel.setFont(labelFont);
        subjectNameLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(subjectNameLabel, gbc);
        
        // 创建课程名称输入框，管理员在此输入课程名称
        JTextField subjectNameField = new JTextField(15);
        subjectNameField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(subjectNameField, gbc);
        
        // 创建课程学分标签
        JLabel creditsLabel = new JLabel("Credits:");
        creditsLabel.setFont(labelFont);
        creditsLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(creditsLabel, gbc);
        
        // 创建学分选择器
        JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        creditsSpinner.setFont(fieldFont);
        creditsSpinner.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(creditsSpinner, gbc);
        
        subjectDialog.add(mainPanel, BorderLayout.CENTER);  // 将主面板添加到对话框中央区域
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));  // 流式布局，居中，水平和垂直间距
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));  // 底部20像素内边距
        
        // 确认添加按钮
        JButton confirmButton = new JButton("Add Subject");
        confirmButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(120, 35));
        confirmButton.setBackground(Color.BLUE);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);      // 取消按钮焦点边框绘制
        confirmButton.setBorderPainted(false);     // 取消按钮边框绘制
        confirmButton.setOpaque(true);             // 设置按钮为不透明
        
        // 为确认按钮添加点击事件监听器
        confirmButton.addActionListener(e -> {
            String subjectName = subjectNameField.getText().trim();
            String subjectId = subjectIdDisplayField.getText().trim();
            int credits = (int) creditsSpinner.getValue();
            
            // 验证课程名称不能为空
            if (subjectName.isEmpty()) {
                // 弹出错误提示对话框
                JOptionPane.showMessageDialog(subjectDialog, 
                    "Please enter the subject name!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;  // 终止执行，不保存
            }
            
            // 将课程信息保存到CSV文件
            boolean saveSuccess = saveSubjectToCSVWithEmptyRow(subjectName, subjectId, credits);
            
            if (saveSuccess) {
                // 保存成功，显示成功消息
                JOptionPane.showMessageDialog(subjectDialog, 
                    "Subject added successfully!\n\nSubject ID: " + subjectId + 
                    "\nSubject Name: " + subjectName + 
                    "\nCredits: " + credits,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                subjectDialog.dispose(); // 关闭对话框
            } else {
                // 保存失败，显示错误消息
                JOptionPane.showMessageDialog(subjectDialog, 
                    "Failed to save subject information!\nPlease check the file path.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // 取消按钮
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBackground(Color.ORANGE);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(true);
        // 直接关闭对话框，不保存任何数据
        cancelButton.addActionListener(e -> subjectDialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        subjectDialog.add(buttonPanel, BorderLayout.SOUTH);  // 将按钮面板添加到对话框底部
        
        subjectDialog.setVisible(true);  // 显示对话框
    }
    
    // 将课程信息保存到CSV文件中
    private static boolean saveSubjectToCSVWithEmptyRow(String subjectName, String subjectId, int credits) {
        List<String[]> allRows = new ArrayList<>();  // 存储所有行的数据
        BufferedReader reader = null;
        boolean isFirstLine = true;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            boolean isNewFile = !csvFile.exists();  // 判断文件是否不存在
            
            // 确保文件所在的目录存在
            File parentDir = csvFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();  // 创建不存在的父目录
            }
            
            // 如果文件存在，则读取所有现有行
            if (!isNewFile) {
                reader = new BufferedReader(new FileReader(csvFile));
                String line;
                
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        allRows.add(new String[0]);  // 空行用空数组表示
                        continue;
                    }
                    
                    String[] row = parseCSVLine(line);  // 解析CSV行
                    allRows.add(row);
                    isFirstLine = false;
                }
                reader.close();
            }
            
            // 获取当前时间戳，用于记录添加时间
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            
            // 准备新课程数据行
            String[] newSubjectRow = {
                subjectId,
                subjectName,
                String.valueOf(credits),
                timestamp
            };
            
            // 如果是新文件，需要先添加表头行
            if (isNewFile) {
                String[] headerRow = {"Subject ID", "Subject Name", "Credits", "Added Date"};
                allRows.add(headerRow);
                allRows.add(newSubjectRow);
            } else {
                // 从表头之后查找空行
                boolean foundEmptyRow = false;
                for (int i = 1; i < allRows.size(); i++) {
                    String[] row = allRows.get(i);
                    // 检查该行是否为空（所有字段都为空或只有空字符串）
                    boolean isEmpty = true;
                    for (String field : row) {
                        if (field != null && !field.trim().isEmpty()) {
                            isEmpty = false;
                            break;
                        }
                    }
                    if (isEmpty) {
                        // 在找到的空行位置替换为新数据
                        allRows.set(i, newSubjectRow);
                        foundEmptyRow = true;
                        System.out.println("Subject saved in empty row at index: " + i);
                        break;
                    }
                }
                
                // 如果没有找到空行，则追加到文件末尾
                if (!foundEmptyRow) {
                    allRows.add(newSubjectRow);
                    System.out.println("Subject appended to end of file");
                }
            }
            
            // 打印当前工作目录和文件路径（用于调试）
            String userDir = System.getProperty("user.dir");
            System.out.println("Current working directory: " + userDir);
            System.out.println("Subject CSV file path: " + new File(SUBJECT_CSV_FILE_PATH).getAbsolutePath());
            
            // 写回文件（覆盖写入）
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            
            for (int i = 0; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                if (row.length == 0) {
                    writer.newLine();  // 写入空行
                    continue;
                }
                
                StringBuilder lineBuilder = new StringBuilder();
                for (int j = 0; j < row.length; j++) {
                    if (j > 0) {
                        lineBuilder.append(",");  // 添加逗号分隔符
                    }
                    String field = row[j];
                    if (field == null) field = "";
                    // 如果字段包含逗号、引号或换行符，需要用双引号包围并转义内部引号
                    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                        lineBuilder.append("\"").append(field.replace("\"", "\"\"")).append("\"");
                    } else {
                        lineBuilder.append(field);
                    }
                }
                writer.write(lineBuilder.toString());
                if (i < allRows.size() - 1) {
                    writer.newLine();  // 除最后一行外，每行末尾添加换行符
                }
            }
            writer.close();  // 关闭写入流
            
            System.out.println("Subject saved: " + subjectId + " - " + subjectName);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (reader != null) {
                    reader.close();  // 确保读取流被关闭
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
                // 处理引号内的双引号转义（两个双引号表示一个双引号字符）
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');  // 添加转义后的双引号
                    i++;  // 跳过下一个引号
                } else {
                    inQuotes = !inQuotes;  // 切换引号状态
                }
            } else if (c == ',' && !inQuotes) {
                // 不在引号内的逗号表示字段分隔符
                fields.add(currentField.toString());
                currentField.setLength(0);  // 清空StringBuilder，准备下一个字段
            } else {
                currentField.append(c);  // 添加普通字符
            }
        }
        fields.add(currentField.toString());  // 添加最后一个字段
        
        return fields.toArray(new String[0]);
    }
}