package com.example.project.model;

public class OrderResponse {

    private boolean success;
    private String message;

    private int order_id;

    private double subtotal;
    private double shipping_fee;
    private double total_amount;


    public boolean isSuccess() {
        return success;
    }


    public String getMessage() {
        return message;
    }


    public int getOrder_id() {
        return order_id;
    }


    public double getSubtotal() {
        return subtotal;
    }


    public double getShipping_fee() {
        return shipping_fee;
    }


    public double getTotal_amount() {
        return total_amount;
    }
}