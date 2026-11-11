package com.example.project.model;

public class AuthResponse {

    private boolean success;
    private String message;
    private User user;
    private String access_token;
    private String token_type;
    private int expires_in;


    public boolean isSuccess() {
        return success;
    }


    public String getMessage() {
        return message;
    }


    public User getUser() {
        return user;
    }

    public String getAccess_token() {
        return access_token;
    }
}