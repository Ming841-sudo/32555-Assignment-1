import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

// 学生排名窗口
class StudentGradeInfo {
    // 学生基本信息
    String studentId;
    String studentName;
    String email;
    
    // 成绩相关
    ArrayList<Integer> scores;
    double average; 
    String gradeLetter;

    StudentGradeInfo(String studentId, String studentName, String email) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.scores = new ArrayList<>();
        this.average = 0;
        this.gradeLetter = "N/A";
    }
    
    // 计算平均分和等级
    void calculateAverage() {
        if (scores.isEmpty()) {
            // 没有成绩时平均分为0，等级为N/A
            average = 0;
            gradeLetter = "N/A";
        } else {
            // 计算总分
            int sum = 0;
            for (int score : scores) {
                sum += score;
            }
            // 计算平均分
            average = (double) sum / scores.size();
            // 根据平均分确定等级
            gradeLetter = getGradeLetter((int) average);
        }
    }
    
    // 获取平均分
    double getAverage() {
        return average;
    }
    
    // 检查是否有成绩记录
    boolean hasScores() {
        return !scores.isEmpty();
    }
    
    // 根据分数返回等级
    private String getGradeLetter(int score) {
        if (score >= 85) return "HD";
        if (score >= 75) return "D";
        if (score >= 65) return "C";
        if (score >= 50) return "P";
        return "Z";
    }
}

// 学生成绩排名窗口
public class StudentRankFrame {
    
    
    // 学生CSV文件保存路径（存储所有学生基本信息）
    private static final String STUDENT_CSV_FILE_PATH = "StudentInfo.csv";
    // 学生个人课程文件夹路径（每个学生独立存放课程记录）
    private static final String STUDENTS_FOLDER_PATH = "Students";
    // 课程CSV文件保存路径（存储系统中所有课程信息）
    private static final String SUBJECT_CSV_FILE_PATH = "SubjectInfo.csv";
    
    // 打印当前工作目录
    private static void printWorkingDirectory() {
        // System.getProperty("user.dir") 获取Java虚拟机启动时的当前工作目录
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        System.out.println("Student CSV file path: " + new File(STUDENT_CSV_FILE_PATH).getAbsolutePath());
        System.out.println("Students folder path: " + new File(STUDENTS_FOLDER_PATH).getAbsolutePath());
        System.out.println("Subject CSV file path: " + new File(SUBJECT_CSV_FILE_PATH).getAbsolutePath());
    }
    
    // 获取学生的个人课程文件路径
    private static String getStudentCourseFilePath(String studentId) {
        // File.separator 是系统相关的路径分隔符（Windows为\，Linux/Mac为/）
        return STUDENTS_FOLDER_PATH + File.separator + "Student-" + studentId + ".csv";
    }
    
