package org.GUI_publish;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class AddSubjectFrame {
    

    private static final String SUBJECT_CSV_FILE_PATH = "SubjectInfo.csv";

    public static void showAddSubjectDialog(JFrame parentFrame) {

        JDialog subjectDialog = new JDialog(parentFrame, "Add New Subject", true);
        subjectDialog.setSize(450, 320);
        subjectDialog.setLayout(new BorderLayout());
        subjectDialog.setLocationRelativeTo(parentFrame);
        

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(Color.WHITE);
        

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        

        Font labelFont = new Font("Times New Roman", Font.PLAIN, 14);
        Font fieldFont = new Font("Times New Roman", Font.PLAIN, 14);
        Font displayFont = new Font("Times New Roman", Font.BOLD, 14);
        

        JLabel subjectIdLabel = new JLabel("Subject ID:");
        subjectIdLabel.setFont(labelFont);
        subjectIdLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(subjectIdLabel, gbc);
        

        String generatedSubjectId = AdminMainFrame.generateUniqueSubjectId();

        JTextField subjectIdDisplayField = new JTextField(15);
        subjectIdDisplayField.setFont(displayFont);
        subjectIdDisplayField.setText(generatedSubjectId);
        subjectIdDisplayField.setEditable(false);
        subjectIdDisplayField.setBackground(Color.WHITE);
        subjectIdDisplayField.setForeground(Color.BLACK);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(subjectIdDisplayField, gbc);
        

        JLabel subjectNameLabel = new JLabel("Subject Name:");
        subjectNameLabel.setFont(labelFont);
        subjectNameLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(subjectNameLabel, gbc);
        

        JTextField subjectNameField = new JTextField(15);
        subjectNameField.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(subjectNameField, gbc);
        

        JLabel creditsLabel = new JLabel("Credits:");
        creditsLabel.setFont(labelFont);
        creditsLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(creditsLabel, gbc);
        

        JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        creditsSpinner.setFont(fieldFont);
        creditsSpinner.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(creditsSpinner, gbc);
        
        subjectDialog.add(mainPanel, BorderLayout.CENTER);
        

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        

        JButton confirmButton = new JButton("Add Subject");
        confirmButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(120, 35));
        confirmButton.setBackground(Color.BLUE);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorderPainted(false);
        confirmButton.setOpaque(true);
        

        confirmButton.addActionListener(e -> {
            String subjectName = subjectNameField.getText().trim();
            String subjectId = subjectIdDisplayField.getText().trim();
            int credits = (int) creditsSpinner.getValue();
            

            if (subjectName.isEmpty()) {

                JOptionPane.showMessageDialog(subjectDialog, 
                    "Please enter the subject name!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            

            boolean saveSuccess = saveSubjectToCSVWithEmptyRow(subjectName, subjectId, credits);
            
            if (saveSuccess) {

                JOptionPane.showMessageDialog(subjectDialog, 
                    "Subject added successfully!\n\nSubject ID: " + subjectId + 
                    "\nSubject Name: " + subjectName + 
                    "\nCredits: " + credits,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                subjectDialog.dispose();
            } else {

                JOptionPane.showMessageDialog(subjectDialog, 
                    "Failed to save subject information!\nPlease check the file path.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBackground(Color.ORANGE);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(true);

        cancelButton.addActionListener(e -> subjectDialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        subjectDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        subjectDialog.setVisible(true);
    }
    

    private static boolean saveSubjectToCSVWithEmptyRow(String subjectName, String subjectId, int credits) {
        List<String[]> allRows = new ArrayList<>();
        BufferedReader reader = null;
        boolean isFirstLine = true;
        
        try {
            File csvFile = new File(SUBJECT_CSV_FILE_PATH);
            boolean isNewFile = !csvFile.exists();
            

            File parentDir = csvFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            

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
            

            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            

            String[] newSubjectRow = {
                subjectId,
                subjectName,
                String.valueOf(credits),
                timestamp
            };
            

            if (isNewFile) {
                String[] headerRow = {"Subject ID", "Subject Name", "Credits", "Added Date"};
                allRows.add(headerRow);
                allRows.add(newSubjectRow);
            } else {

                boolean foundEmptyRow = false;
                for (int i = 1; i < allRows.size(); i++) {
                    String[] row = allRows.get(i);

                    boolean isEmpty = true;
                    for (String field : row) {
                        if (field != null && !field.trim().isEmpty()) {
                            isEmpty = false;
                            break;
                        }
                    }
                    if (isEmpty) {

                        allRows.set(i, newSubjectRow);
                        foundEmptyRow = true;
                        System.out.println("Subject saved in empty row at index: " + i);
                        break;
                    }
                }
                

                if (!foundEmptyRow) {
                    allRows.add(newSubjectRow);
                    System.out.println("Subject appended to end of file");
                }
            }

            String userDir = System.getProperty("user.dir");
            System.out.println("Current working directory: " + userDir);
            System.out.println("Subject CSV file path: " + new File(SUBJECT_CSV_FILE_PATH).getAbsolutePath());
            

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
            
            System.out.println("Subject saved: " + subjectId + " - " + subjectName);
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
    

    private static String[] parseCSVLine(String line) {
        java.util.ArrayList<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {

                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {

                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
}