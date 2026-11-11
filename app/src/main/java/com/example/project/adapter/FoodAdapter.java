package com.example.project.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.model.CartResponse;
import com.example.project.model.Food;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FoodAdapter
        extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {


    // =====================================================
    // BIẾN
    // =====================================================

    private final Context context;

    private final List<Food> foodList =
            new ArrayList<>();


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FoodAdapter(Context context) {

        this.context = context;

    }


    // =====================================================
    // TẠO ITEM
    // =====================================================

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(context)
                        .inflate(
                                R.layout.item_food,
                                parent,
                                false
                        );

        return new FoodViewHolder(view);
    }


    // =====================================================
    // GÁN DATA VÀO ITEM
    // =====================================================

    @Override
    public void onBindViewHolder(
            @NonNull FoodViewHolder holder,
            int position
    ) {

        Food food =
                foodList.get(position);


        // =============================================
        // TÊN MÓN
        // =============================================

        holder.txtFoodName.setText(
                food.getFood_name()
        );


        // =============================================
        // MÔ TẢ
        // =============================================

        holder.txtDescription.setText(
                food.getDescription()
        );


        // =============================================
        // GIÁ
        // =============================================

        holder.txtPrice.setText(
                formatMoney(
                        food.getPrice()
                )
        );


        // =============================================
        // HÌNH ẢNH
        // =============================================

        String imageName =
                food.getImage_url();


        if (imageName != null
                && !imageName.isEmpty()) {

            int imageResource =
                    context
                            .getResources()
                            .getIdentifier(
                                    imageName,
                                    "drawable",
                                    context.getPackageName()
                            );


            if (imageResource != 0) {

                holder.imgFood.setImageResource(
                        imageResource
                );

            } else {

                holder.imgFood.setImageResource(
                        R.drawable.logo_restaurant
                );
            }

        } else {

            holder.imgFood.setImageResource(
                    R.drawable.logo_restaurant
            );
        }


        // =============================================
        // THÊM VÀO GIỎ HÀNG
        // =============================================

        holder.btnAddCart.setOnClickListener(v -> {

            addToCart(food);

        });
    }


    // =====================================================
    // SỐ LƯỢNG ITEM
    // =====================================================

    @Override
    public int getItemCount() {

        return foodList.size();

    }


    // =====================================================
    // UPDATE DATA
    // =====================================================

    public void updateData(
            List<Food> newFoods
    ) {

        foodList.clear();


        if (newFoods != null) {

            foodList.addAll(
                    newFoods
            );
        }


        notifyDataSetChanged();
    }


    // =====================================================
    // THÊM MÓN VÀO GIỎ HÀNG
    // =====================================================

    private void addToCart(
            Food food
    ) {

        // =============================================
        // LẤY USER_ID ĐANG ĐĂNG NHẬP
        // =============================================

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(
                        "SushiZenAccount",
                        Context.MODE_PRIVATE
                );


        int userId =
                sharedPreferences.getInt(
                        "user_id",
                        -1
                );


        // =============================================
        // KHÔNG TÌM THẤY USER_ID
        // =============================================

        if (userId == -1) {

            Toast.makeText(
                    context,
                    "Không tìm thấy user_id. Vui lòng đăng nhập lại.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =============================================
        // TẠO API SERVICE
        // =============================================

        ApiService apiService =
                ApiClient
                        .getClient()
                        .create(
                                ApiService.class
                        );


        // =============================================
        // GỌI API
        // user_id + food_id
        // =============================================

        apiService
                .addToCart(
                        userId,
                        food.getFood_id()
                )
                .enqueue(
                        new Callback<CartResponse>() {


                            // =================================
                            // API TRẢ RESPONSE
                            // =================================

                            @Override
                            public void onResponse(
                                    @NonNull Call<CartResponse> call,
                                    @NonNull Response<CartResponse> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {


                                    CartResponse cartResponse =
                                            response.body();


                                    // =========================
                                    // THÊM THÀNH CÔNG
                                    // =========================

                                    if (cartResponse.isSuccess()) {

                                        String message =
                                                cartResponse.getMessage();


                                        if (message == null
                                                || message.isEmpty()) {

                                            message =
                                                    "Đã thêm "
                                                            + food.getFood_name()
                                                            + " vào giỏ hàng";
                                        }


                                        Toast.makeText(
                                                context,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();


                                    } else {


                                        // =====================
                                        // API BÁO THẤT BẠI
                                        // =====================

                                        String message =
                                                cartResponse.getMessage();


                                        if (message == null
                                                || message.isEmpty()) {

                                            message =
                                                    "Không thể thêm vào giỏ hàng";
                                        }


                                        Toast.makeText(
                                                context,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }


                                } else {


                                    // =========================
                                    // HTTP ERROR
                                    // =========================

                                    Toast.makeText(
                                            context,
                                            "Lỗi HTTP: "
                                                    + response.code(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }


                            // =================================
                            // LỖI KẾT NỐI / JSON
                            // =================================

                            @Override
                            public void onFailure(
                                    @NonNull Call<CartResponse> call,
                                    @NonNull Throwable t
                            ) {

                                Toast.makeText(
                                        context,
                                        "Lỗi kết nối: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // =====================================================
    // FORMAT TIỀN
    // =====================================================

    private String formatMoney(
            double price
    ) {

        NumberFormat formatter =
                NumberFormat
                        .getNumberInstance(
                                new Locale(
                                        "vi",
                                        "VN"
                                )
                        );


        return formatter.format(
                price
        ) + "đ";
    }


    // =====================================================
    // VIEW HOLDER
    // =====================================================

    static class FoodViewHolder
            extends RecyclerView.ViewHolder {


        ImageView imgFood;

        TextView txtFoodName;

        TextView txtDescription;

        TextView txtPrice;

        TextView btnAddCart;


        public FoodViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            imgFood =
                    itemView.findViewById(
                            R.id.imgFood
                    );


            txtFoodName =
                    itemView.findViewById(
                            R.id.txtFoodName
                    );


            txtDescription =
                    itemView.findViewById(
                            R.id.txtDescription
                    );


            txtPrice =
                    itemView.findViewById(
                            R.id.txtPrice
                    );


            btnAddCart =
                    itemView.findViewById(
                            R.id.btnAddCart
                    );
        }
    }
}