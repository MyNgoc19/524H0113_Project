package com.example.project.model;

public class Food {

    private int food_id;
    private int category_id;
    private String food_name;
    private double price;
    private String description;
    private String image_url;
    private int is_featured;

    public int getFood_id() {
        return food_id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public String getFood_name() {
        return food_name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getImage_url() {
        return image_url;
    }

    public int getIs_featured() {
        return is_featured;
    }
}