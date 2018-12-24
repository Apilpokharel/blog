package com.blog.app.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.util.ArrayList;

@Document(collection="users")
public class UserSchema {

    @Id
    private String id;

    @Size(min=3, message="must be filled")
    private String email;

    @Size(min=3, message="cannot be less than 6 characters")
    private String password;

    @Size(min=3, message="must be filled")
    private String username;
    private String createdAt;
    private ArrayList<Tokens> tokens;
    private String image;

    public ArrayList<Tokens> getTokens() {
        return tokens;
    }

    public void setTokens(ArrayList<Tokens> tokens) {
        this.tokens = tokens;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public UserSchema(){
        super();
    }




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private String gender;
    private String profession;
    private boolean checked;
}
