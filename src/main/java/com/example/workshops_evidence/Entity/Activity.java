package com.example.workshops_evidence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.example.workshops_evidence.Enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String description;
    private TaskStatus status;

    @ManyToMany(mappedBy = "activities")
    private List<UserInfo> users;
    @ManyToOne
    @JoinColumn(name = "workshop_id", nullable = false)
    private Workshop workshop;

    public Activity(Workshop workshop, List<UserInfo> users, TaskStatus status, String description, String title) {
        this.workshop = workshop;
        this.users = users;
        this.status = status;
        this.description = description;
        this.title = title;
    }

    public Activity() {
    }

    public Activity(String title, String description, TaskStatus status, Workshop workshop) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.workshop = workshop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public List<UserInfo> getUsers() {
        if (users == null) {
            users = new ArrayList<>();
        }
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

    public Workshop getWorkshop() {
        return workshop;
    }

    public void setWorkshop(Workshop workshop) {
        this.workshop = workshop;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
