package model;

import java.util.List;
import java.util.Scanner;

public class Controller {
    private final Scanner scanner;
    private int nextStudentId;
    private List<Student> students;

    public Controller() {
        this.scanner = new Scanner(System.in);
        this.nextStudentId = 1;
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
                    System.out.println("Student Sign In");
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
    private void showAdminMenu(){
        boolean run = true;
        while (run){
            System.out.print("Admin System (c/g/p/r/s/x): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            switch (choice){
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
    private void register(){
        System.out.println("Student Sign Up");
        while (true) {
            System.out.println("Email: ");
            String email = scanner.nextLine().trim();


            System.out.println("Password: ");
            String password = scanner.nextLine().trim();
            if (!isValidEmail(email)) {
                System.out.println("Incorrect email format");
                continue;
            } else if (!isValidPassword(password)) {
                System.out.println("Incorrect password format");
                continue;
            }else{
                System.out.println("email and password formats acceptable");
                System.out.println(getName(email));
                System.out.println("Enrolling Student " + getName(email));
                Student student = new Student(nextStudentId,getName(email),email,password);
                students.add(student);
                nextStudentId++;
            }

            break;
        }

    }

    private boolean isValidEmail(String email){
        String end = "university.com";
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

    private String getName(String email){
        String end = "@university.com";


        String begin = email.substring(0, email.length() - end.length());
        String[] parts = begin.split("\\.");

        String firstName = parts[0].substring(0,1).toUpperCase() + parts[0].substring(1).toLowerCase();
        String lastName = parts[1].substring(0,1).toUpperCase() + parts[1].substring(1).toLowerCase();

        return firstName + " " + lastName;
    }

}


