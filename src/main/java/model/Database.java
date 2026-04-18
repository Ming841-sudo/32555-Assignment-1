package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final String fileName;

    public Database(String fileName) {
        this.fileName = "students.data";
        createFile();
    }

    private void createFile(){
        File file = new File(fileName);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error creating file.");
        }
    }

    public List<Student> loadStudents(){
        List<Student> students = new ArrayList<>();
        try (BufferedReader read = new BufferedReader(new FileReader(fileName))){
            String line;

            while ((line = read.readLine()) != null){
                String[] mainParts = line.split("\\|");

                String studentParts = mainParts[0];
                String subjectParts = "";
                if(mainParts[1].length() > 1){
                    studentParts = mainParts[1];
                }

                String[] studentInfo = studentParts.split(",");

                int id = Integer.parseInt(studentInfo[0]);
                String name = studentInfo[1];
                String email = studentInfo[2];
                String password = studentInfo[3];

                List<Subject> subjects = new ArrayList<>();
                if(!subjectParts.isEmpty()){
                    String[] subjectsInfo = subjectParts.split(";");

                    for(String item : subjectsInfo){
                        if(item.trim().isEmpty()){
                            continue;
                        }

                        String[] subjectField = item.split(":");
                        int subjectID = Integer.parseInt(subjectField[0]);
                        double mark = Double.parseDouble(subjectField[1]);
                        String grade = subjectField[2];

                        subjects.add(new Subject(subjectID, mark, grade));

                    }
                }

                students.add(new Student(id,name, email, password, subjects));
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }

        return students;
    }

    public void saveStudents(List<Student> students){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Student s: students){
                StringBuilder line = new StringBuilder();

                line.append(s.getId()).append(",")
                        .append(s.getName()).append(",")
                        .append(s.getEmail()).append(",")
                        .append(s.getPassword());

                line.append("|");

                List<Subject> subjects = s.getSubjects();
                for(int i = 0; i < subjects.size(); i++){
                    Subject sub = subjects.get(i);

                    line.append(sub.getID()).append(":")
                            .append(sub.getMark()).append(":")
                            .append(sub.getGrade());

                    if(i < subjects.size() - 1){
                        line.append(";");
                    }
                }

                writer.write(line.toString());
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error writing file.");
        }
    }

}
