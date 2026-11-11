package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {

    private TextView btnRegister;
    private TextView btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        // Tạo tài khoản
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Welcome.this,
                    Register.class
            );

            startActivity(intent);
        });

        // Đăng nhập
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Welcome.this,
                    Login.class
            );

            startActivity(intent);
        });
    }
}