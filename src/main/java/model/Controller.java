package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Controller {
    private final Scanner scanner;

    private Database database;
    private List<Student> students;

    public Controller() {
        this.scanner = new Scanner(System.in);

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
            System.out.print("Student System (l/r/x): ");
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

    private String generateUniqueStudentId() {

        Random random = new Random();

        String id;

        boolean exists;

        List<Student> students = database.loadStudents();

        do {

            int number = random.nextInt(999999) + 1;

            id = String.format("%06d", number);

            exists = false;

            for (Student s : students) {

                if (s.getId().equals(id)) {

                    exists = true;
                    break;
                }
            }

        } while (exists);

        return id;
    }
    private void showAdminMenu(){
        boolean run = true;
        while (run){
            System.out.print("Admin System (c/g/p/r/s/x): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            switch (choice){

                case "c":

                    System.out.println("Clearing students database");

                    while (true) {

                        System.out.print(
                                "Are you sure you want to clear the database (Y)ES/(N)O: "
                        );

                        String confirm = scanner.nextLine().trim().toUpperCase();

                        if (confirm.equals("Y")) {

                            database.saveStudents(new ArrayList<>());

                            System.out.println("Students data cleared.");

                            break;

                        } else if (confirm.equals("N")) {



                            break;

                        } else {

                            System.out.println("Invalid option.");

                        }
                    }

                    break;

                case "g":
                    System.out.println("Grade Grouping");

                    List<Student> gStudents = database.loadStudents();
                    String[] grades = {"HD", "D", "C", "P", "F"};
                    boolean isEmpty = false;
                    for (String grade : grades) {
                        List<String> entries = new ArrayList<>();

                        for (Student s : gStudents) {
                            if (s.getSubjects().isEmpty()) {
                                continue;
                            }

                            double avg = getAverageMark(s);
                            String finalGrade = Subject.calculateGrade(avg);

                            if (finalGrade.equalsIgnoreCase(grade)) {
                                entries.add(formatStudentResult(s));
                            }
                        }

                        if (!entries.isEmpty()) {
                            isEmpty = true;
                            System.out.println(grade + " --> [" + String.join(", ", entries) + "]");
                        }

                    }
                    if (!isEmpty){
                        System.out.println("< Nothing to display >");
                    }

                    break;

                case "p":
                    System.out.println("PASS/FAIL Partition");

                    List<Student> pStudents = database.loadStudents();
                    List<String> passEntries = new ArrayList<>();
                    List<String> failEntries = new ArrayList<>();

                    for (Student s : pStudents) {
                        if (s.getSubjects().isEmpty()) {
                            continue;
                        }

                        double avg = getAverageMark(s);

                        if (avg >= 50) {
                            passEntries.add(formatStudentResult(s));
                        } else {
                            failEntries.add(formatStudentResult(s));
                        }
                    }
                    if(!passEntries.isEmpty() || !failEntries.isEmpty()){
                        System.out.println("FAIL --> [" + String.join(", ", failEntries) + "]");
                        System.out.println("PASS --> [" + String.join(", ", passEntries) + "]");
                    }else{
                        System.out.println("< Nothing to display >");
                    }

                    break;

                case "r":

                    System.out.print("Remove by ID: ");

                    String targetId = scanner.nextLine().trim();

                    List<Student> rStudents = database.loadStudents();

                    Student toRemove = null;

                    for (Student s : rStudents) {

                        if (s.getId().equals(targetId)) {

                            toRemove = s;
                            break;
                        }
                    }

                    if (toRemove == null) {

                        System.out.println(
                                "Student " + targetId + " does not exist"
                        );

                    } else {

                        rStudents.remove(toRemove);

                        database.saveStudents(rStudents);

                        System.out.println(
                                "Removing Student "
                                        + toRemove.getId()
                                        + " Account"
                        );
                    }

                    break;

                case "s":
                    System.out.println("Student List");
                    List<Student> sStudents = database.loadStudents();
                    if (sStudents.isEmpty()) {
                        System.out.println("< Nothing to display >");
                        break;
                    }
                    for (Student s : sStudents) {
                        System.out.println(s.getName() + " :: " + s.getId()  + " --> Email: " + s.getEmail());
                    }
                    break;

                case "x":
                    run = false;
                    break;
            }
        }
    }
    private void register(){
        System.out.println("Student Sign Up");
        while (true) {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            List<Student> students = database.loadStudents();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (!isValidEmail(email)) {
                System.out.println("Incorrect email format");
                continue;
            } else if (!isValidPassword(password)) {
                System.out.println("Incorrect password format");
                continue;
            }else{
                boolean exists = false;
                for (Student s : students){
                    if(s.getEmail().equalsIgnoreCase(email)){

                        exists = true;
                        break;
                    }
                }
                if(exists){
                    System.out.println("Email already exist.");
                    continue;
                }

                System.out.println("email and password formats acceptable");
                System.out.println("Name: " + getName(email));
                System.out.println("Enrolling Student " +   getName(email));
                String id = generateUniqueStudentId();
                Student student = new Student(id,getName(email),email,password);

                students.add(student);
                database.saveStudents(students);



            }

            break;
        }

    }

    private boolean isValidEmail(String email){
        String end = "@university.com";
        if(email == null){
            return false;
        }

        if(!email.endsWith(end)){
            return false;
        }

        String begin = email.substring(0, email.length() - end.length());

        int index = begin.indexOf('.');
        if(index == -1){
            return false;
        }
        if(begin.indexOf('.', index + 1) != -1){
            return false;
        }
        if(index == 0 || index == begin.length() - 1){
            return false;
        }
        return true;

    }

    private boolean isValidPassword(String password){
        if(password == null || password.length() < 8){
            return false;
        }

        if(!Character.isUpperCase(password.charAt(0))){
            return false;
        }

        int letter = 0;
        int number = 0;

        for(char c : password.toCharArray()){
            if (Character.isLetter(c)){
                letter++;
            } else if (Character.isDigit(c)) {
                number++;
                
            }
        }

        if(letter < 5){
            return false;
        }

        if(number < 3){
            return false;
        }

        return true;
    }

    private void login(){
        System.out.println("Student Sign In.");

        while (true){
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (!isValidEmail(email) || !isValidPassword(password)) {
                System.out.println("Incorrect email or password format.");
                continue;
            }
            List<Student> students = database.loadStudents();

            Student loginStudent = null;

            for (Student s : students){
                if(s.getEmail().equals(email) && s.getPassword().equals(password)){
                    loginStudent = s;
                    break;
                }
            }

            if(loginStudent == null){
                System.out.println("email and password formats acceptable");
                System.out.println("Student does not exist");
                continue;
            }
            System.out.println("email and password formats acceptable");
            System.out.println("Welcome " + loginStudent.getName());
            showSubjectMenu(loginStudent,students);
            break;
        }
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
                    if(student.getSubjects().size() >= 4){
                        System.out.println("Students are allowed to enrol in 4 subjects only");
                        break;
                    }
                    int subjectID = generateUniqueSubjectId(student);

                    Subject subject = new Subject(subjectID);
                    student.getSubjects().add(subject);
                    database.saveStudents(students);
                    System.out.println("Enrolling in Subject-" + subject.getID());
                    System.out.println("You are now enrolled in " +  student.getSubjects().size() + " out of 4 subjects");
                    break;

                case "r":
                    System.out.print("Remove Subject by ID: ");

                    try {
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

                            System.out.println("Droping Subject-" + removeSubject.getID());
                            System.out.println("You are now enrolled in " +  student.getSubjects().size() + " out of 4 subjects");
                        } else {
                            System.out.println("Subject not found.");
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid subject ID.");
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
        System.out.println("Updating Password");

        System.out.print("New Password: ");

        String newPassword = scanner.nextLine().trim();
        System.out.print("Confirm Password: ");


        if (!isValidPassword(newPassword)) {

            System.out.println("Incorrect password format");

            return;
        }
        while (true) {

            System.out.print("Confirm Password: ");
            String confirmPassword = scanner.nextLine().trim();

            if (!newPassword.equals(confirmPassword)) {

                System.out.println("Password does not match -- try again");

                continue;
            }


            student.resetPassword(newPassword);

            database.saveStudents(students);



            break;
        }


    }

    private int generateUniqueSubjectId(Student student) {
        java.util.Random random = new java.util.Random();

        int id;
        boolean exists;

        do {
            id = random.nextInt(999) + 1;
            exists = false;

            for (Subject s : student.getSubjects()) {
                if (s.getID() == id) {
                    exists = true;
                    break;
                }
            }

        } while (exists);

        return id;
    }

    private double getAverageMark(Student student) {
        if (student.getSubjects().isEmpty()) {
            return 0;
        }

        double total = 0;
        for (Subject subject : student.getSubjects()) {
            total += subject.getMark();
        }

        return total / student.getSubjects().size();
    }

    private String formatStudentResult(Student student) {
        double avg = getAverageMark(student);
        String grade = Subject.calculateGrade(avg);

        return student.getName() + " :: " + student.getId() + " --> GRADE: " + grade + " - MARK: " + String.format("%.2f", avg);
    }

    private String getName(String email){
        String end = "@university.com";


        String begin = email.substring(0, email.length() - end.length());
        String[] parts = begin.split("\\.");

        String firstName = parts[0].substring(0,1).toUpperCase() + parts[0].substring(1).toLowerCase();
        String lastName = parts[1].substring(0,1).toUpperCase() + parts[1].substring(1).toLowerCase();

        return firstName + " " + lastName;
    }

}


