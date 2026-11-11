
package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
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
import com.example.project.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private LinearLayout btnGoogle;
    private TextView txtRegister;
    private TextView txtForgotPassword;
    private TextView txtBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ÁNH XẠ
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtRegister = findViewById(R.id.txtRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtBack = findViewById(R.id.txtBack);

        // NHẬN EMAIL TỪ REGISTER
        String emailFromRegister = getIntent().getStringExtra("email");

        if (emailFromRegister != null) {
            edtEmail.setText(emailFromRegister);
        }

        // LOGIN
        btnLogin.setOnClickListener(v -> {
            login();
        });

        // REGISTER
        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Login.this,
                    Register.class
            );
            startActivity(intent);
        });

        // BACK
        txtBack.setOnClickListener(v -> {
            finish();
        });

        // QUÊN MẬT KHẨU
        txtForgotPassword.setOnClickListener(v -> {
            Toast.makeText(
                    Login.this,
                    "Chức năng quên mật khẩu sẽ làm sau",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // GOOGLE LOGIN
        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(
                    Login.this,
                    "Google Login sẽ làm sau",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // LOGIN
    private void login() {

        String email = edtEmail
                .getText()
                .toString()
                .trim();

        String password = edtPassword
                .getText()
                .toString();

        // KIỂM TRA EMAIL
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        // KIỂM TRA PASSWORD
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        // GỌI API LOGIN
        loginWithApi(email, password);
    }

    // LOGIN VỚI MYSQL THÔNG QUA API
    private void loginWithApi(
            String email,
            String password
    ) {

        // Không cho bấm Login liên tục
        btnLogin.setEnabled(false);

        // TẠO API SERVICE
        ApiService apiService =
                ApiClient
                        .getClient()
                        .create(ApiService.class);

        // GỌI login.php
        Call<AuthResponse> call =
                apiService.login(
                        email,
                        password
                );

        call.enqueue(
                new Callback<AuthResponse>() {

                    // SERVER TRẢ RESPONSE
                    @Override
                    public void onResponse(
                            @NonNull Call<AuthResponse> call,
                            @NonNull Response<AuthResponse> response
                    ) {

                        btnLogin.setEnabled(true);

                        // HTTP ERROR
                        if (!response.isSuccessful()) {
                            Toast.makeText(
                                    Login.this,
                                    "Lỗi HTTP: " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        // BODY NULL
                        if (response.body() == null) {
                            Toast.makeText(
                                    Login.this,
                                    "Server không trả dữ liệu",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        AuthResponse authResponse = response.body();

                        // LOGIN THẤT BẠI
                        if (!authResponse.isSuccess()) {

                            String message = authResponse.getMessage();

                            if (message == null || message.isEmpty()) {
                                message = "Email hoặc mật khẩu không đúng";
                            }

                            Toast.makeText(
                                    Login.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        // LẤY USER TỪ SERVER
                        User user = authResponse.getUser();

                        if (user == null) {
                            Toast.makeText(
                                    Login.this,
                                    "Không nhận được thông tin user từ server",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        // LẤY USER_ID
                        int userId = user.getUser_id();

                        if (userId <= 0) {
                            Toast.makeText(
                                    Login.this,
                                    "user_id không hợp lệ",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        String role = user.getRole();
                        int driverId = user.getDriver_id();

                        if (role == null) {
                            role = "";
                        }

                        role = role.trim().toUpperCase(
                                java.util.Locale.ROOT
                        );

                        // CHỈ HỖ TRỢ CUSTOMER VÀ DRIVER
                        if (!role.equals("CUSTOMER")
                                && !role.equals("DRIVER")) {

                            Toast.makeText(
                                    Login.this,
                                    "Tài khoản không được hỗ trợ trên ứng dụng",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        if (role.equals("DRIVER") && driverId <= 0) {
                            Toast.makeText(
                                    Login.this,
                                    "Không tìm thấy hồ sơ tài xế",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        // LƯU ĐĂNG NHẬP
                        SharedPreferences sharedPreferences =
                                getSharedPreferences(
                                        "SushiZenAccount",
                                        MODE_PRIVATE
                                );

                        String accessToken =
                                authResponse.getAccess_token();

                        if (accessToken == null
                                || accessToken.trim().isEmpty()) {

                            Toast.makeText(
                                    Login.this,
                                    "API chưa trả access_token. Kiểm tra auth/login.php",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        SharedPreferences.Editor editor =
                                sharedPreferences.edit();

                        editor.putInt("user_id", userId);
                        editor.putString("fullName", user.getFull_name());
                        editor.putString("email", user.getEmail());
                        editor.putString("phone", user.getPhone());
                        editor.putString("role", role);
                        editor.putInt("driver_id", driverId);
                        editor.putString("access_token", accessToken);
                        editor.putBoolean("isLoggedIn", true);

                        editor.apply();

                        // LOGIN THÀNH CÔNG
                        Toast.makeText(
                                Login.this,
                                "Đăng nhập thành công - user_id: " + userId,
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent;

                        if (role.equals("DRIVER")) {

                            // VÀO GIAO DIỆN TÀI XẾ
                            intent = new Intent(
                                    Login.this,
                                    DriverOrdersActivity.class
                            );

                            intent.putExtra("driver_id", driverId);

                        } else {

                            // VÀO HOME
                            intent = new Intent(
                                    Login.this,
                                    Home.class
                            );
                        }

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();
                    }

                    // LỖI KẾT NỐI / JSON
                    @Override
                    public void onFailure(
                            @NonNull Call<AuthResponse> call,
                            @NonNull Throwable t
                    ) {

                        btnLogin.setEnabled(true);

                        Toast.makeText(
                                Login.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}