    // 获取学生列表
    private static ArrayList<StudentGradeInfo> getStudentList() {
        ArrayList<StudentGradeInfo> studentList = new ArrayList<>();
        BufferedReader reader = null;
        
        try {
            File csvFile = new File(STUDENT_CSV_FILE_PATH);
            if (!csvFile.exists()) {
                System.out.println("Student CSV file does not exist: " + csvFile.getAbsolutePath());
                return studentList;
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
                    String studentId = cleanField(row[0]);
                    String studentName = cleanField(row[1]);
                    String email = cleanField(row[2]);
                    
                    // 验证学生ID格式（6位数字）
                    if (studentId != null && !studentId.isEmpty() && studentId.matches("\\d{6}")) {
                        StudentGradeInfo studentInfo = new StudentGradeInfo(studentId, studentName, email);
                        studentList.add(studentInfo);
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
        
        System.out.println("Total students found: " + studentList.size());
        return studentList;
    }
    
    // 从学生的个人课程文件中读取成绩
    private static ArrayList<Integer> getStudentScoresFromFile(String studentId) {
        ArrayList<Integer> scores = new ArrayList<>();
        BufferedReader reader = null;
        
        try {
            String studentFilePath = getStudentCourseFilePath(studentId);
            File studentFile = new File(studentFilePath);
            
            if (!studentFile.exists()) {
                System.out.println("Student course file does not exist: " + studentFilePath);
                return scores;
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
                if (row.length >= 4) {
                    String scoreStr = cleanField(row[3]);
                    // 只统计有效成绩（排除空值和"Not graded"）
                    if (scoreStr != null && !scoreStr.isEmpty() && !scoreStr.equals("Not graded")) {
                        try {
                            int score = Integer.parseInt(scoreStr);
                            scores.add(score);
                        } catch (NumberFormatException e) {
                            // 忽略无效分数（非数字）
                        }
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
        
        return scores;
    }
    
    // 获取所有学生的成绩信息
    private static ArrayList<StudentGradeInfo> getAllStudentGrades() {
        // 获取所有学生列表
        ArrayList<StudentGradeInfo> studentList = getStudentList();
        
        // 为每个学生读取成绩并计算平均分
        for (StudentGradeInfo student : studentList) {
            ArrayList<Integer> scores = getStudentScoresFromFile(student.studentId);
            student.scores = scores;
            student.calculateAverage();
            System.out.println("Student: " + student.studentId + " - " + student.studentName + 
                               " | Scores: " + scores.size() + " | Avg: " + student.average);
        }
        
        return studentList;
    }
    
    // 显示学生成绩排名窗口
    public static void showStudentRank(JFrame parentFrame) {
        // 打印当前工作目录和文件路径
        printWorkingDirectory();
        
        // 获取所有学生成绩信息
        ArrayList<StudentGradeInfo> allStudents = getAllStudentGrades();
        
        // 分离及格和不及格的学生
        ArrayList<StudentGradeInfo> passedStudents = new ArrayList<>();
        ArrayList<StudentGradeInfo> failedStudents = new ArrayList<>();
        
        for (StudentGradeInfo student : allStudents) {
            if (!student.hasScores()) {
                // 没有成绩的学生归入不及格组
                failedStudents.add(student);
            } else if (student.average >= 50) {
                // 平均分≥50为及格
                passedStudents.add(student);
            } else {
                // 平均分<50为不及格
                failedStudents.add(student);
            }
        }
        
        // 分别按平均分从高到低排序（各自组内排序）
        passedStudents.sort((a, b) -> Double.compare(b.getAverage(), a.getAverage()));
        failedStudents.sort((a, b) -> Double.compare(b.getAverage(), a.getAverage()));
        
        // 创建排名窗口
        JFrame rankFrame = new JFrame("Student Ranking by Grade");
        rankFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // 关闭时销毁当前窗口
        rankFrame.setSize(1200, 700);
        rankFrame.setLayout(new BorderLayout());
        rankFrame.setLocationRelativeTo(parentFrame);
        
        // 创建顶部标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.PINK);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Student Performance Ranking", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        rankFrame.add(titlePanel, BorderLayout.NORTH);
        
        //  创建主内容面板（左右两个表格）
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 10, 10));  // 1行2列，水平和垂直间距10像素
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 左侧及格学生表格
        String[] columnNames = {"Rank", "Student ID", "Student Name", "Email", "Avg Score", "Grade"};
        
        // 创建表格模型，禁止编辑
        DefaultTableModel passedModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // 添加及格学生数据到表格
        for (int i = 0; i < passedStudents.size(); i++) {
            StudentGradeInfo student = passedStudents.get(i);
            passedModel.addRow(new Object[]{
                i + 1,
                student.studentId,
                student.studentName,
                student.email,
                String.format("%.1f", student.average),
                student.gradeLetter
            });
        }
        
        JTable passedTable = new JTable(passedModel);
        passedTable.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        passedTable.setRowHeight(25);
        // 设置表头样式
        passedTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 16));
        passedTable.getTableHeader().setBackground(Color.BLUE);
        passedTable.getTableHeader().setForeground(Color.WHITE);
        passedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // 单选模式
        
        // 创建滚动面板
        JScrollPane passedScrollPane = new JScrollPane(passedTable);
        passedScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLUE, 2),
            "Passing Students (Average ≥ 50) - Total: " + passedStudents.size()
        ));
        
        // 右侧不及格学生表格
        // 排名接着及格学生之后，即从 passedStudents.size() + 1 开始
        DefaultTableModel failedModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        int startRank = passedStudents.size() + 1;  // 起始排名 = 及格学生总数 + 1
        for (int i = 0; i < failedStudents.size(); i++) {
            StudentGradeInfo student = failedStudents.get(i);
            // 平均分显示处理：没有成绩则显示"No grades"
            String avgDisplay = !student.hasScores() ? "No grades" : String.format("%.1f", student.average);
            String gradeDisplay = !student.hasScores() ? "N/A" : student.gradeLetter;
            failedModel.addRow(new Object[]{
                startRank + i,
                student.studentId,
                student.studentName,
                student.email,
                avgDisplay,
                gradeDisplay
            });
        }
        
        JTable failedTable = new JTable(failedModel);
        failedTable.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        failedTable.setRowHeight(25);
        // 设置表头样式
        failedTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 16));
        failedTable.getTableHeader().setBackground(Color.ORANGE);
        failedTable.getTableHeader().setForeground(Color.WHITE);
        failedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane failedScrollPane = new JScrollPane(failedTable);
        failedScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.ORANGE, 2),
            "Failing Students (Average < 50 or No Grades) - Total: " + failedStudents.size()
        ));
        
        mainPanel.add(passedScrollPane);
        mainPanel.add(failedScrollPane);
        
        rankFrame.add(mainPanel, BorderLayout.CENTER);
        
        // 底部按钮面板（统计信息和关闭按钮）
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 统计信息计算
        int totalStudents = allStudents.size();
        int gradedStudents = 0;
        double totalScore = 0;
        
        for (StudentGradeInfo student : allStudents) {
            if (student.hasScores()) {
                gradedStudents++;
                totalScore += student.getAverage();
            }
        }
        // 计算总体平均分（只计算有成绩的学生）
        double overallAverage = gradedStudents > 0 ? totalScore / gradedStudents : 0;
        
        // 统计信息标签
        JLabel statsLabel = new JLabel("Total Students: " + totalStudents + 
            "  |  Graded Students: " + gradedStudents + 
            "  |  Overall Average: " + String.format("%.1f", overallAverage));
        statsLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        statsLabel.setForeground(Color.BLUE);
        
        // 关闭按钮
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Times New Roman", Font.BOLD, 16));
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.setBackground(Color.RED);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);   // 取消焦点边框
        closeButton.setBorderPainted(false);  // 取消按钮边框
        closeButton.setOpaque(true);          // 设置不透明
        closeButton.addActionListener(e -> rankFrame.dispose());  // 点击关闭窗口
        
        bottomPanel.add(statsLabel);
        bottomPanel.add(closeButton);
        
        rankFrame.add(bottomPanel, BorderLayout.SOUTH);
        
        // 显示窗口
        rankFrame.setVisible(true);
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