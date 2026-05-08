package org.GUI_publish;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SubjectEnrolmentFrame {

    private static final int MAX_SUBJECTS = 4;
    private static final String CSV_FILE_PATH = "StudentInfo.csv";

    private static HashMap<String, ArrayList<SubjectRecord>> studentSubjects = new HashMap<>();

    public static void showSubjectEnrolmentSystem(String studentEmail) {

        ArrayList<SubjectRecord> subjects =
                studentSubjects.getOrDefault(studentEmail, new ArrayList<>());

        studentSubjects.put(studentEmail, subjects);

        JFrame frame = new JFrame("Subject Enrolment System");
        frame.setSize(700, 450);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Subject Enrolment System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 28));
        titleLabel.setForeground(Color.PINK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JTextArea subjectArea = new JTextArea();
        subjectArea.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        subjectArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(subjectArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton enrolButton = new JButton("Enrol");
        JButton removeButton = new JButton("Remove");
        JButton showButton = new JButton("Show Subjects");
        JButton changePasswordButton = new JButton("Change Password");
        JButton exitButton = new JButton("Exit");

        buttonPanel.add(enrolButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(showButton);
        buttonPanel.add(changePasswordButton);
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        enrolButton.addActionListener(e -> {
            if (subjects.size() >= MAX_SUBJECTS) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Students are allowed to enrol in 4 subjects only",
                        "Enrolment Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            SubjectRecord subject = new SubjectRecord();
            subjects.add(subject);

            JOptionPane.showMessageDialog(
                    frame,
                    "Enrolling in Subject-" + subject.getId()
                            + "\nYou are now enrolled in "
                            + subjects.size()
                            + " out of 4 subjects"
            );

            updateSubjectArea(subjectArea, subjects);
        });

        removeButton.addActionListener(e -> {
            if (subjects.isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "You are not enrolled in any subjects.",
                        "Remove Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String subjectId = JOptionPane.showInputDialog(
                    frame,
                    "Remove Subject by ID:"
            );

            if (subjectId == null || subjectId.trim().isEmpty()) {
                return;
            }

            subjectId = subjectId.trim();

            SubjectRecord subjectToRemove = null;

            for (SubjectRecord subject : subjects) {
                if (subject.getId().equals(subjectId)) {
                    subjectToRemove = subject;
                    break;
                }
            }

            if (subjectToRemove != null) {
                subjects.remove(subjectToRemove);

                JOptionPane.showMessageDialog(
                        frame,
                        "Dropping Subject-" + subjectId
                                + "\nYou are now enrolled in "
                                + subjects.size()
                                + " out of 4 subjects"
                );
            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Subject-" + subjectId + " does not exist",
                        "Remove Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            updateSubjectArea(subjectArea, subjects);
        });

        showButton.addActionListener(e -> {
            updateSubjectArea(subjectArea, subjects);
        });

        changePasswordButton.addActionListener(e -> {
            changePassword(frame, studentEmail);
        });

        exitButton.addActionListener(e -> {
            frame.dispose();
        });

        frame.setContentPane(mainPanel);
        frame.setVisible(true);

        updateSubjectArea(subjectArea, subjects);
    }

    private static void updateSubjectArea(
            JTextArea subjectArea,
            ArrayList<SubjectRecord> subjects
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("Showing ").append(subjects.size()).append(" subjects\n\n");

        for (SubjectRecord subject : subjects) {
            sb.append(subject).append("\n");
        }

        subjectArea.setText(sb.toString());
    }

    private static void changePassword(JFrame frame, String studentEmail) {
        JPasswordField currentPasswordField = new JPasswordField(15);
        JPasswordField newPasswordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);

        Dimension fieldSize = new Dimension(180, 25);
        currentPasswordField.setPreferredSize(fieldSize);
        newPasswordField.setPreferredSize(fieldSize);
        confirmPasswordField.setPreferredSize(fieldSize);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Current Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(currentPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Confirm New Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(confirmPasswordField, gbc);

        int result = JOptionPane.showConfirmDialog(
                frame,
                panel,
                "Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Password fields cannot be empty.",
                    "Password Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!checkCurrentPassword(studentEmail, currentPassword)) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Current password is incorrect.",
                    "Password Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Password does not match - try again",
                    "Password Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        boolean success = updatePasswordInCSV(studentEmail, newPassword);

        if (success) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Password updated successfully."
            );
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Password update failed.",
                    "Password Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static boolean checkCurrentPassword(String email, String currentPassword) {
        File file = new File(CSV_FILE_PATH);

        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length >= 4) {
                    String csvEmail = parts[2].trim();
                    String csvPassword = parts[3].trim();

                    if (csvEmail.equalsIgnoreCase(email)
                            && csvPassword.equals(currentPassword)) {
                        return true;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean updatePasswordInCSV(String email, String newPassword) {
        File file = new File(CSV_FILE_PATH);

        if (!file.exists()) {
            return false;
        }

        ArrayList<String> lines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    lines.add(line);
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length >= 4 && parts[2].trim().equalsIgnoreCase(email)) {
                    parts[3] = newPassword;
                    line = String.join(",", parts);
                    updated = true;
                }

                lines.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!updated) {
            return false;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static class SubjectRecord {
        private String id;
        private int mark;
        private String grade;

        public SubjectRecord() {
            Random random = new Random();

            int randomId = random.nextInt(999) + 1;
            this.id = String.format("%03d", randomId);

            this.mark = random.nextInt(76) + 25;
            this.grade = calculateGrade(this.mark);
        }

        private String calculateGrade(int mark) {
            if (mark >= 85) {
                return "HD";
            } else if (mark >= 75) {
                return "D";
            } else if (mark >= 65) {
                return "C";
            } else if (mark >= 50) {
                return "P";
            } else {
                return "F";
            }
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "[ Subject::" + id
                    + " -- mark = " + mark
                    + " -- grade = " + grade
                    + " ]";
        }
    }
}