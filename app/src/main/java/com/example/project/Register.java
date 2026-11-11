package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.model.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Register extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPhone;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    private CheckBox chkTerms;

    private Button btnRegister;

    private LinearLayout btnGoogle;

    private TextView txtLogin;
    private TextView txtBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);


        // =====================================
        // ÁNH XẠ
        // =====================================

        edtFullName =
                findViewById(R.id.edtFullName);

        edtEmail =
                findViewById(R.id.edtEmail);

        edtPhone =
                findViewById(R.id.edtPhone);

        edtPassword =
                findViewById(R.id.edtPassword);

        edtConfirmPassword =
                findViewById(R.id.edtConfirmPassword);

        chkTerms =
                findViewById(R.id.chkTerms);

        btnRegister =
                findViewById(R.id.btnRegister);

        btnGoogle =
                findViewById(R.id.btnGoogle);

        txtLogin =
                findViewById(R.id.txtLogin);

        txtBack =
                findViewById(R.id.txtBack);


        // =====================================
        // NÚT ĐĂNG KÝ
        // =====================================

        btnRegister.setOnClickListener(v -> {

            register();

        });


        // =====================================
        // ĐÃ CÓ TÀI KHOẢN → LOGIN
        // =====================================

        txtLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            Register.this,
                            Login.class
                    );

            startActivity(intent);

            finish();

        });


        // =====================================
        // BACK → WELCOME
        // =====================================

        txtBack.setOnClickListener(v -> {

            finish();

        });


        // =====================================
        // GOOGLE - CHƯA LÀM
        // =====================================

        btnGoogle.setOnClickListener(v -> {

            Toast.makeText(
                    Register.this,
                    "Google Register sẽ làm sau",
                    Toast.LENGTH_SHORT
            ).show();

        });
    }


    // =========================================
    // REGISTER
    // =========================================

    private void register() {

        String fullName =
                edtFullName
                        .getText()
                        .toString()
                        .trim();


        String email =
                edtEmail
                        .getText()
                        .toString()
                        .trim();


        String phone =
                edtPhone
                        .getText()
                        .toString()
                        .trim();


        String password =
                edtPassword
                        .getText()
                        .toString()
                        .trim();


        String confirmPassword =
                edtConfirmPassword
                        .getText()
                        .toString()
                        .trim();


        // =====================================
        // KIỂM TRA HỌ TÊN
        // =====================================

        if (fullName.isEmpty()) {

            edtFullName.setError(
                    "Vui lòng nhập họ tên"
            );

            edtFullName.requestFocus();

            return;
        }


        // =====================================
        // KIỂM TRA EMAIL
        // =====================================

        if (email.isEmpty()) {

            edtEmail.setError(
                    "Vui lòng nhập email"
            );

            edtEmail.requestFocus();

            return;
        }


        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            edtEmail.setError(
                    "Email không hợp lệ"
            );

            edtEmail.requestFocus();

            return;
        }


        // =====================================
        // KIỂM TRA SỐ ĐIỆN THOẠI
        // =====================================

        if (phone.isEmpty()) {

            edtPhone.setError(
                    "Vui lòng nhập số điện thoại"
            );

            edtPhone.requestFocus();

            return;
        }


        if (!phone.matches("\\d{10}")) {

            edtPhone.setError(
                    "Số điện thoại phải có 10 chữ số"
            );

            edtPhone.requestFocus();

            return;
        }


        // =====================================
        // KIỂM TRA PASSWORD
        // =====================================

        if (password.isEmpty()) {

            edtPassword.setError(
                    "Vui lòng nhập mật khẩu"
            );

            edtPassword.requestFocus();

            return;
        }


        if (password.length() < 6) {

            edtPassword.setError(
                    "Mật khẩu phải có ít nhất 6 ký tự"
            );

            edtPassword.requestFocus();

            return;
        }


        // =====================================
        // KIỂM TRA CONFIRM PASSWORD
        // =====================================

        if (confirmPassword.isEmpty()) {

            edtConfirmPassword.setError(
                    "Vui lòng xác nhận mật khẩu"
            );

            edtConfirmPassword.requestFocus();

            return;
        }


        if (!password.equals(confirmPassword)) {

            edtConfirmPassword.setError(
                    "Mật khẩu xác nhận không khớp"
            );

            edtConfirmPassword.requestFocus();

            return;
        }


        // =====================================
        // KIỂM TRA ĐIỀU KHOẢN
        // =====================================

        if (!chkTerms.isChecked()) {

            Toast.makeText(
                    Register.this,
                    "Vui lòng đồng ý điều khoản",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================
        // GỌI API ĐĂNG KÝ
        // =====================================

        registerUser(
                fullName,
                email,
                phone,
                password
        );
    }


    // =========================================
    // GỌI REGISTER API
    // =========================================

    private void registerUser(
            String fullName,
            String email,
            String phone,
            String password
    ) {

        // Không cho người dùng bấm nhiều lần
        btnRegister.setEnabled(false);


        ApiService apiService =
                ApiClient
                        .getClient()
                        .create(ApiService.class);


        Call<AuthResponse> call =
                apiService.register(
                        fullName,
                        email,
                        phone,
                        password
                );


        call.enqueue(new Callback<AuthResponse>() {

            @Override
            public void onResponse(
                    Call<AuthResponse> call,
                    Response<AuthResponse> response
            ) {

                // Cho phép bấm lại
                btnRegister.setEnabled(true);


                // =====================================
                // HTTP ERROR
                // =====================================

                if (!response.isSuccessful()) {

                    Toast.makeText(
                            Register.this,
                            "Lỗi HTTP: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }


                // =====================================
                // SERVER KHÔNG TRẢ BODY
                // =====================================

                if (response.body() == null) {

                    Toast.makeText(
                            Register.this,
                            "Server không trả dữ liệu",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }


                AuthResponse authResponse =
                        response.body();


                // =====================================
                // ĐĂNG KÝ THÀNH CÔNG
                // =====================================

                if (authResponse.isSuccess()) {

                    Toast.makeText(
                            Register.this,
                            authResponse.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();


                    // =================================
                    // CHUYỂN SANG LOGIN
                    // =================================

                    Intent intent =
                            new Intent(
                                    Register.this,
                                    Login.class
                            );


                    // Điền sẵn email vừa đăng ký
                    intent.putExtra(
                            "email",
                            email
                    );


                    startActivity(intent);

                    finish();


                } else {

                    // =================================
                    // ĐĂNG KÝ THẤT BẠI
                    // =================================

                    String message =
                            authResponse.getMessage();


                    if (message == null ||
                            message.isEmpty()) {

                        message =
                                "Đăng ký thất bại";
                    }


                    Toast.makeText(
                            Register.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }


            // =========================================
            // LỖI KẾT NỐI / JSON
            // =========================================

            @Override
            public void onFailure(
                    Call<AuthResponse> call,
                    Throwable t
            ) {

                btnRegister.setEnabled(true);


                Toast.makeText(
                        Register.this,
                        "Lỗi kết nối: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}