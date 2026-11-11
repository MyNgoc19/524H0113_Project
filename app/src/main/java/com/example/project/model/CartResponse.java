package com.example.project.model;

import java.util.List;

public class CartResponse {

    private boolean success;
    private String message;
    private List<CartItem> data;


    public boolean isSuccess() {
        return success;
    }


    public String getMessage() {
        return message;
    }


    public List<CartItem> getData() {
        return data;
    }
}