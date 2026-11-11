
package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderTrackingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        int orderId = getIntent().getIntExtra("order_id", -1);

        TextView txtOrderId = findViewById(R.id.txtTrackingOrderId);
        Button btnChat = findViewById(R.id.btnTrackingChat);

        txtOrderId.setText("Đơn hàng #" + orderId);

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(
                    OrderTrackingActivity.this,
                    ChatActivity.class
            );
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });
    }
}
