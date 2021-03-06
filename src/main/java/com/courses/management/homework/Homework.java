package com.courses.management.homework;

import com.courses.management.common.BaseEntity;
import com.courses.management.course.Course;
import com.courses.management.solution.Solution;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.InputStream;
import java.util.List;

@Entity
@Table(name = "home_work")
@Component
public class Homework extends BaseEntity {
    private String title;
    private String path;
    private String text;
    private Course course;
    private List<Solution> solutions;
    private InputStream data;

    public Homework() {
    }

    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "file_path")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Column(name = "text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "homework")
    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;
    }

    @ManyToOne
    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Transient
    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }
}
