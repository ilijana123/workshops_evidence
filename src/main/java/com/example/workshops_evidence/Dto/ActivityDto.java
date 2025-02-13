package com.example.workshops_evidence.Dto;

import com.example.workshops_evidence.Entity.Activity;
import com.example.workshops_evidence.Entity.UserInfo;
import com.example.workshops_evidence.Entity.Workshop;
import com.example.workshops_evidence.Enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class ActivityDto {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
    public ActivityDto(Activity activity) {
        this.id = activity.getId();
        this.title = activity.getTitle();
        this.description = activity.getDescription();
        this.status = activity.getStatus();
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
}
