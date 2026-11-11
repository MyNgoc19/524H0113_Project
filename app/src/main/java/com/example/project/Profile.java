
package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.model.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends AppCompatActivity {

    private TextView txtProfileName;

    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPhone;

    private EditText edtCurrentPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmPassword;

    private Button btnSave;
    private Button btnLogout;

    private LinearLayout navHome;
    private LinearLayout navSearch;
    private LinearLayout navCart;
    private LinearLayout navProfile;

    // SHARED PREFERENCES
    private SharedPreferences preferences;

    private boolean isDriver = false;
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        preferences = getSharedPreferences(
                "SushiZenAccount",
                MODE_PRIVATE
        );

        // KIỂM TRA LOGIN
        boolean isLoggedIn = preferences.getBoolean(
                "isLoggedIn",
                false
        );

        int userId = preferences.getInt(
                "user_id",
                -1
        );

        if (!isLoggedIn || userId <= 0) {
            Toast.makeText(
                    this,
                    "Vui lòng đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();

            goToLogin();
            return;
        }

        String role = preferences.getString(
                "role",
                "CUSTOMER"
        );

        isDriver = "DRIVER".equalsIgnoreCase(role);

        initViews();
        loadUserInformation();
        setupDriverNavigation();
        setupEvents();
    }

    // ÁNH XẠ VIEW
    private void initViews() {

        txtProfileName = findViewById(R.id.txtProfileName);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);

        edtCurrentPassword =
                findViewById(R.id.edtCurrentPassword);

        edtNewPassword =
                findViewById(R.id.edtNewPassword);

        edtConfirmPassword =
                findViewById(R.id.edtConfirmPassword);

        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);

        navHome = findViewById(R.id.navHome);
        navSearch = findViewById(R.id.navSearch);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile);
    }

    // LOAD USER INFORMATION
    private void loadUserInformation() {

        String fullName = preferences.getString(
                "fullName",
                ""
        );

        String email = preferences.getString(
                "email",
                ""
        );

        String phone = preferences.getString(
                "phone",
                ""
        );

        // HEADER
        txtProfileName.setText(fullName);

        // FORM
        edtFullName.setText(fullName);
        edtEmail.setText(email);
        edtPhone.setText(phone);
    }

    // ĐIỀU CHỈNH THANH ĐIỀU HƯỚNG CHO TÀI XẾ
    private void setupDriverNavigation() {

        if (!isDriver) {
            navHome.setVisibility(View.VISIBLE);
            navSearch.setVisibility(View.VISIBLE);
            navCart.setVisibility(View.VISIBLE);
            navProfile.setVisibility(View.VISIBLE);
            return;
        }

        // ẨN MỤC TÌM KIẾM
        navSearch.setVisibility(View.GONE);

        // ĐỔI GIỎ HÀNG THÀNH ĐƠN HÀNG
        // Tài xế không cần mục Đơn hàng riêng
        navCart.setVisibility(View.GONE);

        navHome.setVisibility(View.VISIBLE);
        navProfile.setVisibility(View.VISIBLE);
    }

    // TÌM TEXTVIEW TRONG THANH ĐIỀU HƯỚNG
    private TextView findFirstTextView(View view) {

        if (view instanceof TextView) {
            return (TextView) view;
        }

        if (view instanceof android.view.ViewGroup) {

            android.view.ViewGroup group =
                    (android.view.ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {

                TextView result = findFirstTextView(
                        group.getChildAt(i)
                );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    // TÌM IMAGEVIEW TRONG THANH ĐIỀU HƯỚNG
    private android.widget.ImageView findFirstImageView(View view) {

        if (view instanceof android.widget.ImageView) {
            return (android.widget.ImageView) view;
        }

        if (view instanceof android.view.ViewGroup) {

            android.view.ViewGroup group =
                    (android.view.ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {

                android.widget.ImageView result =
                        findFirstImageView(
                                group.getChildAt(i)
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    // MỞ MÀN HÌNH ĐƠN HÀNG TÀI XẾ
    private void openDriverOrders() {

        int driverId = preferences.getInt(
                "driver_id",
                -1
        );

        if (driverId <= 0) {
            Toast.makeText(
                    this,
                    "Không tìm thấy thông tin tài xế",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent intent = new Intent(
                Profile.this,
                DriverOrdersActivity.class
        );

        intent.putExtra("driver_id", driverId);

        startActivity(intent);
        finish();
    }

    // EVENTS
    private void setupEvents() {

        // SAVE
        btnSave.setOnClickListener(v -> {
            saveInformation();
        });

        // LOGOUT
        btnLogout.setOnClickListener(v -> {
            logout();
        });

        // HOME
        navHome.setOnClickListener(v -> {

            if (isDriver) {
                openDriverOrders();
                return;
            }

            Intent intent = new Intent(
                    Profile.this,
                    Home.class
            );

            startActivity(intent);
            finish();
        });

        // SEARCH
        navSearch.setOnClickListener(v -> {

            if (isDriver) {
                return;
            }

            Intent intent = new Intent(
                    Profile.this,
                    Menu.class
            );

            intent.putExtra("mode", "search");

            startActivity(intent);
        });

        // CART
        navCart.setOnClickListener(v -> {

            if (isDriver) {
                return;
            }

            Intent intent = new Intent(
                    Profile.this,
                    Cart.class
            );

            startActivity(intent);
        });

        // PROFILE
        navProfile.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "Bạn đang ở trang Tài khoản",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // SAVE INFORMATION
    private void saveInformation() {

        if (isSaving) {
            return;
        }

        // LẤY DATA FORM
        String fullName = edtFullName
                .getText().toString().trim();

        String email = edtEmail
                .getText().toString().trim();

        String phone = edtPhone
                .getText().toString().trim();

        String currentPassword = edtCurrentPassword
                .getText().toString();

        String newPassword = edtNewPassword
                .getText().toString();

        String confirmPassword = edtConfirmPassword
                .getText().toString();

        // VALIDATE
        if (fullName.isEmpty()) {
            edtFullName.setError("Vui lòng nhập họ tên");
            edtFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            edtPhone.requestFocus();
            return;
        }

        if (!phone.matches("\\d{10}")) {
            edtPhone.setError(
                    "Số điện thoại phải có đúng 10 chữ số"
            );
            edtPhone.requestFocus();
            return;
        }

        boolean wantsChangePassword =
                !currentPassword.isEmpty()
                        || !newPassword.isEmpty()
                        || !confirmPassword.isEmpty();

        if (wantsChangePassword) {

            if (currentPassword.isEmpty()) {
                edtCurrentPassword.setError(
                        "Vui lòng nhập mật khẩu hiện tại"
                );
                edtCurrentPassword.requestFocus();
                return;
            }

            // Không giới hạn mật khẩu tối thiểu 8 ký tự
            if (newPassword.isEmpty()) {
                edtNewPassword.setError(
                        "Vui lòng nhập mật khẩu mới"
                );
                edtNewPassword.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                edtConfirmPassword.setError(
                        "Vui lòng xác nhận mật khẩu mới"
                );
                edtConfirmPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                edtConfirmPassword.setError(
                        "Mật khẩu xác nhận không khớp"
                );
                edtConfirmPassword.requestFocus();
                return;
            }

            if (newPassword.equals(currentPassword)) {
                edtNewPassword.setError(
                        "Mật khẩu mới phải khác mật khẩu hiện tại"
                );
                edtNewPassword.requestFocus();
                return;
            }
        }

        // LẤY USER ID
        int userId = preferences.getInt(
                "user_id",
                -1
        );

        if (userId <= 0) {
            Toast.makeText(
                    this,
                    "Không tìm thấy tài khoản",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String savedFullName = preferences.getString(
                "fullName", ""
        );

        String savedEmail = preferences.getString(
                "email", ""
        );

        String savedPhone = preferences.getString(
                "phone", ""
        );

        boolean profileChanged =
                !fullName.equals(savedFullName)
                        || !email.equals(savedEmail)
                        || !phone.equals(savedPhone);

        if (!profileChanged && !wantsChangePassword) {
            Toast.makeText(
                    this,
                    "Bạn chưa thay đổi thông tin nào",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (wantsChangePassword) {

            // Phần đổi mật khẩu phải được backend xác thực.
            // Không lưu mật khẩu vào SharedPreferences.
            changePassword(currentPassword, newPassword, () -> {
                if (profileChanged) {
                    updateProfile(userId, fullName, email, phone);
                }
            });
            return;
        }

        updateProfile(userId, fullName, email, phone);
    }

    private void changePassword(
            String currentPassword,
            String newPassword,
            Runnable onSuccess
    ) {

        String token = preferences.getString("access_token", "");

        if (token.isEmpty()) {
            token = preferences.getString("token", "");
        }

        if (token.isEmpty()) {
            Toast.makeText(
                    this,
                    "Không tìm thấy token. Vui lòng đăng nhập lại",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        isSaving = true;
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        ApiService apiService = ApiClient
                .getClient()
                .create(ApiService.class);

        apiService.changePassword(
                "Bearer " + token,
                currentPassword,
                newPassword
        ).enqueue(new Callback<AuthResponse>() {

            @Override
            public void onResponse(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Response<AuthResponse> response
            ) {

                isSaving = false;
                btnSave.setEnabled(true);
                btnSave.setText("Lưu thay đổi");

                AuthResponse result = response.body();

                if (response.isSuccessful()
                        && result != null
                        && result.isSuccess()) {

                    edtCurrentPassword.setText("");
                    edtNewPassword.setText("");
                    edtConfirmPassword.setText("");

                    Toast.makeText(
                            Profile.this,
                            "Đổi mật khẩu thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    onSuccess.run();

                } else {

                    String message =
                            result != null
                                    && result.getMessage() != null
                                    ? result.getMessage()
                                    : "Đổi mật khẩu thất bại (HTTP "
                                      + response.code() + ")";

                    Toast.makeText(
                            Profile.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {

                isSaving = false;
                btnSave.setEnabled(true);
                btnSave.setText("Lưu thay đổi");

                Toast.makeText(
                        Profile.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void updateProfile(
            int userId,
            String fullName,
            String email,
            String phone
    ) {

        // DISABLE BUTTON
        // Tránh user bấm nhiều lần
        isSaving = true;
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        // CREATE API SERVICE
        ApiService apiService = ApiClient
                .getClient()
                .create(ApiService.class);

        // CALL UPDATE PROFILE API
        apiService.updateProfile(
                userId,
                fullName,
                email,
                phone
        ).enqueue(new Callback<AuthResponse>() {

            @Override
            public void onResponse(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Response<AuthResponse> response
            ) {

                // MỞ LẠI BUTTON
                isSaving = false;
                btnSave.setEnabled(true);
                btnSave.setText("Lưu thay đổi");

                // HTTP THÀNH CÔNG
                if (response.isSuccessful()
                        && response.body() != null) {

                    AuthResponse result = response.body();

                    // MYSQL UPDATE THÀNH CÔNG
                    if (result.isSuccess()) {

                        // UPDATE LOCAL
                        preferences.edit()
                                .putString("fullName", fullName)
                                .putString("email", email)
                                .putString("phone", phone)
                                .apply();

                        // UPDATE HEADER
                        txtProfileName.setText(fullName);

                        // THÔNG BÁO
                        Toast.makeText(
                                Profile.this,
                                result.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        // SERVER TRẢ success = false
                        Toast.makeText(
                                Profile.this,
                                result.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }

                } else {

                    // HTTP ERROR
                    Toast.makeText(
                            Profile.this,
                            "Lỗi HTTP: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {

                // MỞ LẠI BUTTON
                isSaving = false;
                btnSave.setEnabled(true);
                btnSave.setText("Lưu thay đổi");

                // CONNECTION ERROR
                Toast.makeText(
                        Profile.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // LOGOUT
    private void logout() {

        // XÓA THÔNG TIN ĐĂNG NHẬP
        preferences.edit()
                .clear()
                .apply();

        Toast.makeText(
                this,
                "Đăng xuất thành công",
                Toast.LENGTH_SHORT
        ).show();

        goToLogin();
    }

    // GO TO LOGIN
    private void goToLogin() {

        Intent intent = new Intent(
                Profile.this,
                Login.class
        );

        // XÓA BACK STACK
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
