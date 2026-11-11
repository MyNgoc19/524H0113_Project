package com.example.project;

import android.content.Intent;
import android.os.Bundle;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity {

    private EditText edtSearch;

    private TextView txtSeeAllCategory;
    private TextView txtSeeAllFood;

    private LinearLayout categorySushi;
    private LinearLayout categorySashimi;
    private LinearLayout categoryMaki;
    private LinearLayout categoryRamen;
    private LinearLayout categoryDrink;

    private LinearLayout navHome;
    private LinearLayout navSearch;
    private LinearLayout navCart;
    private LinearLayout navProfile;

    // MÓN ĂN NỔI BẬT
    private RecyclerView recyclerFeaturedFood;
    private FoodAdapter foodAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ÁNH XẠ VIEW
        edtSearch = findViewById(R.id.edtSearch);
        txtSeeAllCategory = findViewById(R.id.txtSeeAllCategory);
        txtSeeAllFood = findViewById(R.id.txtSeeAllFood);
        categorySushi = findViewById(R.id.categorySushi);
        categorySashimi = findViewById(R.id.categorySashimi);
        categoryMaki = findViewById(R.id.categoryMaki);
        categoryRamen = findViewById(R.id.categoryRamen);
        categoryDrink = findViewById(R.id.categoryDrink);
        navHome = findViewById(R.id.navHome);
        navSearch = findViewById(R.id.navSearch);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile);


        // RECYCLERVIEW MÓN NỔI BẬT
        recyclerFeaturedFood = findViewById(R.id.recyclerFeaturedFood);
        recyclerFeaturedFood.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter(this);
        recyclerFeaturedFood.setAdapter(foodAdapter);


        // LOAD MÓN NỔI BẬT TỪ MYSQL
        loadFeaturedFoods();


        // XEM TẤT CẢ CATEGORY
        txtSeeAllCategory.setOnClickListener(v -> {
            Intent intent = new Intent(
                            Home.this,
                            Menu.class
                    );
            intent.putExtra("mode", "all");
            startActivity(intent);
        });


        //XEM TẤT CẢ MÓN NỔI BẬT
        txtSeeAllFood.setOnClickListener(v -> {
            Intent intent = new Intent(
                            Home.this,
                            Menu.class
                    );
            intent.putExtra("mode", "featured");
            startActivity(intent);
        });


        // SEARCH
        edtSearch.setFocusable(false);
        edtSearch.setOnClickListener(v -> {
            Intent intent = new Intent(
                            Home.this,
                            Menu.class
                    );
            intent.putExtra("mode", "search");
            startActivity(intent);
        });


        //LIST MÓN ĂN
        categorySushi.setOnClickListener(v -> openCategory("sushi"));
        categorySashimi.setOnClickListener(v -> openCategory("sashimi"));
        categoryMaki.setOnClickListener(v -> openCategory("maki"));
        categoryRamen.setOnClickListener(v -> openCategory("ramen"));
        categoryDrink.setOnClickListener(v -> openCategory("drink"));


        // BOTTOM NAVIGATION
        navHome.setOnClickListener(v ->
                Toast.makeText(
                        Home.this,
                        "Bạn đang ở Trang chủ",
                        Toast.LENGTH_SHORT
                ).show()
        );


        navSearch.setOnClickListener(v -> {
            Intent intent = new Intent(
                            Home.this,
                            Menu.class
                    );
            intent.putExtra("mode", "search");
            startActivity(intent);
        });


        navCart.setOnClickListener(v -> {
            Intent intent = new Intent(
                            Home.this,
                            Cart.class
                    );
            startActivity(intent);
        });


        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                            Home.this,
                            Profile.class
                    );
            startActivity(intent);
        });
    }


    //TẢI MÓN ĂN NỔI BẬT
    private void loadFeaturedFoods() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getFoods().enqueue(
                new Callback<FoodResponse>() {

                    @Override
                    public void onResponse(
                            Call<FoodResponse> call,
                            Response<FoodResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            List<Food> allFoods =
                                    response.body().getData();

                            // Danh sách chỉ chứa món nổi bật
                            List<Food> featuredFoods =
                                    new ArrayList<>();

                            if (allFoods != null) {
                                for (Food food : allFoods) {
                                    if (food.getIs_featured() == 1) {
                                        featuredFoods.add(food);
                                    }
                                }
                            }


                            // Đưa data vào FoodAdapter
                            foodAdapter.updateData(
                                    featuredFoods
                            );
                        } else {
                            Toast.makeText(
                                    Home.this,
                                    "Không thể tải món ăn nổi bật",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<FoodResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                Home.this,
                                "Lỗi kết nối: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // =========================================
    // OPEN CATEGORY
    // =========================================

    private void openCategory(
            String category
    ) {

        Intent intent =
                new Intent(
                        Home.this,
                        Menu.class
                );

        intent.putExtra(
                "mode",
                "category"
        );

        intent.putExtra(
                "category",
                category
        );

        startActivity(intent);
    }
}