package com.example.project;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    // =====================================================
    // MAP
    // =====================================================

    private GoogleMap googleMap;
    private Marker selectedMarker;


    // =====================================================
    // VIEW
    // =====================================================

    private TextView btnBack;
    private EditText edtSearchAddress;
    private TextView txtSelectedAddress;
    private Button btnConfirmAddress;


    // =====================================================
    // DATA
    // =====================================================

    private String selectedAddress = "";

    private double selectedLatitude = 0;
    private double selectedLongitude = 0;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);


        initViews();

        setupMap();

        setupAddressSearch();


        // BACK
        btnBack.setOnClickListener(v -> finish());


        // CONFIRM
        btnConfirmAddress.setOnClickListener(v -> {

            confirmAddress();

        });
    }


    // =====================================================
    // INIT VIEW
    // =====================================================

    private void initViews() {

        btnBack =
                findViewById(R.id.btnBack);

        edtSearchAddress =
                findViewById(R.id.edtSearchAddress);

        txtSelectedAddress =
                findViewById(R.id.txtSelectedAddress);

        btnConfirmAddress =
                findViewById(R.id.btnConfirmAddress);
    }


    // =====================================================
    // SETUP MAP
    // =====================================================

    private void setupMap() {

        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getSupportFragmentManager()
                                .findFragmentById(R.id.map);


        if (mapFragment != null) {

            mapFragment.getMapAsync(this);

        } else {

            Toast.makeText(
                    this,
                    "Không tìm thấy Google Map",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =====================================================
    // MAP READY
    // =====================================================

    @Override
    public void onMapReady(@NonNull GoogleMap map) {

        googleMap = map;


        // TP.HCM
        LatLng hoChiMinhCity =
                new LatLng(
                        10.7769,
                        106.7009
                );


        googleMap.moveCamera(

                CameraUpdateFactory.newLatLngZoom(
                        hoChiMinhCity,
                        12f
                )
        );
    }


    // =====================================================
    // SETUP SEARCH
    // =====================================================

    private void setupAddressSearch() {

        // Cho phép nhập địa chỉ
        edtSearchAddress.setFocusableInTouchMode(true);
        edtSearchAddress.setClickable(true);
        edtSearchAddress.setSingleLine(true);

        edtSearchAddress.setHint(
                "Nhập địa chỉ giao hàng..."
        );


        // Hiện nút Search trên bàn phím
        edtSearchAddress.setImeOptions(
                EditorInfo.IME_ACTION_SEARCH
        );


        // Khi bấm Search
        edtSearchAddress.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId
                            == EditorInfo.IME_ACTION_SEARCH) {

                        searchAddress();

                        return true;
                    }

                    return false;
                }
        );
    }


    // =====================================================
    // SEARCH ADDRESS
    // =====================================================

    private void searchAddress() {

        String inputAddress =
                edtSearchAddress
                        .getText()
                        .toString()
                        .trim();


        if (inputAddress.isEmpty()) {

            edtSearchAddress.setError(
                    "Vui lòng nhập địa chỉ"
            );

            return;
        }


        // Thêm Việt Nam để tìm chính xác hơn
        String searchText =
                inputAddress + ", Việt Nam";


        Geocoder geocoder =
                new Geocoder(
                        this,
                        new Locale("vi", "VN")
                );


        try {

            List<Address> addresses =
                    geocoder.getFromLocationName(
                            searchText,
                            1
                    );


            // Không tìm thấy
            if (addresses == null
                    || addresses.isEmpty()) {

                Toast.makeText(
                        this,
                        "Không tìm thấy địa chỉ",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }


            Address result =
                    addresses.get(0);


            // =================================================
            // LAT / LNG
            // =================================================

            selectedLatitude =
                    result.getLatitude();

            selectedLongitude =
                    result.getLongitude();


            // =================================================
            // ADDRESS
            // =================================================

            if (result.getMaxAddressLineIndex() >= 0) {

                selectedAddress =
                        result.getAddressLine(0);

            } else {

                selectedAddress =
                        inputAddress;
            }


            // =================================================
            // UI
            // =================================================

            edtSearchAddress.setText(
                    selectedAddress
            );


            txtSelectedAddress.setText(
                    selectedAddress
            );


            // =================================================
            // MAP
            // =================================================

            LatLng location =
                    new LatLng(
                            selectedLatitude,
                            selectedLongitude
                    );


            showLocationOnMap(
                    location
            );


            Toast.makeText(
                    this,
                    "Đã tìm thấy địa chỉ",
                    Toast.LENGTH_SHORT
            ).show();


        } catch (IOException e) {

            Toast.makeText(
                    this,
                    "Không thể tìm địa chỉ. Kiểm tra Internet.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =====================================================
    // SHOW LOCATION ON MAP
    // =====================================================

    private void showLocationOnMap(
            LatLng location
    ) {

        if (googleMap == null) {

            return;
        }


        // Xóa marker cũ
        if (selectedMarker != null) {

            selectedMarker.remove();
        }


        // Tạo marker mới
        selectedMarker =
                googleMap.addMarker(

                        new MarkerOptions()
                                .position(location)
                                .title("Địa chỉ giao hàng")
                );


        // Zoom đến địa chỉ
        googleMap.animateCamera(

                CameraUpdateFactory.newLatLngZoom(
                        location,
                        17f
                )
        );
    }


    // =====================================================
    // CONFIRM
    // =====================================================

    private void confirmAddress() {

        if (selectedAddress == null
                || selectedAddress.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Vui lòng nhập và tìm địa chỉ",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (selectedLatitude == 0
                || selectedLongitude == 0) {

            Toast.makeText(
                    this,
                    "Không lấy được tọa độ địa chỉ",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =================================================
        // RETURN TO CHECKOUT
        // =================================================

        Intent resultIntent =
                new Intent();


        resultIntent.putExtra(
                "address",
                selectedAddress
        );


        resultIntent.putExtra(
                "latitude",
                selectedLatitude
        );


        resultIntent.putExtra(
                "longitude",
                selectedLongitude
        );


        setResult(
                Activity.RESULT_OK,
                resultIntent
        );


        finish();
    }
}