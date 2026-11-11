package com.example.project.api;

import com.example.project.model.AuthResponse;
import com.example.project.model.FoodResponse;
import com.example.project.model.CartResponse;
import com.example.project.model.OrderResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Field;

public interface ApiService {

    // =========================================
    // REGISTER
    // =========================================

    @FormUrlEncoded
    @POST("auth/login.php")
    Call<AuthResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("auth/register.php")
    Call<AuthResponse> register(
            @Field("full_name") String fullName,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("auth/update_profile.php")
    Call<AuthResponse> updateProfile(

            @Field("user_id")
            int userId,

            @Field("full_name")
            String fullName,

            @Field("email")
            String email,

            @Field("phone")
            String phone
    );


    @FormUrlEncoded
    @POST("auth/change_password.php")
    Call<AuthResponse> changePassword(
            @Header("Authorization") String token,
            @Field("current_password") String currentPassword,
            @Field("new_password") String newPassword
    );

    // =========================================
    // LẤY DANH SÁCH MÓN ĂN
    // =========================================
    @GET("foods/get_foods.php")
    Call<FoodResponse> getFoods();


    // =========================================
    // THÊM MÓN VÀO GIỎ HÀNG
    // =========================================
    @FormUrlEncoded
    @POST("cart/add_cart.php")
    Call<CartResponse> addToCart(
            @Field("user_id") int userId,
            @Field("food_id") int foodId
    );


    // =========================================
    // LẤY GIỎ HÀNG
    // =========================================
    @FormUrlEncoded
    @POST("cart/get_cart.php")
    Call<CartResponse> getCart(
            @Field("user_id") int userId
    );

    @FormUrlEncoded
    @POST("cart/update_cart.php")
    Call<CartResponse> updateCart(
            @Field("cart_item_id") int cartItemId,
            @Field("quantity") int quantity
    );

    @FormUrlEncoded
    @POST("cart/delete_cart.php")
    Call<CartResponse> deleteCart(
            @Field("cart_item_id") int cartItemId
    );

    @FormUrlEncoded
    @POST("order/create_order.php")
    Call<OrderResponse> createOrder(

            @Field("user_id")
            int userId,

            @Field("receiver_name")
            String receiverName,

            @Field("receiver_phone")
            String receiverPhone,

            @Field("delivery_address")
            String deliveryAddress,

            @Field("shipping_fee")
            double shippingFee,

            @Field("payment_method")
            String paymentMethod
    );
}