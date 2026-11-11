
package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetail extends AppCompatActivity {

    // =====================================================
    // VIEW
    // =====================================================

    private TextView btnBack;

    private TextView txtReceiverName;
    private TextView txtReceiverPhone;
    private TextView txtReceiverAddress;

    private TextView txtPaymentMethod;

    private TextView txtSubtotal;
    private TextView txtShippingFee;
    private TextView txtTotal;

    private Button btnHome;

    private Button btnTrackOrder;
    private Button btnChatDriver;

    private int orderId;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_order_detail
        );

        orderId = getIntent().getIntExtra("order_id", -1);


        // =================================================
        // ÁNH XẠ VIEW
        // =================================================

        initViews();


        // =================================================
        // NHẬN THÔNG TIN NGƯỜI NHẬN
        // =================================================

        String name =
                getIntent().getStringExtra(
                        "receiver_name"
                );


        String phone =
                getIntent().getStringExtra(
                        "receiver_phone"
                );


        String address =
                getIntent().getStringExtra(
                        "receiver_address"
                );


        // =================================================
        // NHẬN PHƯƠNG THỨC THANH TOÁN
        // =================================================

        String payment =
                getIntent().getStringExtra(
                        "payment_method"
                );


        // =================================================
        // NHẬN TIỀN TỪ CHECKOUT
        // =================================================

        double subtotal =
                getIntent().getDoubleExtra(
                        "subtotal",
                        0
                );


        double shippingFee =
                getIntent().getDoubleExtra(
                        "shipping_fee",
                        0
                );


        // =================================================
        // TÍNH TỔNG
        // =================================================

        double total =
                subtotal + shippingFee;


        // =================================================
        // KIỂM TRA DATA NULL
        // =================================================

        if (name == null || name.trim().isEmpty()) {
            name = "Chưa có thông tin";
        }


        if (phone == null || phone.trim().isEmpty()) {
            phone = "Chưa có thông tin";
        }


        if (address == null || address.trim().isEmpty()) {
            address = "Chưa có địa chỉ";
        }


        if (payment == null || payment.trim().isEmpty()) {
            payment = "Chưa xác định";
        }


        // =================================================
        // HIỂN THỊ THÔNG TIN NGƯỜI NHẬN
        // =================================================

        txtReceiverName.setText(
                name
        );


        txtReceiverPhone.setText(
                phone
        );


        txtReceiverAddress.setText(
                address
        );


        // =================================================
        // HIỂN THỊ PHƯƠNG THỨC THANH TOÁN
        // =================================================

        txtPaymentMethod.setText(
                payment
        );


        // =================================================
        // HIỂN THỊ TIỀN
        // =================================================

        txtSubtotal.setText(
                formatMoney(subtotal)
        );


        txtShippingFee.setText(
                formatMoney(shippingFee)
        );


        txtTotal.setText(
                formatMoney(total)
        );


        // =================================================
        // BACK
        // =================================================

        btnBack.setOnClickListener(v -> {

            goHome();

        });


        // =================================================
        // THEO DÕI GIAO HÀNG
        // =================================================

        btnTrackOrder.setOnClickListener(v -> {

            if (orderId <= 0) {
                Toast.makeText(
                        OrderDetail.this,
                        "Không tìm thấy mã đơn hàng",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    OrderDetail.this,
                    OrderTrackingActivity.class
            );

            intent.putExtra("order_id", orderId);

            startActivity(intent);

        });


        // =================================================
        // CHAT VỚI TÀI XẾ
        // =================================================

        btnChatDriver.setOnClickListener(v -> {

            if (orderId <= 0) {
                Toast.makeText(
                        OrderDetail.this,
                        "Không tìm thấy mã đơn hàng",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    OrderDetail.this,
                    ChatActivity.class
            );

            intent.putExtra("order_id", orderId);

            startActivity(intent);

        });


        // =================================================
        // VỀ TRANG CHỦ
        // =================================================

        btnHome.setOnClickListener(v -> {

            goHome();

        });
    }


    // =====================================================
    // INIT VIEW
    // =====================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        txtReceiverName =
                findViewById(
                        R.id.txtReceiverName
                );


        txtReceiverPhone =
                findViewById(
                        R.id.txtReceiverPhone
                );


        txtReceiverAddress =
                findViewById(
                        R.id.txtReceiverAddress
                );


        txtPaymentMethod =
                findViewById(
                        R.id.txtPaymentMethod
                );


        txtSubtotal =
                findViewById(
                        R.id.txtSubtotal
                );


        txtShippingFee =
                findViewById(
                        R.id.txtShippingFee
                );


        txtTotal =
                findViewById(
                        R.id.txtTotal
                );


        btnHome =
                findViewById(
                        R.id.btnHome
                );


        btnTrackOrder =
                findViewById(
                        R.id.btnTrackOrder
                );


        btnChatDriver =
                findViewById(
                        R.id.btnChatDriver
                );
    }


    // =====================================================
    // VỀ HOME
    // =====================================================

    private void goHome() {

        Intent intent =
                new Intent(
                        OrderDetail.this,
                        Home.class
                );


        /*
         * Xóa các Activity phía trên Home.
         *
         * Ví dụ:
         *
         * Cart
         *   ↓
         * Checkout
         *   ↓
         * OrderDetail
         *
         * Sau khi về Home sẽ không quay ngược
         * lại màn hình đặt hàng thành công.
         */

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK
        );


        startActivity(intent);

        finish();
    }


    // =====================================================
    // FORMAT MONEY
    // =====================================================

    private String formatMoney(double amount) {

        NumberFormat formatter =
                NumberFormat.getNumberInstance(
                        new Locale(
                                "vi",
                                "VN"
                        )
                );


        return formatter.format(
                amount
        ) + "đ";
    }


    // =====================================================
    // NÚT BACK CỦA ĐIỆN THOẠI
    // =====================================================

    @Override
    public void onBackPressed() {

        goHome();
    }
}
