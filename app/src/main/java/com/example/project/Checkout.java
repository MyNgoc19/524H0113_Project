package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.model.OrderResponse;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Checkout extends AppCompatActivity {

    // =====================================================
    // VIEW
    // =====================================================

    private TextView btnBack;

    private EditText edtReceiverName;
    private EditText edtReceiverPhone;
    private EditText edtAddress;

    private TextView txtDistance;
    private TextView txtShippingFee;

    private RadioGroup radioPayment;

    private TextView txtSubtotal;
    private TextView txtSummaryShipping;
    private TextView txtTotal;

    private Button btnPlaceOrder;


    // =====================================================
    // DATA
    // =====================================================

    private double subtotal = 0;
    private double shippingFee = 0;
    private double distanceKm = 0;

    private double customerLatitude = 0;
    private double customerLongitude = 0;

    private boolean hasValidAddress = false;


    // =====================================================
    // VỊ TRÍ QUÁN SUSHIZEN
    // =====================================================

    private static final double RESTAURANT_LATITUDE = 10.7327;
    private static final double RESTAURANT_LONGITUDE = 106.6997;


    // =====================================================
    // SHARED PREFERENCES
    // =====================================================

    private SharedPreferences preferences;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_checkout
        );


        // =================================================
        // VIEW
        // =================================================

        initViews();


        // =================================================
        // SHARED PREFERENCES
        // =================================================

        preferences =
                getSharedPreferences(
                        "SushiZenAccount",
                        MODE_PRIVATE
                );


        // =================================================
        // LOAD USER
        // =================================================

        loadUserInformation();


        // =================================================
        // NHẬN SUBTOTAL TỪ CART
        // =================================================

        subtotal =
                getIntent().getDoubleExtra(
                        "subtotal",
                        0
                );


        // =================================================
        // KIỂM TRA CART
        // =================================================

        if (subtotal <= 0) {

            Toast.makeText(
                    this,
                    "Không có dữ liệu giỏ hàng",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // =================================================
        // UPDATE
        // =================================================

        updateCheckout();


        // =================================================
        // ADDRESS
        // =================================================

        setupAddressInput();


        // =================================================
        // BACK
        // =================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =================================================
        // PLACE ORDER
        // =================================================

        btnPlaceOrder.setOnClickListener(
                v -> checkOrder()
        );
    }


    // =====================================================
    // INIT VIEW
    // =====================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        edtReceiverName =
                findViewById(
                        R.id.edtReceiverName
                );


        edtReceiverPhone =
                findViewById(
                        R.id.edtReceiverPhone
                );


        edtAddress =
                findViewById(
                        R.id.edtAddress
                );


        txtDistance =
                findViewById(
                        R.id.txtDistance
                );


        txtShippingFee =
                findViewById(
                        R.id.txtShippingFee
                );


        radioPayment =
                findViewById(
                        R.id.radioPayment
                );


        txtSubtotal =
                findViewById(
                        R.id.txtSubtotal
                );


        txtSummaryShipping =
                findViewById(
                        R.id.txtSummaryShipping
                );


        txtTotal =
                findViewById(
                        R.id.txtTotal
                );


        btnPlaceOrder =
                findViewById(
                        R.id.btnPlaceOrder
                );
    }


    // =====================================================
    // LOAD USER
    // =====================================================

    private void loadUserInformation() {

        String fullName =
                preferences.getString(
                        "fullName",
                        ""
                );


        String phone =
                preferences.getString(
                        "phone",
                        ""
                );


        edtReceiverName.setText(
                fullName
        );


        edtReceiverPhone.setText(
                phone
        );
    }


    // =====================================================
    // SETUP ADDRESS
    // =====================================================

    private void setupAddressInput() {

        edtAddress.setFocusableInTouchMode(
                true
        );

        edtAddress.setClickable(
                true
        );

        edtAddress.setSingleLine(
                true
        );


        edtAddress.setHint(
                "Nhập địa chỉ giao hàng"
        );


        edtAddress.setImeOptions(
                EditorInfo.IME_ACTION_SEARCH
        );


        // =================================================
        // SEARCH TRÊN KEYBOARD
        // =================================================

        edtAddress.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId
                            == EditorInfo.IME_ACTION_SEARCH

                            || actionId
                            == EditorInfo.IME_ACTION_DONE) {

                        searchAddress();

                        return true;
                    }

                    return false;
                }
        );


        // =================================================
        // USER THAY ĐỔI ADDRESS
        // =================================================

        edtAddress.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        hasValidAddress =
                                false;

                    } else {

                        String address =
                                edtAddress
                                        .getText()
                                        .toString()
                                        .trim();


                        if (!address.isEmpty()
                                && !hasValidAddress) {

                            searchAddress();
                        }
                    }
                }
        );
    }


    // =====================================================
    // SEARCH ADDRESS
    // =====================================================

    private void searchAddress() {

        String addressText =
                edtAddress
                        .getText()
                        .toString()
                        .trim();


        if (addressText.isEmpty()) {

            edtAddress.setError(
                    "Vui lòng nhập địa chỉ"
            );

            return;
        }


        String searchText =
                addressText
                        + ", Việt Nam";


        Geocoder geocoder =
                new Geocoder(
                        this,
                        new Locale(
                                "vi",
                                "VN"
                        )
                );


        try {

            List<Address> addresses =
                    geocoder.getFromLocationName(
                            searchText,
                            1
                    );


            // =================================================
            // KHÔNG TÌM THẤY
            // =================================================

            if (addresses == null
                    || addresses.isEmpty()) {

                hasValidAddress =
                        false;

                shippingFee =
                        0;

                distanceKm =
                        0;


                updateCheckout();


                Toast.makeText(
                        this,
                        "Không tìm thấy địa chỉ. Hãy nhập chi tiết hơn.",
                        Toast.LENGTH_LONG
                ).show();


                return;
            }


            // =================================================
            // ADDRESS
            // =================================================

            Address result =
                    addresses.get(0);


            customerLatitude =
                    result.getLatitude();


            customerLongitude =
                    result.getLongitude();


            hasValidAddress =
                    true;


            // =================================================
            // FULL ADDRESS
            // =================================================

            if (result.getMaxAddressLineIndex()
                    >= 0) {

                String fullAddress =
                        result.getAddressLine(
                                0
                        );


                if (fullAddress != null
                        && !fullAddress
                        .trim()
                        .isEmpty()) {

                    edtAddress.setText(
                            fullAddress
                    );


                    edtAddress.setSelection(
                            edtAddress.length()
                    );
                }
            }


            // =================================================
            // DISTANCE
            // =================================================

            calculateDistance();


            // =================================================
            // SHIPPING
            // =================================================

            shippingFee =
                    calculateShippingFee(
                            distanceKm
                    );


            updateCheckout();


            Toast.makeText(
                    this,
                    "Đã xác nhận địa chỉ giao hàng",
                    Toast.LENGTH_SHORT
            ).show();


        } catch (IOException e) {

            hasValidAddress =
                    false;

            shippingFee =
                    0;

            distanceKm =
                    0;


            updateCheckout();


            Toast.makeText(
                    this,
                    "Không thể tìm địa chỉ. Kiểm tra kết nối mạng.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =====================================================
    // CALCULATE DISTANCE
    // =====================================================

    private void calculateDistance() {

        float[] results =
                new float[1];


        Location.distanceBetween(

                RESTAURANT_LATITUDE,
                RESTAURANT_LONGITUDE,

                customerLatitude,
                customerLongitude,

                results
        );


        distanceKm =
                results[0]
                        / 1000.0;
    }


    // =====================================================
    // SHIPPING FEE
    // =====================================================

    private double calculateShippingFee(
            double distance
    ) {

        // 0 - 2 KM
        if (distance <= 2) {

            return 15000;
        }


        // 2 - 5 KM
        else if (distance <= 5) {

            return 20000;
        }


        // 5 - 10 KM
        else if (distance <= 10) {

            return 30000;
        }


        // > 10 KM
        else {

            return 40000;
        }
    }


    // =====================================================
    // UPDATE CHECKOUT
    // =====================================================

    private void updateCheckout() {

        // =================================================
        // SUBTOTAL
        // =================================================

        txtSubtotal.setText(
                formatMoney(
                        subtotal
                )
        );


        // =================================================
        // ADDRESS CHƯA VALID
        // =================================================

        if (!hasValidAddress) {

            txtDistance.setText(
                    "-- km"
            );


            txtShippingFee.setText(
                    "Chưa tính"
            );


            txtSummaryShipping.setText(
                    "Chưa tính"
            );


            txtTotal.setText(
                    formatMoney(
                            subtotal
                    )
            );


            return;
        }


        // =================================================
        // DISTANCE
        // =================================================

        txtDistance.setText(

                String.format(
                        Locale.US,
                        "%.1f km",
                        distanceKm
                )
        );


        // =================================================
        // SHIPPING
        // =================================================

        txtShippingFee.setText(
                formatMoney(
                        shippingFee
                )
        );


        txtSummaryShipping.setText(
                formatMoney(
                        shippingFee
                )
        );


        // =================================================
        // TOTAL
        // =================================================

        double total =
                subtotal
                        + shippingFee;


        txtTotal.setText(
                formatMoney(
                        total
                )
        );
    }


    // =====================================================
    // CHECK ORDER
    // =====================================================

    private void checkOrder() {

        String name =
                edtReceiverName
                        .getText()
                        .toString()
                        .trim();


        String phone =
                edtReceiverPhone
                        .getText()
                        .toString()
                        .trim();


        String address =
                edtAddress
                        .getText()
                        .toString()
                        .trim();


        // =================================================
        // NAME
        // =================================================

        if (name.isEmpty()) {

            edtReceiverName.setError(
                    "Vui lòng nhập họ tên"
            );

            edtReceiverName.requestFocus();

            return;
        }


        // =================================================
        // PHONE
        // =================================================

        if (phone.isEmpty()) {

            edtReceiverPhone.setError(
                    "Vui lòng nhập số điện thoại"
            );

            edtReceiverPhone.requestFocus();

            return;
        }


        if (!phone.matches("\\d{10}")) {

            edtReceiverPhone.setError(
                    "Số điện thoại phải có đúng 10 chữ số"
            );

            edtReceiverPhone.requestFocus();

            return;
        }


        // =================================================
        // ADDRESS
        // =================================================

        if (address.isEmpty()) {

            edtAddress.setError(
                    "Vui lòng nhập địa chỉ giao hàng"
            );

            edtAddress.requestFocus();

            return;
        }


        if (!hasValidAddress) {

            Toast.makeText(
                    this,
                    "Đang kiểm tra địa chỉ giao hàng...",
                    Toast.LENGTH_SHORT
            ).show();


            searchAddress();

            return;
        }


        // =================================================
        // PAYMENT
        // =================================================

        int paymentId =
                radioPayment
                        .getCheckedRadioButtonId();


        if (paymentId == -1) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn phương thức thanh toán",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =================================================
        // COD
        // =================================================

        if (paymentId
                == R.id.radioCOD) {

            placeCODOrder();
        }


        // =================================================
        // BANK
        // =================================================

        else if (paymentId
                == R.id.radioBank) {

            placeBankOrder();
        }
    }


    // =====================================================
    // COD
    // =====================================================

    private void placeCODOrder() {

        createOrder(
                "COD"
        );
    }


    // =====================================================
    // BANK
    // =====================================================

    private void placeBankOrder() {

        createOrder(
                "BANK"
        );
    }


    // =====================================================
    // CREATE ORDER
    // =====================================================

    private void createOrder(
            String paymentMethod
    ) {

        // =================================================
        // USER ID
        // =================================================

        int userId =
                preferences.getInt(
                        "user_id",
                        0
                );


        if (userId <= 0) {

            Toast.makeText(
                    this,
                    "Không tìm thấy tài khoản đăng nhập",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =================================================
        // RECEIVER
        // =================================================

        String receiverName =
                edtReceiverName
                        .getText()
                        .toString()
                        .trim();


        String receiverPhone =
                edtReceiverPhone
                        .getText()
                        .toString()
                        .trim();


        String deliveryAddress =
                edtAddress
                        .getText()
                        .toString()
                        .trim();


        // =================================================
        // BUTTON LOADING
        // =================================================

        btnPlaceOrder.setEnabled(
                false
        );


        btnPlaceOrder.setText(
                "ĐANG ĐẶT HÀNG..."
        );


        // =================================================
        // API SERVICE
        // =================================================

        ApiService apiService =
                ApiClient
                        .getClient()
                        .create(
                                ApiService.class
                        );


        // =================================================
        // CREATE CALL
        // =================================================

        Call<OrderResponse> call =
                apiService.createOrder(

                        userId,

                        receiverName,

                        receiverPhone,

                        deliveryAddress,

                        shippingFee,

                        paymentMethod
                );


        // =================================================
        // CALL API
        // =================================================

        call.enqueue(

                new Callback<OrderResponse>() {

                    // =========================================
                    // RESPONSE
                    // =========================================

                    @Override
                    public void onResponse(

                            Call<OrderResponse> call,

                            Response<OrderResponse> response
                    ) {

                        resetPlaceOrderButton();


                        // =====================================
                        // HTTP ERROR
                        // =====================================

                        if (!response.isSuccessful()) {

                            Toast.makeText(
                                    Checkout.this,
                                    "Lỗi server: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }


                        // =====================================
                        // BODY
                        // =====================================

                        OrderResponse result =
                                response.body();


                        if (result == null) {

                            Toast.makeText(
                                    Checkout.this,
                                    "Server không trả dữ liệu",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }


                        // =====================================
                        // SUCCESS
                        // =====================================

                        if (result.isSuccess()) {

                            Toast.makeText(
                                    Checkout.this,
                                    result.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();


                            openOrderDetail(

                                    result.getOrder_id(),

                                    paymentMethod,

                                    result.getSubtotal(),

                                    result.getShipping_fee()
                            );
                        }


                        // =====================================
                        // FAILED
                        // =====================================

                        else {

                            Toast.makeText(
                                    Checkout.this,
                                    result.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }


                    // =========================================
                    // NETWORK FAILURE
                    // =========================================

                    @Override
                    public void onFailure(

                            Call<OrderResponse> call,

                            Throwable t
                    ) {

                        resetPlaceOrderButton();


                        Toast.makeText(
                                Checkout.this,
                                "Không kết nối được server: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // =====================================================
    // RESET BUTTON
    // =====================================================

    private void resetPlaceOrderButton() {

        btnPlaceOrder.setEnabled(
                true
        );


        btnPlaceOrder.setText(
                "ĐẶT HÀNG"
        );
    }


    // =====================================================
    // OPEN ORDER DETAIL
    // =====================================================

    private void openOrderDetail(

            int orderId,

            String paymentMethod,

            double serverSubtotal,

            double serverShippingFee
    ) {

        Intent intent =
                new Intent(
                        Checkout.this,
                        OrderDetail.class
                );


        // =================================================
        // ORDER ID
        // =================================================

        intent.putExtra(
                "order_id",
                orderId
        );


        // =================================================
        // RECEIVER NAME
        // =================================================

        intent.putExtra(

                "receiver_name",

                edtReceiverName
                        .getText()
                        .toString()
                        .trim()
        );


        // =================================================
        // PHONE
        // =================================================

        intent.putExtra(

                "receiver_phone",

                edtReceiverPhone
                        .getText()
                        .toString()
                        .trim()
        );


        // =================================================
        // ADDRESS
        // =================================================

        intent.putExtra(

                "receiver_address",

                edtAddress
                        .getText()
                        .toString()
                        .trim()
        );


        // =================================================
        // PAYMENT METHOD
        // =================================================

        intent.putExtra(
                "payment_method",
                paymentMethod
        );


        // =================================================
        // SUBTOTAL
        // =================================================

        intent.putExtra(
                "subtotal",
                serverSubtotal
        );


        // =================================================
        // SHIPPING
        // =================================================

        intent.putExtra(
                "shipping_fee",
                serverShippingFee
        );


        // =================================================
        // DISTANCE
        // =================================================

        intent.putExtra(
                "distance",
                distanceKm
        );


        // =================================================
        // LATITUDE
        // =================================================

        intent.putExtra(
                "latitude",
                customerLatitude
        );


        // =================================================
        // LONGITUDE
        // =================================================

        intent.putExtra(
                "longitude",
                customerLongitude
        );


        // =================================================
        // OPEN
        // =================================================

        startActivity(
                intent
        );


        finish();
    }


    // =====================================================
    // FORMAT MONEY
    // =====================================================

    private String formatMoney(
            double amount
    ) {

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
}