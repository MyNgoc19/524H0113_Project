
package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

public class DriverOrdersActivity extends AppCompatActivity {

    private static final int RED = Color.rgb(183, 28, 28);
    private static final int DARK = Color.rgb(26, 26, 26);
    private static final int GRAY = Color.rgb(119, 119, 119);

    // Dùng 10.0.2.2 khi chạy Android Emulator
    private static final String BASE_URL =
            "http://10.0.2.2/sushizen_api/driver/";

    private LinearLayout layoutOrders;
    private LinearLayout layoutOrderDetails;

    private TextView txtDetailTitle;
    private TextView txtDetailContent;
    private Button btnCloseDetails;

    private TextView btnLoadOrders;
    private ScrollView ordersScrollView;

    private TextView tabOffered;
    private TextView tabActive;
    private TextView tabDelivered;

    private TextView txtCountOffered;
    private TextView txtCountActive;
    private TextView txtCountDelivered;

    private String selectedTab = "OFFERED";
    private JSONArray allOrders = new JSONArray();

    private int driverId = -1;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_driver_orders);

        layoutOrders = findViewById(R.id.layoutOrders);

        layoutOrderDetails = findViewById(R.id.layoutOrderDetails);
        txtDetailTitle = findViewById(R.id.txtDetailTitle);
        txtDetailContent = findViewById(R.id.txtDetailContent);
        btnCloseDetails = findViewById(R.id.btnCloseDetails);

        btnCloseDetails.setOnClickListener(v ->
                layoutOrderDetails.setVisibility(View.GONE)
        );

        btnLoadOrders = findViewById(R.id.btnLoadOrders);
        ordersScrollView = findViewById(R.id.scrollDriver);

        LinearLayout navDriverHome =
                findViewById(R.id.navDriverHome);

        LinearLayout navDriverProfile =
                findViewById(R.id.navDriverProfile);

        tabOffered = findViewById(R.id.tabOffered);
        tabActive = findViewById(R.id.tabActive);
        tabDelivered = findViewById(R.id.tabDelivered);

        txtCountOffered = findViewById(R.id.txtCountOffered);
        txtCountActive = findViewById(R.id.txtCountActive);
        txtCountDelivered = findViewById(R.id.txtCountDelivered);

        driverId = getIntent().getIntExtra("driver_id", -1);

        SharedPreferences prefs =
                getSharedPreferences("SushiZenAccount", MODE_PRIVATE);

        if (driverId <= 0) {
            driverId = prefs.getInt("driver_id", -1);
        }

        String name = prefs.getString("fullName", "");

        ((TextView) findViewById(R.id.txtDriverGreeting))
                .setText(
                        name == null || name.trim().isEmpty()
                                ? "Xin chào, tài xế!"
                                : "Xin chào, " + name + "!"
                );

        btnLoadOrders.setOnClickListener(v -> loadOrders());

        tabOffered.setOnClickListener(v -> selectTab("OFFERED"));
        tabActive.setOnClickListener(v -> selectTab("ACTIVE"));
        tabDelivered.setOnClickListener(v -> selectTab("DELIVERED"));

        // TRANG CHỦ TÀI XẾ
        navDriverHome.setOnClickListener(v -> {
            ordersScrollView.smoothScrollTo(0, 0);
            loadOrders();
        });

        // ĐƠN HÀNG TÀI XẾ
        // Đơn hàng được quản lý trực tiếp tại Trang chủ, không cần tab điều hướng riêng.

        // TÀI KHOẢN TÀI XẾ
        navDriverProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DriverOrdersActivity.this,
                    Profile.class
            );

            intent.putExtra("driver_id", driverId);
            startActivity(intent);
        });

        updateTabStyles();

        if (driverId <= 0) {
            showMessage("Không tìm thấy thông tin tài xế");
            return;
        }

        loadOrders();
    }

    // TÀI KHOẢN TÀI XẾ
    private void showDriverProfile() {
        SharedPreferences prefs =
                getSharedPreferences("SushiZenAccount", MODE_PRIVATE);

        String fullName = prefs.getString("fullName", "");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("phone", "");

        if (phone == null || phone.trim().isEmpty()) {
            phone = "Chưa cập nhật";
        }

        new AlertDialog.Builder(this)
                .setTitle("Tài khoản tài xế")
                .setMessage(
                        "Họ tên: " + fullName
                                + "\nEmail: " + email
                                + "\nSố điện thoại: " + phone
                                + "\nMã tài xế: " + driverId
                )
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Đăng xuất", (dialog, which) -> {
                    prefs.edit().clear().apply();

                    Intent intent = new Intent(
                            DriverOrdersActivity.this,
                            Login.class
                    );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void selectTab(String tab) {
        selectedTab = tab;

        layoutOrderDetails.setVisibility(View.GONE);

        updateTabStyles();
        renderOrders();
    }

    private void updateTabStyles() {
        tabOffered.setTextColor(
                "OFFERED".equals(selectedTab) ? RED : GRAY
        );

        tabActive.setTextColor(
                "ACTIVE".equals(selectedTab) ? RED : GRAY
        );

        tabDelivered.setTextColor(
                "DELIVERED".equals(selectedTab) ? RED : GRAY
        );

        tabOffered.setTypeface(
                null,
                "OFFERED".equals(selectedTab)
                        ? Typeface.BOLD : Typeface.NORMAL
        );

        tabActive.setTypeface(
                null,
                "ACTIVE".equals(selectedTab)
                        ? Typeface.BOLD : Typeface.NORMAL
        );

        tabDelivered.setTypeface(
                null,
                "DELIVERED".equals(selectedTab)
                        ? Typeface.BOLD : Typeface.NORMAL
        );
    }

    private void loadOrders() {
        layoutOrderDetails.setVisibility(View.GONE);
        showMessage("Đang tải danh sách đơn hàng...");

        new Thread(() -> {
            try {
                JSONObject response = sendRequest(
                        "GET",
                        BASE_URL + "get_orders.php?driver_id=" + driverId,
                        null
                );

                mainHandler.post(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (!response.optBoolean("success")) {
                        showMessage(
                                response.optString(
                                        "message",
                                        "Không thể tải đơn hàng"
                                )
                        );
                        return;
                    }

                    JSONArray orders = response.optJSONArray("orders");

                    allOrders = orders == null
                            ? new JSONArray()
                            : orders;

                    updateStatistics();
                    renderOrders();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        showMessage(
                                "Lỗi kết nối: " + e.getMessage()
                        );
                    }
                });
            }
        }).start();
    }

    private void updateStatistics() {
        int offered = 0;
        int active = 0;
        int delivered = 0;

        for (int i = 0; i < allOrders.length(); i++) {
            JSONObject order = allOrders.optJSONObject(i);

            if (order == null) continue;

            String status =
                    order.optString("assignment_status", "");

            if ("OFFERED".equals(status)) {
                offered++;
            } else if (
                    "ACCEPTED".equals(status)
                            || "PICKED_UP".equals(status)
            ) {
                active++;
            } else if ("DELIVERED".equals(status)) {
                delivered++;
            }
        }

        txtCountOffered.setText(String.valueOf(offered));
        txtCountActive.setText(String.valueOf(active));
        txtCountDelivered.setText(String.valueOf(delivered));
    }

    private void renderOrders() {
        layoutOrders.removeAllViews();

        int count = 0;

        for (int i = 0; i < allOrders.length(); i++) {
            JSONObject order = allOrders.optJSONObject(i);

            if (order == null) continue;

            String status =
                    order.optString("assignment_status", "");

            boolean visible =
                    ("OFFERED".equals(selectedTab)
                            && "OFFERED".equals(status))
                            ||
                            ("ACTIVE".equals(selectedTab)
                                    && ("ACCEPTED".equals(status)
                                    || "PICKED_UP".equals(status)))
                            ||
                            ("DELIVERED".equals(selectedTab)
                                    && "DELIVERED".equals(status));

            if (visible) {
                createOrderCard(order);
                count++;
            }
        }

        if (count == 0) {
            showMessage("Không có đơn hàng trong mục này");
        }
    }

    private void createOrderCard(JSONObject order) {
        int assignmentId = order.optInt("assignment_id");
        int orderId = order.optInt("order_id");

        String status =
                order.optString("assignment_status", "");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        GradientDrawable cardBackground =
                new GradientDrawable();

        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(dp(16));

        card.setBackground(cardBackground);
        card.setElevation(dp(3));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(16);

        layoutOrders.addView(card, cardParams);

        addText(
                card,
                "Đơn hàng #" + orderId,
                19,
                DARK,
                true
        );

        addText(
                card,
                getStatusText(status),
                14,
                RED,
                true
        );

        addText(
                card,
                "Chi nhánh: " + order.optString("branch_name", ""),
                14,
                DARK,
                false
        );

        addText(
                card,
                "Khách hàng: " + order.optString("receiver_name", ""),
                14,
                DARK,
                false
        );

        addText(
                card,
                "SĐT: " + order.optString("receiver_phone", ""),
                14,
                GRAY,
                false
        );

        addText(
                card,
                "Địa chỉ giao: "
                        + order.optString("delivery_address", ""),
                14,
                GRAY,
                false
        );

        double amount =
                order.optDouble("total_amount", 0);

        String formattedAmount =
                NumberFormat.getNumberInstance(
                        new Locale("vi", "VN")
                ).format(amount);

        addText(
                card,
                "Tổng tiền: " + formattedAmount + "đ",
                16,
                RED,
                true
        );

        // XEM CHI TIẾT ĐƠN HÀNG
        Button btnDetails = new Button(this);
        btnDetails.setText("Xem chi tiết");
        btnDetails.setAllCaps(false);

        LinearLayout.LayoutParams detailParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48)
                );

        detailParams.topMargin = dp(8);
        card.addView(btnDetails, detailParams);

        btnDetails.setOnClickListener(v ->
                showOrderDetails(order)
        );

        if ("OFFERED".equals(status)) {
            addActionButton(
                    card,
                    "Nhận đơn",
                    assignmentId,
                    "accept_order.php",
                    true
            );

            addActionButton(
                    card,
                    "Từ chối",
                    assignmentId,
                    "reject_order.php",
                    false
            );
        }

        if ("ACCEPTED".equals(status)) {
            addActionButton(
                    card,
                    "Đã lấy món",
                    assignmentId,
                    "pickup_order.php",
                    true
            );
        }

        if ("PICKED_UP".equals(status)) {
            addActionButton(
                    card,
                    "Giao thành công",
                    assignmentId,
                    "complete_order.php",
                    true
            );
        }
    }

    // XEM CHI TIẾT ĐƠN HÀNG
    private void showOrderDetails(JSONObject order) {
        String total =
                NumberFormat.getNumberInstance(
                        new Locale("vi", "VN")
                ).format(
                        order.optDouble("total_amount", 0)
                );

        String details =
                "Khách hàng: "
                        + order.optString("receiver_name", "")
                        + "\nSố điện thoại: "
                        + order.optString("receiver_phone", "")
                        + "\nĐịa chỉ giao: "
                        + order.optString("delivery_address", "")
                        + "\n\nChi nhánh: "
                        + order.optString("branch_name", "")
                        + "\nĐịa chỉ lấy món: "
                        + order.optString("branch_address", "")
                        + "\n\nPhương thức thanh toán: "
                        + order.optString("payment_method", "")
                        + "\nTrạng thái thanh toán: "
                        + order.optString(
                        "payment_status",
                        "Chưa có thông tin"
                )
                        + "\nTổng tiền: "
                        + total + "đ"
                        + "\n\n"
                        + getStatusText(
                        order.optString(
                                "assignment_status",
                                ""
                        )
                );

        txtDetailTitle.setText(
                "Chi tiết đơn hàng #"
                        + order.optInt("order_id")
        );

        txtDetailContent.setText(details);

        layoutOrderDetails.setVisibility(View.VISIBLE);

        layoutOrderDetails.post(() -> {
            int scrollY = layoutOrderDetails.getTop();

            ordersScrollView.smoothScrollTo(0, scrollY);
        });
    }

    private String getStatusText(String status) {
        switch (status) {
            case "OFFERED":
                return "Trạng thái: Chờ nhận đơn";

            case "ACCEPTED":
                return "Trạng thái: Đã nhận đơn";

            case "REJECTED":
                return "Trạng thái: Đã từ chối";

            case "PICKED_UP":
                return "Trạng thái: Đang giao hàng";

            case "DELIVERED":
                return "Trạng thái: Giao thành công";

            case "CANCELLED":
                return "Trạng thái: Đã hủy";

            default:
                return "Trạng thái: " + status;
        }
    }

    private void addText(
            LinearLayout parent,
            String content,
            int textSize,
            int textColor,
            boolean bold
    ) {
        TextView textView = new TextView(this);

        textView.setText(content);
        textView.setTextSize(textSize);
        textView.setTextColor(textColor);

        if (bold) {
            textView.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }

        textView.setPadding(
                0,
                dp(5),
                0,
                dp(5)
        );

        parent.addView(textView);
    }

    private void addActionButton(
            LinearLayout parent,
            String title,
            int assignmentId,
            String endpoint,
            boolean primary
    ) {
        Button button = new Button(this);

        button.setText(title);
        button.setTextSize(14);
        button.setAllCaps(false);

        GradientDrawable background =
                new GradientDrawable();

        background.setCornerRadius(dp(10));

        if (primary) {
            background.setColor(RED);
            button.setTextColor(Color.WHITE);
        } else {
            background.setColor(Color.WHITE);
            background.setStroke(dp(1), RED);
            button.setTextColor(RED);
        }

        button.setBackgroundTintList(null);
        button.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48)
                );

        params.topMargin = dp(8);

        parent.addView(button, params);

        button.setOnClickListener(v -> {
            if ("reject_order.php".equals(endpoint)) {
                new AlertDialog.Builder(this)
                        .setTitle("Từ chối đơn hàng")
                        .setMessage(
                                "Bạn có chắc chắn muốn từ chối đơn này?"
                        )
                        .setNegativeButton("Không", null)
                        .setPositiveButton(
                                "Từ chối",
                                (dialog, which) -> {
                                    button.setEnabled(false);

                                    updateOrder(
                                            assignmentId,
                                            endpoint,
                                            button
                                    );
                                }
                        )
                        .show();
            } else {
                button.setEnabled(false);

                updateOrder(
                        assignmentId,
                        endpoint,
                        button
                );
            }
        });
    }

    private void updateOrder(
            int assignmentId,
            String endpoint,
            Button button
    ) {
        new Thread(() -> {
            try {
                String body =
                        "assignment_id=" + assignmentId
                                + "&driver_id=" + driverId;

                JSONObject response = sendRequest(
                        "POST",
                        BASE_URL + endpoint,
                        body
                );

                mainHandler.post(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    Toast.makeText(
                            this,
                            response.optString(
                                    "message",
                                    "Đã xử lý yêu cầu"
                            ),
                            Toast.LENGTH_SHORT
                    ).show();

                    if (response.optBoolean("success")) {
                        loadOrders();
                    } else {
                        button.setEnabled(true);
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    button.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Lỗi: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        }).start();
    }

    private JSONObject sendRequest(
            String method,
            String urlString,
            String body
    ) throws Exception {
        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(urlString).openConnection();

        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if ("POST".equals(method)) {
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                );

                try (OutputStream output =
                             connection.getOutputStream()) {
                    output.write(
                            body.getBytes(StandardCharsets.UTF_8)
                    );
                }
            }

            int responseCode =
                    connection.getResponseCode();

            InputStream inputStream =
                    responseCode >= 400
                            ? connection.getErrorStream()
                            : connection.getInputStream();

            if (inputStream == null) {
                throw new Exception(
                        "Server không trả về dữ liệu"
                );
            }

            StringBuilder result = new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         inputStream,
                                         StandardCharsets.UTF_8
                                 )
                         )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            return new JSONObject(result.toString());

        } finally {
            connection.disconnect();
        }
    }

    private void showMessage(String message) {
        layoutOrders.removeAllViews();

        TextView textView = new TextView(this);

        textView.setText(message);
        textView.setTextSize(15);
        textView.setTextColor(GRAY);
        textView.setGravity(Gravity.CENTER);

        textView.setPadding(
                dp(12),
                dp(24),
                dp(12),
                dp(24)
        );

        layoutOrders.addView(textView);
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
        );
    }
}
