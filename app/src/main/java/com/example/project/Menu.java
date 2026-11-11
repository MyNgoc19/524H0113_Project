package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapter.FoodAdapter;
import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.model.Food;
import com.example.project.model.FoodResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Menu extends AppCompatActivity {
    private TextView btnBack;
    private TextView btnAll;
    private TextView btnFeatured;
    private TextView btnSushi;
    private TextView btnSashimi;
    private TextView btnMaki;
    private TextView btnRamen;
    private TextView btnDrink;
    private EditText edtSearchMenu;
    private TextView txtCategoryTitle;
    private RecyclerView recyclerFoods;
    private LinearLayout navHome;
    private LinearLayout navSearch;
    private LinearLayout navCart;
    private LinearLayout navProfile;


    // RECYCLERVIEW
    private FoodAdapter foodAdapter;

    // DATA
    private final List<Food> allFoods = new ArrayList<>();
    private final List<Food> filteredFoods = new ArrayList<>();
    private int currentCategoryId = 0;
    private boolean featuredOnly = false;     // true = chỉ hiện món nổi bật

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        setupRecyclerView();
        handleIntent();
        setupCategoryButtons();
        setupSearch();
        setupNavigation();
        loadFoods();
    }


    // ÁNH XẠ VIEW
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAll = findViewById(R.id.btnAll);
        btnFeatured = findViewById(R.id.btnFeatured);
        btnSushi = findViewById(R.id.btnSushi);
        btnSashimi = findViewById(R.id.btnSashimi);
        btnMaki = findViewById(R.id.btnMaki);
        btnRamen = findViewById(R.id.btnRamen);
        btnDrink = findViewById(R.id.btnDrink);
        edtSearchMenu = findViewById(R.id.edtSearchMenu);
        txtCategoryTitle = findViewById(R.id.txtCategoryTitle);
        recyclerFoods = findViewById(R.id.recyclerFoods);
        navHome = findViewById(R.id.navHome);
        navSearch = findViewById(R.id.navSearch);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile);
    }


    // RECYCLERVIEW
    private void setupRecyclerView() {
        // Tạo Adapter
        foodAdapter = new FoodAdapter(this);

        // Hiển thị dạng danh sách dọc
        recyclerFoods.setLayoutManager(new LinearLayoutManager(this));

        // Gắn Adapter
        recyclerFoods.setAdapter(foodAdapter);
        recyclerFoods.setHasFixedSize(false);
    }


    // NHẬN INTENT TỪ HOME
    private void handleIntent() {
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        String category = intent.getStringExtra("category");
        String keyword = intent.getStringExtra("keyword");

        // Nếu Home không truyền gì
        if (mode == null) {
            showAll();
            return;
        }
        switch (mode) {
            case "all":
                showAll();
                break;

            // MÓN NỔI BẬT
            case "featured":
                currentCategoryId = 0;
                featuredOnly = true;
                selectCategoryButton(btnFeatured);
                txtCategoryTitle.setText("Món nổi bật");
                break;


            // SEARCH TỪ HOME
            case "search":
                currentCategoryId = 0;
                featuredOnly = false;
                selectCategoryButton(btnAll);
                txtCategoryTitle.setText("Kết quả tìm kiếm");

                if (keyword != null) {
                    edtSearchMenu.setText(keyword);
                }
                break;


            // CATEGORY
            case "category":
                featuredOnly = false;
                handleCategory(category);
                break;

            default:
                showAll();
                break;
        }
    }


    // CATEGORY TỪ HOME
    private void handleCategory(
            String category
    ) {
        if (category == null) {
            showAll();
            return;
        }

        switch (category.toLowerCase(Locale.ROOT))
        {
            case "sushi":
                currentCategoryId = 1;
                selectCategoryButton(btnSushi);
                txtCategoryTitle.setText("Sushi");
                break;

            case "sashimi":
                currentCategoryId = 2;
                selectCategoryButton(btnSashimi);
                txtCategoryTitle.setText("Sashimi");
                break;

            case "maki":
                currentCategoryId = 3;
                selectCategoryButton(btnMaki);
                txtCategoryTitle.setText("Maki");
                break;

            case "ramen":
                currentCategoryId = 4;
                selectCategoryButton(btnRamen);
                txtCategoryTitle.setText("Ramen");
                break;

            case "drink":
                currentCategoryId = 5;
                selectCategoryButton(btnDrink);
                txtCategoryTitle.setText("Đồ uống");
                break;

            default:
                showAll();
                break;
        }
    }

    // CATEGORY BUTTONS
    private void setupCategoryButtons() {

        // TẤT CẢ
        btnAll.setOnClickListener(v -> {
            showAll();
            filterFoods();
        });


        // FEATURED
        btnFeatured.setOnClickListener(v -> {
            currentCategoryId = 0;
            featuredOnly = true;
            selectCategoryButton(btnFeatured);
            txtCategoryTitle.setText("Món nổi bật");
            filterFoods();
        });


        // SUSHI
        btnSushi.setOnClickListener(v -> {
            currentCategoryId = 1;
            featuredOnly = false;
            selectCategoryButton(btnSushi);
            txtCategoryTitle.setText("Sushi");
            filterFoods();
        });


        // SASHIMI
        btnSashimi.setOnClickListener(v -> {
            currentCategoryId = 2;
            featuredOnly = false;
            selectCategoryButton(btnSashimi);
            txtCategoryTitle.setText("Sashimi");
            filterFoods();
        });


        // MAKI
        btnMaki.setOnClickListener(v -> {
            currentCategoryId = 3;
            featuredOnly = false;
            selectCategoryButton(btnMaki);
            txtCategoryTitle.setText("Maki");
            filterFoods();
        });

        // RAMEN
        btnRamen.setOnClickListener(v -> {
            currentCategoryId = 4;
            featuredOnly = false;
            selectCategoryButton(btnRamen);
            txtCategoryTitle.setText("Ramen");
            filterFoods();
        });


        // ĐỒ UỐNG
        btnDrink.setOnClickListener(v -> {
            currentCategoryId = 5;
            featuredOnly = false;
            selectCategoryButton(btnDrink);
            txtCategoryTitle.setText("Đồ uống");
            filterFoods();
        });
    }


    // HIỂN THỊ TẤT CẢ
    private void showAll() {
        currentCategoryId = 0;
        featuredOnly = false;
        selectCategoryButton(btnAll);
        txtCategoryTitle.setText("Tất cả món ăn"
        );
    }


    // SEARCH
    private void setupSearch() {
        edtSearchMenu.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        /*
                         * Khi người dùng nhập chữ,
                         * lọc lại danh sách ngay.
                         */

                        if (!allFoods.isEmpty()) {
                            filterFoods();
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }


    // NAVIGATION
    private void setupNavigation() {
        // BACK
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // HOME
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Menu.this,
                    Home.class
            );

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });


        // SEARCH
        navSearch.setOnClickListener(v -> {
            edtSearchMenu.requestFocus();

            /*
             * Đưa con trỏ vào cuối text.
             */
            edtSearchMenu.setSelection(
                    edtSearchMenu
                            .getText()
                            .length()
            );
        });


        // CART
        navCart.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Menu.this,
                    Cart.class
            );

            startActivity(intent);
        });


