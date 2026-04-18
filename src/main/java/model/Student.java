package model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private static final int max_Subject = 4;
    private int id;
    private String name;
    private String email;
    private String password;
    private List<Subject> subjects;

    public Student(int id, String name, String email, String password){
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.subjects = new ArrayList<>();
    }

    public Student(int id, String name, String email, String password, List<Subject> subjects){
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.subjects = subjects;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void resetPassword(String password){
        this.password = password;
    }

    public String getFormattedId() {
        return String.format("%06d", id);
    }


}
