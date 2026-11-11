package com.example.project.model;

public class CartItem {

    private int cart_item_id;
    private int food_id;
    private int quantity;

    private String food_name;
    private double price;
    private String description;
    private String image_url;


    public int getCart_item_id() {
        return cart_item_id;
    }

    public int getFood_id() {
        return food_id;
    }

    public int getQuantity() {
        return quantity;
    }

    // Dùng khi bấm nút + hoặc -
    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
}