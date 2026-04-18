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

    public int getID(){
        return subjectID;
    }

    public double getMark(){
        return mark;
    }

    public String getGrade(){
        return grade;
    }
}
