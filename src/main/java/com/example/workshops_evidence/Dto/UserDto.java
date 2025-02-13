package com.example.workshops_evidence.Dto;

import com.example.workshops_evidence.Entity.UserInfo;

public class UserDto {
    private int id;
    private String name;
    private String email;

    public UserDto(UserInfo user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}