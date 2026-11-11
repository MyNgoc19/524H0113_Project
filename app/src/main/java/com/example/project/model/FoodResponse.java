package com.example.project.model;

import java.util.List;

public class FoodResponse {

    private boolean success;
    private List<Food> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Food> getData() {
        return data;
    }
}