// PROFILE
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Menu.this,
                    Profile.class
            );
            startActivity(intent);
        });
    }


    // LOAD FOOD TỪ MYSQL (Android -> Retrofit -> get_foods.php -> MySQL)
    private void loadFoods() {
        ApiService apiService = ApiClient
                .getClient()
                .create(
                        ApiService.class
                );

        Call<FoodResponse> call = apiService.getFoods();

        call.enqueue(
                new Callback<FoodResponse>() {

                    // SERVER TRẢ RESPONSE
                    @Override
                    public void onResponse(
                            Call<FoodResponse> call,
                            Response<FoodResponse> response
                    ) {

                        // HTTP không thành công
                        if (!response.isSuccessful()) {
                            Toast.makeText(
                                    Menu.this,
                                    "Lỗi server: " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        // Lấy JSON đã convert thành Java object
                        FoodResponse foodResponse = response.body();

                        // Không có body
                        if (foodResponse == null) {
                            Toast.makeText(
                                    Menu.this,
                                    "Server không trả về dữ liệu",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }


                        // PHP trả success = false
                        if (!foodResponse.isSuccess()) {
                            Toast.makeText(
                                    Menu.this,
                                    "Không thể tải thực đơn",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }


                        // Không có mảng data
                        if (foodResponse.getData() == null) {
                            Toast.makeText(
                                    Menu.this,
                                    "Thực đơn hiện đang trống",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }


                        // DATA THẬT TỪ MYSQL
                        allFoods.clear();
                        allFoods.addAll(foodResponse.getData());
                        filterFoods(); //Sau khi lấy được database -> thì lọc và đưa lên RecyclerView.
                    }


                    // KHÔNG KẾT NỐI ĐƯỢC SERVER
                    @Override
                    public void onFailure(
                            Call<FoodResponse> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                Menu.this,
                                "Không kết nối được server" + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // FILTER
    private void filterFoods() {
        filteredFoods.clear();
        // KEYWORD
        String keyword = edtSearchMenu
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);


        // DUYỆT TOÀN BỘ FOOD TỪ DATABASE
        for (Food food : allFoods) {

            // CATEGORY
            boolean categoryMatch = currentCategoryId == 0 || food.getCategory_id() == currentCategoryId;

            // FEATURED
            boolean featuredMatch = !featuredOnly || food.getIs_featured() == 1;

            // SEARCH
            boolean searchMatch = true;

            if (!keyword.isEmpty()) {
                String foodName = "";
                String description = "";

                // FOOD NAME
                if (food.getFood_name() != null) {
                    foodName = food
                            .getFood_name()
                            .toLowerCase(Locale.ROOT);
                }

                // DESCRIPTION
                if (food.getDescription() != null) {
                    description = food
                            .getDescription()
                            .toLowerCase(Locale.ROOT);
                }

                searchMatch = foodName.contains(keyword) || description.contains(keyword);
            }


            // THỎA TẤT CẢ ĐIỀU KIỆN
            if (categoryMatch && featuredMatch && searchMatch) {
                filteredFoods.add(food);
            }
        }

        // ĐƯA DANH SÁCH LÊN RECYCLERVIEW
        foodAdapter.updateData(
                filteredFoods
        );

        // UPDATE TITLE KHI SEARCH
        if (!keyword.isEmpty()) {
            txtCategoryTitle.setText(
                    "Kết quả tìm kiếm (" + filteredFoods.size() + ")"
            );
        }
    }


    // CATEGORY SELECTED
    private void selectCategoryButton(
            TextView selectedButton
    ) {


        // Reset tất cả
        resetButton(btnAll);
        resetButton(btnFeatured);
        resetButton(btnSushi);
        resetButton(btnSashimi);
        resetButton(btnMaki);
        resetButton(btnRamen);
        resetButton(btnDrink);


        // Selected
        selectedButton.setBackgroundResource(R.drawable.bg_category_selected);
        selectedButton.setTextColor(Color.WHITE);
        selectedButton.setTypeface(null, Typeface.BOLD);
    }


    // CATEGORY NORMAL
    private void resetButton(TextView button) {
        button.setBackgroundResource(R.drawable.bg_category_normal);
        button.setTextColor(Color.parseColor("#333333"));
        button.setTypeface(null, Typeface.NORMAL);
    }
}