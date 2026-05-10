package model;

public class Subject {
    private int subjectID;
    private double mark;
    private String grade;

    public Subject(int subjectID, double mark, String grade) {
        this.subjectID = subjectID;
        this.mark = mark;
        this.grade = grade;
    }

    public int getID() {
        return subjectID;
    }

    public double getMark() {
        return mark;
    }

    public String getGrade() {
        return grade;
    }

    public Subject() {

        java.util.Random random = new java.util.Random();

        this.subjectID = random.nextInt(999) + 1;

        this.mark = random.nextInt(76) + 25;

        this.grade = calculateGrade(this.mark);
    }

    private String calculateGrade(double mark) {

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

    @Override
    public String toString() {

        return "[ Subject::"
                + String.format("%03d", subjectID)
                + " -- mark = "
                + String.format("%.0f", mark)
                + " -- grade = "
                + grade
                + " ]";
    }
}