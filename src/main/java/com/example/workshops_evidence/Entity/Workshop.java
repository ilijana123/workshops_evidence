package com.example.workshops_evidence.Entity;

import jakarta.persistence.*;
import com.example.workshops_evidence.Enums.Status;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private Status status;
    private String date;
    private int ownerId;
    private String type;
    private String priority;
    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities;

    public Workshop(String title, Status status, String date, int ownerId, String type, String priority, List<Activity> activities) {
        this.title = title;
        this.status = status;
        this.date = date;
        this.ownerId = ownerId;
        this.type = type;
        this.priority = priority;
        this.activities = activities;
    }
    public Workshop(String title, Status status, String date, int ownerId, String type, String priority) {
        this.title = title;
        this.status = status;
        this.date = date;
        this.ownerId = ownerId;
        this.type = type;
        this.priority = priority;
    }

    public Workshop(String title, String date, String type, String priority) {
        this.title = title;
        this.date = date;
        this.type = type;
        this.priority = priority;
    }
    public Workshop() {

    }

    public Workshop(String priority) {
        this.priority = priority;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Activity> getActivities() {
        if(activities == null) {
            activities = new ArrayList<>();
        }
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
