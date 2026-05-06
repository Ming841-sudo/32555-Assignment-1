package org.GUI_publish;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 学生详情查看窗口
public class AdminOverviewFrame {
    
    // 学生CSV文件保存路径
    private static final String STUDENT_CSV_FILE_PATH = "StudentInfo.csv";
    // 学生个人课程文件夹路径
    private static final String STUDENTS_FOLDER_PATH = "Students";
    // 课程CSV文件保存路径
    private static final String SUBJECT_CSV_FILE_PATH = "SubjectInfo.csv";
    
    // 最大允许选课数量
    private static final int MAX_SUBJECTS = 4;
    // 课程ID到课程名称的映射缓存
    private static Map<String, String> subjectNameCache = null;
    
    // 打印当前工作目录
    private static void printWorkingDirectory() {
        // System.getProperty("user.dir") 获取Java虚拟机启动时的当前工作目录
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        System.out.println("Student CSV file path: " + new File(STUDENT_CSV_FILE_PATH).getAbsolutePath());
        System.out.println("Students folder path: " + new File(STUDENTS_FOLDER_PATH).getAbsolutePath());
        System.out.println("Subject CSV file path: " + new File(SUBJECT_CSV_FILE_PATH).getAbsolutePath());
    }
    
    // 获取课程名称映射
    private static Map<String, String> getSubjectNameMap() {
        // 如果缓存不为空，直接返回缓存数据
        if (subjectNameCache != null) {
            return subjectNameCache;
        }
        
        Map<String, String> subjectMap = new HashMap<>();
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            if (!csvFile.exists()) {
                System.out.println("Subject CSV file does not exist: " + csvFile.getAbsolutePath());
                return subjectMap;
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
                if (row.length >= 2) {
                    String subjectId = cleanField(row[0]);
                    String subjectName = cleanField(row[1]);
                    if (!subjectId.isEmpty() && !subjectName.isEmpty()) {
                        subjectMap.put(subjectId, subjectName);
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
        
        // 将读取的数据存入缓存
        subjectNameCache = subjectMap;
        return subjectMap;
    }
    
    // 根据课程ID获取课程名称
    private static String getSubjectNameById(String subjectId) {
        Map<String, String> subjectMap = getSubjectNameMap();
        String subjectName = subjectMap.get(subjectId);
        if (subjectName != null && !subjectName.isEmpty()) {
            return subjectName;
        }
        return "Unknown Subject";
    }
    
    // 根据分数返回等级
    private static String getGradeLetter(int score) {
        if (score >= 85) return "HD";
        if (score >= 75) return "D";
        if (score >= 65) return "C";
        if (score >= 50) return "P";
        return "Z";
    }
    
    // 获取学生的个人课程文件路径
    private static String getStudentCourseFilePath(String studentId) {
        // File.separator 是系统相关的路径分隔符（Windows为\，Linux/Mac为/）
        return STUDENTS_FOLDER_PATH + File.separator + "Student-" + studentId + ".csv";
    }
    
    // 获取学生的选课信息
    private static List<String[]> getStudentEnrolmentInfo(String studentId) {
        List<String[]> enrolmentList = new ArrayList<>();
        BufferedReader reader = null;
        
        try {
            String studentFilePath = getStudentCourseFilePath(studentId);
            File studentFile = new File(studentFilePath);
            
            if (!studentFile.exists()) {
                System.out.println("Student course file does not exist: " + studentFilePath);
                return enrolmentList;
            }
            
            // 读取学生个人课程文件
            reader = new BufferedReader(new FileReader(studentFile));
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
                    String subjectId = cleanField(row[0]);
                    String subjectName = cleanField(row[1]);
                    String credits = cleanField(row[2]);
                    String scoreStr = cleanField(row[3]);
                    String grade = cleanField(row[4]);
                    
                    // 如果没有等级但有分数，重新计算等级
                    if ((grade == null || grade.isEmpty() || grade.equals("Not graded")) && 
                        !scoreStr.isEmpty() && !scoreStr.equals("Not graded")) {
                        try {
                            int score = Integer.parseInt(scoreStr);
                            grade = getGradeLetter(score);
                        } catch (NumberFormatException e) {
                            grade = "N/A";
                        }
                    }
                    
                    // 添加表头
                    enrolmentList.add(new String[]{subjectName, subjectId, scoreStr, grade});
                    System.out.println("Loaded subject: " + subjectId + " - " + subjectName + " (Score: " + scoreStr + ", Grade: " + grade + ")");
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
        
        System.out.println("Found " + enrolmentList.size() + " enrolled subjects for student ID: " + studentId);
        return enrolmentList;
    }
    
    // 显示学生详情窗口
    public static void showStudentOverview(JFrame parentFrame, String studentId, String studentName, String studentEmail) {
        // 打印当前工作目录和文件路径
        printWorkingDirectory();
        
        // 创建详情窗口
        JFrame overviewFrame = new JFrame("Student Overview - " + studentName + " (" + studentId + ")");
        overviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // 关闭时只销毁当前窗口
        overviewFrame.setSize(800, 550);
        overviewFrame.setLayout(new BorderLayout());
        overviewFrame.setLocationRelativeTo(parentFrame);  // 相对于父窗口居中
        
        // 创建顶部学生信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(3, 1, 5, 5));
        infoPanel.setBackground(Color.PINK);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // 学生ID显示
        JLabel idLabel = new JLabel("Student ID: " + studentId);
        idLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        
        // 学生姓名显示
        JLabel nameLabel = new JLabel("Student Name: " + studentName);
        nameLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        
        // 学生邮箱显示
        JLabel emailLabel = new JLabel("Email: " + studentEmail);
        emailLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        
        infoPanel.add(idLabel);
        infoPanel.add(nameLabel);
        infoPanel.add(emailLabel);
        
        overviewFrame.add(infoPanel, BorderLayout.NORTH);
        
        // 获取学生的选课信息
        List<String[]> enrolmentList = getStudentEnrolmentInfo(studentId);
        
        // 创建选课信息表格
        String[] columnNames = {"Subject Name", "Subject ID", "Score", "Grade"};
        
        // 创建表格模型，使所有单元格不可编辑
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // 将选课数据逐行添加到表格
        for (String[] enrolment : enrolmentList) {
            tableModel.addRow(enrolment);
        }
        
        // 创建表格组件
        JTable subjectTable = new JTable(tableModel);
        subjectTable.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        subjectTable.setRowHeight(25);
        
        // 设置表头样式
        subjectTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 16));
        subjectTable.getTableHeader().setBackground(new Color(0, 102, 204));
        subjectTable.getTableHeader().setForeground(Color.WHITE);
        
        // 创建滚动面板包裹表格
        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Enrolled Subjects (from Students folder)"));
        scrollPane.setPreferredSize(new Dimension(700, 350));
        
        overviewFrame.add(scrollPane, BorderLayout.CENTER);
        
        // 底部统计面板
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        
        // 计算统计信息
        int totalSubjects = enrolmentList.size();
        int gradedCount = 0;
        int totalScore = 0;
        
        for (String[] enrolment : enrolmentList) {
            String scoreStr = enrolment[2];
            // 统计有效分数（排除"Not graded"）
            if (scoreStr != null && !scoreStr.isEmpty() && !scoreStr.equals("Not graded")) {
                try {
                    int score = Integer.parseInt(scoreStr);
                    totalScore += score;
                    gradedCount++;
                } catch (NumberFormatException e) {
                    // 忽略非数字分数
                }
            }
        }
        
        // 显示总选课数量
        JLabel totalLabel = new JLabel("Total Subjects: " + totalSubjects + " / " + MAX_SUBJECTS);
        totalLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        totalLabel.setForeground(Color.BLUE);
        statsPanel.add(totalLabel);
        
        // 如果有已评分的课程，显示平均分和对应等级
        if (gradedCount > 0) {
            double average = (double) totalScore / gradedCount;
            JLabel avgLabel = new JLabel("Average Score: " + String.format("%.1f", average) + " (" + getGradeLetter((int) average) + ")");
            avgLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
            avgLabel.setForeground(Color.BLUE);
            statsPanel.add(avgLabel);
        } else if (totalSubjects > 0) {
            // 有选课但都未评分
            JLabel avgLabel = new JLabel("No graded subjects yet");
            avgLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
            avgLabel.setForeground(Color.GRAY);
            statsPanel.add(avgLabel);
        }
        
        // 关闭按钮
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Times New Roman", Font.BOLD, 16));
        closeButton.setPreferredSize(new Dimension(80, 30));
        closeButton.setBackground(Color.RED);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);   // 取消焦点边框
        closeButton.setBorderPainted(false);  // 取消按钮边框
        closeButton.setOpaque(true);          // 设置不透明
        closeButton.addActionListener(e -> overviewFrame.dispose());  // 点击关闭窗口
        
        statsPanel.add(closeButton);
        overviewFrame.add(statsPanel, BorderLayout.SOUTH);
        
        // 显示窗口
        overviewFrame.setVisible(true);
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
                currentField.setLength(0);  // 清空StringBuilder，准备下一个字段
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