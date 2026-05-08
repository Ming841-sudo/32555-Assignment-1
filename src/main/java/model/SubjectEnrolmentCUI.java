import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class SubjectEnrolmentCUI {

    private static final int MAX_SUBJECTS = 4;
    private static ArrayList<Subject> subjects = new ArrayList<>();
    private static String password = "Password123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {
            System.out.println("\n===== Subject Enrolment System =====");
            System.out.println("(e) Enrol Subject");
            System.out.println("(r) Remove Subject");
            System.out.println("(s) Show Subjects");
            System.out.println("(c) Change Password");
            System.out.println("(x) Exit");
            System.out.print("Student Course Menu (c/e/r/s/x): ");

            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "e":
                    enrolSubject();
                    break;
                case "r":
                    removeSubject(scanner);
                    break;
                case "s":
                    showSubjects();
                    break;
                case "c":
                    changePassword(scanner);
                    break;
                case "x":
                    running = false;
                    System.out.println("Exiting Subject Enrolment System...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    private static void enrolSubject() {
        if (subjects.size() >= MAX_SUBJECTS) {
            System.out.println("Students are allowed to enrol in 4 subjects only");
            return;
        }

        Subject subject = new Subject();
        subjects.add(subject);

        System.out.println("Enrolling in Subject-" + subject.getId());
        System.out.println("You are now enrolled in "
                + subjects.size()
                + " out of 4 subjects");
    }

    private static void removeSubject(Scanner scanner) {
        if (subjects.isEmpty()) {
            System.out.println("You are not enrolled in any subjects.");
            return;
        }

        System.out.print("Remove Subject by ID: ");
        String subjectId = scanner.nextLine().trim();

        Subject subjectToRemove = null;

        for (Subject subject : subjects) {
            if (subject.getId().equals(subjectId)) {
                subjectToRemove = subject;
                break;
            }
        }

        if (subjectToRemove != null) {
            subjects.remove(subjectToRemove);
            System.out.println("Dropping Subject-" + subjectId);
            System.out.println("You are now enrolled in "
                    + subjects.size()
                    + " out of 4 subjects");
        } else {
            System.out.println("Subject-" + subjectId + " does not exist");
        }
    }

    private static void showSubjects() {
        System.out.println("Showing " + subjects.size() + " subjects");

        for (Subject subject : subjects) {
            System.out.println(subject);
        }
    }

    private static void changePassword(Scanner scanner) {
        System.out.println("Updating Password");

        System.out.print("Current Password: ");
        String currentPassword = scanner.nextLine();

        if (!currentPassword.equals(password)) {
            System.out.println("Current password is incorrect");
            return;
        }

        System.out.print("New Password: ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Password does not match - try again");
            return;
        }

        password = newPassword;
        System.out.println("Password updated successfully");
    }

    private static class Subject {
        private String id;
        private int mark;
        private String grade;

        public Subject() {
            Random random = new Random();

            int subjectId = random.nextInt(999) + 1;
            this.id = String.format("%03d", subjectId);

            this.mark = random.nextInt(76) + 25;
            this.grade = calculateGrade(mark);
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