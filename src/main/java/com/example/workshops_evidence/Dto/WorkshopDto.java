package com.example.workshops_evidence.Dto;

import com.example.workshops_evidence.Entity.Activity;
import com.example.workshops_evidence.Entity.Workshop;
import com.example.workshops_evidence.Enums.Status;
import com.example.workshops_evidence.Enums.TaskStatus;

public class WorkshopDto {
    private int id;
    private String title;
    private String date;
    private String type;
    private String priority;
    private Status status;
    public WorkshopDto(Workshop workshop) {
        this.id = workshop.getId();
        this.title = workshop.getTitle();
        this.status = workshop.getStatus();
        this.priority = workshop.getPriority();
        this.date = workshop.getDate();
        this.type = workshop.getType();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
