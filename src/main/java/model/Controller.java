package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Controller {
    private final Scanner scanner;
    private int nextStudentId;


    private Database database;

    public Controller() {
        this.scanner = new Scanner(System.in);
        this.nextStudentId = 1;

        this.database = new Database();
    }

    public void start() {
        boolean run = true;

        while (run) {
            System.out.print("University System: (A)dmin, (S)tudent, or X : ");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A":
                    showAdminMenu();
                    break;
                case "S":
                    showStudentMenu();
                    break;
                case "X":
                    System.out.println("Thank you");
                    run = false;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }


    }

    private void showStudentMenu() {
        boolean run = true;
        while (run) {
            System.out.println("Student System (l/r/x): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            switch (choice) {
                case "l":
                    login();
                    break;

                case "r":
                    register();
                    break;

                case "x":
                    run = false;

                    break;
            }
        }
    }

    private void showAdminMenu() {
        boolean run = true;
        while (run) {
            System.out.print("Admin System (c/g/p/r/s/x): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            switch (choice) {
                case "c":
                    System.out.println("Clearing students database");
                    // TODO
                    break;
                case "g":
                    System.out.println("Grade Grouping");
                    // TODO
                    break;
                case "p":
                    System.out.println("PASS/FAIL Partition");
                    // TODO
                    break;
                case "r":
                    System.out.print("Remove by ID: ");
                    // TODO
                    break;
                case "s":
                    System.out.println("Student List");
                    // TODO
                    break;
                case "x":
                    run = false;

                    break;
            }
        }
    }

    private void register() {
        System.out.println("Student Sign Up");
        while (true) {
            System.out.println("Email: ");
            String email = scanner.nextLine().trim();

            List<Student> students = database.loadStudents();
            System.out.println("Password: ");
            String password = scanner.nextLine().trim();
            if (!isValidEmail(email)) {
                System.out.println("Incorrect email format");
                continue;
            } else if (!isValidPassword(password)) {
                System.out.println("Incorrect password format");
                continue;
            } else {
                for (Student s : students) {
                    if (s.getEmail().equalsIgnoreCase(email)) {
                        System.out.println("Email already exist.");
                        continue;
                    }
                }

                System.out.println("email and password formats acceptable");
                System.out.println(getName(email));
                System.out.println("Enrolling Student " + getName(email));
                Student student = new Student(nextStudentId, getName(email), email, password);

                students.add(student);
                database.saveStudents(students);
                nextStudentId++;

                System.out.println("Email and password formats acceptable");
                System.out.println();
                System.out.println();
                System.out.println();
            }

            break;
        }

    }

    private boolean isValidEmail(String email) {
        String end = "@university.com";
        if (email == null) {
            return false;
        }

        if (!email.endsWith(end)) {
            return false;
        }

        String begin = email.substring(0, email.length() - end.length());

        int index = begin.indexOf('.');
        if (index == -1) {
            return false;
        }
        if (begin.indexOf('.', index + 1) != -1) {
            return false;
        }
        if (index == 0 || index == begin.length() - 1) {
            return false;
        }
        return true;

    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        if (!Character.isUpperCase(password.charAt(0))) {
            return false;
        }

        int letter = 0;
        int number = 0;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                letter++;
            } else if (Character.isDigit(c)) {
                number++;

            }
        }

        if (letter < 5) {
            return false;
        }

        if (number < 3) {
            return false;
        }

        return true;
    }

    private void login() {
        System.out.println("Student Sign In.");

        while (true) {
            System.out.println("Email: ");
            String email = scanner.nextLine().trim();
            System.out.println("Password: ");
            String password = scanner.nextLine().trim();

            List<Student> students = database.loadStudents();

            Student loginStudent = null;

            for (Student s : students) {
                if (s.getEmail().equalsIgnoreCase(email) && s.getPassword().equalsIgnoreCase(password)) {
                    loginStudent = s;
                    break;
                }
            }

            if (loginStudent == null) {
                System.out.println("Incorrect email or password format.");
                continue;
            }
            System.out.println("email and password formats acceptable");
            System.out.println("Welcome " + loginStudent.getName());
            showSubjectMenu(loginStudent, students);
            break;
        }
    }

    private String getName(String email) {
        String end = "@university.com";


        String begin = email.substring(0, email.length() - end.length());
        String[] parts = begin.split("\\.");

        String firstName = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase();
        String lastName = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1).toLowerCase();

        return firstName + " " + lastName;
    }

    private void showSubjectMenu(Student student, List<Student> students) {
        boolean run = true;

        while (run) {
            System.out.print("Student Course Menu (c/e/r/s/x): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "c":
                    changePassword(student, students);
                    break;

                case "e":
                    Subject subject = new Subject();
                    student.getSubjects().add(subject);
                    database.saveStudents(students);
                    System.out.println("Enrolled: " + subject);
                    break;

                case "r":
                    System.out.print("Remove Subject by ID: ");
                    int id = Integer.parseInt(scanner.nextLine().trim());

                    Subject removeSubject = null;
                    for (Subject s : student.getSubjects()) {
                        if (s.getID() == id) {
                            removeSubject = s;
                            break;
                        }
                    }

                    if (removeSubject != null) {
                        student.getSubjects().remove(removeSubject);
                        database.saveStudents(students);
                        System.out.println("Subject removed.");
                    } else {
                        System.out.println("Subject not found.");
                    }
                    break;

                case "s":

                    System.out.println(
                            "Showing " + student.getSubjects().size() + " subjects"
                    );

                    for (Subject s : student.getSubjects()) {
                        System.out.println(s);
                    }

                    break;

                case "x":
                    run = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void changePassword(Student student, List<Student> students) {

        System.out.print("New Password: ");

        String newPassword = scanner.nextLine().trim();

        if (!isValidPassword(newPassword)) {

            System.out.println("Incorrect password format");

            return;
        }

        student.resetPassword(newPassword);

        database.saveStudents(students);

        System.out.println("Password updated successfully");
    }
}