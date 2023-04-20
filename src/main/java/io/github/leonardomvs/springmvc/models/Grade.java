package io.github.leonardomvs.springmvc.models;

public interface Grade {
    double getGrade();

    int getId();

    void setId(int id);

    int getStudentId();

    void setStudentId(int studentId);

    void setGrade(double grade);
}
