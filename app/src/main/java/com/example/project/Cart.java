package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapter.CartAdapter;
import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.model.CartItem;
import com.example.project.model.CartResponse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Cart extends AppCompatActivity {
    private TextView btnBack;
    private TextView txtCartCount;
    private TextView txtSubtotal;
    private TextView txtCartTotal;
    private LinearLayout layoutSummary;
    private LinearLayout layoutEmptyCart;
    private Button btnCheckout;
    private RecyclerView recyclerCart;
    private LinearLayout navHome;
    private LinearLayout navSearch;
    private LinearLayout navCart;
    private LinearLayout navProfile;
    private CartAdapter cartAdapter;

    // DATA
    // Lưu tổng tiền hiện tại của giỏ hàng
    // để truyền sang Checkout
    private double currentSubtotal = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // ÁNH XẠ VIEW
        initViews();

        // RECYCLER VIEW
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this);
        recyclerCart.setAdapter(cartAdapter);

        // BACK
        btnBack.setOnClickListener(v -> {finish();});

        // HOME
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(
                            Cart.this,
                            Home.class
                    );
            startActivity(intent);
            finish();
        });

        // PROFILE
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Cart.this,
                    Profile.class
            );
            startActivity(intent);
        });

        // SEARCH
        navSearch.setOnClickListener(v -> {
            Intent intent = new Intent(
                            Cart.this,
                            Menu.class
                    );
            intent.putExtra("mode", "search"
            );
            startActivity(intent);
        });

        // CART
        navCart.setOnClickListener(v -> {
            Toast.makeText(
                    Cart.this,
                    "Bạn đang ở Giỏ hàng",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // CHECKOUT
        btnCheckout.setOnClickListener(v -> {

            // KIỂM TRA TỔNG TIỀN
            if (currentSubtotal <= 0) {
                Toast.makeText(
                        Cart.this,
                        "Giỏ hàng đang trống",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // MỞ CHECKOUT
            Intent intent = new Intent(
                    Cart.this,
                    Checkout.class
            );


            // TRUYỀN TỔNG TIỀN SANG CHECKOUT
            intent.putExtra(
                    "subtotal",
                    currentSubtotal
            );
            startActivity(intent);
        });

    }


    // ÁNH XẠ VIEW
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtCartCount = findViewById(R.id.txtCartCount);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtCartTotal = findViewById(R.id.txtCartTotal);
        layoutSummary = findViewById(R.id.layoutSummary);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        recyclerCart = findViewById(R.id.recyclerCart);
        navHome = findViewById(R.id.navHome);
        navSearch = findViewById(R.id.navSearch);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile);
    }


    // LOAD GIỎ HÀNG
    private void loadCart() {
        // LẤY USER_ID
        SharedPreferences sharedPreferences =
                getSharedPreferences(
                        "SushiZenAccount",
                        MODE_PRIVATE
                );

        int userId = sharedPreferences.getInt(
                        "user_id",
                        -1
                );


        // KHÔNG TÌM THẤY USER
        if (userId == -1) {
            Toast.makeText(
                    Cart.this,
                    "Không tìm thấy user_id. Vui lòng đăng nhập lại.",
                    Toast.LENGTH_LONG
            ).show();
            showCart(null);
            return;
        }

        // API SERVICE
        ApiService apiService = ApiClient
                        .getClient()
                        .create(
                                ApiService.class
                        );


        // GỌI API GET CART
        apiService
                .getCart(userId)
                .enqueue(

                        new Callback<CartResponse>() {

                            @Override
                            public void onResponse(
                                    @NonNull Call<CartResponse> call,
                                    @NonNull Response<CartResponse> response
                            ) {
                                // HTTP ERROR
                                if (!response.isSuccessful()) {
                                    Toast.makeText(
                                            Cart.this,
                                            "Lỗi HTTP: "
                                                    + response.code(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                    return;
                                }

                                // BODY NULL
                                if (response.body() == null) {
                                    Toast.makeText(
                                            Cart.this,
                                            "Server không trả dữ liệu",
                                            Toast.LENGTH_LONG
                                    ).show();
                                    return;
                                }
                                CartResponse cartResponse =
                                        response.body();

                                // API BÁO LỖI
                                if (!cartResponse.isSuccess()) {
                                    String message = cartResponse.getMessage();
                                    if (message == null || message.isEmpty()) {
                                        message = "Không thể tải giỏ hàng";
                                    }

                                    Toast.makeText(
                                            Cart.this,
                                            message,
                                            Toast.LENGTH_LONG
                                    ).show();
                                    showCart(null);
                                    return;
                                }

                                // LẤY DANH SÁCH SẢN PHẨM
                                List<CartItem> items = cartResponse.getData();

                                // HIỂN THỊ CART
                                showCart(items);
                            }

                            @Override
                            public void onFailure(
                                    @NonNull Call<CartResponse> call,
                                    @NonNull Throwable t
                            ) {

                                Toast.makeText(
                                        Cart.this,
                                        "Lỗi kết nối: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // HIỂN THỊ GIỎ HÀNG
    private void showCart(
            List<CartItem> items
    ) {

        // GIỎ HÀNG RỖNG
        if (items == null || items.isEmpty()) {
            currentSubtotal = 0;
            cartAdapter.updateData(null);

            // ẨN RECYCLER VIEW
            recyclerCart.setVisibility(View.GONE);

            // HIỆN EMPTY CART
            layoutEmptyCart.setVisibility(View.VISIBLE);

            // ẨN SUMMARY
            layoutSummary.setVisibility(View.GONE);

            // ẨN CHECKOUT
            btnCheckout.setVisibility(View.GONE);

            // RESET SỐ LƯỢNG
            txtCartCount.setText("0 món trong giỏ");

            // RESET TIỀN
            txtSubtotal.setText(formatMoney(0));
            txtCartTotal.setText(formatMoney(0));
            return;
        }


        // GIỎ HÀNG CÓ DATA
        recyclerCart.setVisibility(View.VISIBLE);
        layoutEmptyCart.setVisibility(View.GONE);
        layoutSummary.setVisibility(View.VISIBLE);
        btnCheckout.setVisibility(View.VISIBLE);


        // ĐƯA DATA VÀO ADAPTER
        cartAdapter.updateData(items);

        // TÍNH TỔNG
        int totalQuantity = 0;
        double subtotal = 0;
        for (CartItem item : items) {
            totalQuantity += item.getQuantity();             // TỔNG SỐ LƯỢNG
            subtotal += item.getPrice() * item.getQuantity();// TỔNG TIỀN
        }

        currentSubtotal = subtotal;                             // LƯU SUBTOTAL ĐỂ TRUYỀN SANG CHECKOUT
        txtCartCount.setText(totalQuantity + " món trong giỏ"); // HIỂN THỊ SỐ LƯỢNG
        txtSubtotal.setText(formatMoney(currentSubtotal));     // HIỂN THỊ TẠM TÍNH
        txtCartTotal.setText(formatMoney(currentSubtotal));    // HIỂN THỊ TỔNG TIỀN
    }

    // FORMAT TIỀN
    private String formatMoney(
            double amount
    ) {

        NumberFormat formatter = NumberFormat.getNumberInstance(
                new Locale("vi", "VN"));

        return formatter.format(
                amount
        ) + "đ";
    }


    // QUAY LẠI CART → LOAD LẠI DATA
    @Override
    protected void onResume() {
        super.onResume();
        loadCart(); // Load lại giỏ hàng từ MySQL
    }
}