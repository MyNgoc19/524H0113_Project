package com.example.project.adapter;

import android.content.Context;
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
import com.example.project.model.CartItem;
import com.example.project.model.CartResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CartAdapter
        extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {


    private final Context context;

    private final List<CartItem> cartItems =
            new ArrayList<>();


    public CartAdapter(Context context) {

        this.context = context;
    }


    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(context)
                        .inflate(
                                R.layout.item_cart,
                                parent,
                                false
                        );


        return new CartViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull CartViewHolder holder,
            int position
    ) {

        CartItem item =
                cartItems.get(position);


        // =========================================
        // TÊN
        // =========================================

        holder.txtFoodName.setText(
                item.getFood_name()
        );


        // =========================================
        // GIÁ
        // =========================================

        holder.txtPrice.setText(
                formatMoney(
                        item.getPrice()
                )
        );


        // =========================================
        // QUANTITY
        // =========================================

        holder.txtQuantity.setText(
                String.valueOf(
                        item.getQuantity()
                )
        );


        // =========================================
        // HÌNH
        // =========================================

        String imageName =
                item.getImage_url();


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


        // =========================================
        // NÚT +
        // =========================================

        holder.btnPlus.setOnClickListener(v -> {

            int newQuantity =
                    item.getQuantity() + 1;


            updateQuantity(
                    item,
                    newQuantity
            );
        });


        // =========================================
        // NÚT -
        // =========================================

        holder.btnMinus.setOnClickListener(v -> {


            if (item.getQuantity() > 1) {


                int newQuantity =
                        item.getQuantity() - 1;


                updateQuantity(
                        item,
                        newQuantity
                );


            } else {


                // quantity = 1
                // nhấn - → xóa món

                deleteItem(item);
            }
        });


        // =========================================
        // NÚT XÓA
        // =========================================

        holder.btnDelete.setOnClickListener(v -> {

            deleteItem(item);

        });
    }


    // =====================================================
    // UPDATE QUANTITY
    // =====================================================

    private void updateQuantity(
            CartItem item,
            int newQuantity
    ) {

        ApiService apiService =
                ApiClient
                        .getClient()
                        .create(
                                ApiService.class
                        );


        apiService
                .updateCart(
                        item.getCart_item_id(),
                        newQuantity
                )
                .enqueue(
                        new Callback<CartResponse>() {


                            @Override
                            public void onResponse(
                                    @NonNull Call<CartResponse> call,
                                    @NonNull Response<CartResponse> response
                            ) {


                                if (response.isSuccessful()
                                        && response.body() != null
                                        && response.body().isSuccess()) {


                                    // CẬP NHẬT OBJECT LOCAL
                                    item.setQuantity(
                                            newQuantity
                                    );


                                    notifyDataSetChanged();


                                    Toast.makeText(
                                            context,
                                            "Đã cập nhật số lượng",
                                            Toast.LENGTH_SHORT
                                    ).show();


                                } else {


                                    Toast.makeText(
                                            context,
                                            "Không thể cập nhật số lượng",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    @NonNull Call<CartResponse> call,
                                    @NonNull Throwable t
                            ) {

                                Toast.makeText(
                                        context,
                                        "Lỗi: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // =====================================================
    // DELETE
    // =====================================================

    private void deleteItem(
            CartItem item
    ) {

        ApiService apiService =
                ApiClient
                        .getClient()
                        .create(
                                ApiService.class
                        );


        apiService
                .deleteCart(
                        item.getCart_item_id()
                )
                .enqueue(
                        new Callback<CartResponse>() {


                            @Override
                            public void onResponse(
                                    @NonNull Call<CartResponse> call,
                                    @NonNull Response<CartResponse> response
                            ) {


                                if (response.isSuccessful()
                                        && response.body() != null
                                        && response.body().isSuccess()) {


                                    cartItems.remove(item);


                                    notifyDataSetChanged();


                                    Toast.makeText(
                                            context,
                                            "Đã xóa món",
                                            Toast.LENGTH_SHORT
                                    ).show();


                                } else {


                                    Toast.makeText(
                                            context,
                                            "Không thể xóa món",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }


                            @Override
                            public void onFailure(
                                    @NonNull Call<CartResponse> call,
                                    @NonNull Throwable t
                            ) {

                                Toast.makeText(
                                        context,
                                        "Lỗi: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    @Override
    public int getItemCount() {

        return cartItems.size();
    }


    public void updateData(
            List<CartItem> newItems
    ) {

        cartItems.clear();


        if (newItems != null) {

            cartItems.addAll(
                    newItems
            );
        }


        notifyDataSetChanged();
    }


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


    static class CartViewHolder
            extends RecyclerView.ViewHolder {


        ImageView imgFood;

        TextView txtFoodName;
        TextView txtPrice;
        TextView txtQuantity;

        TextView btnMinus;
        TextView btnPlus;
        TextView btnDelete;


        public CartViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            imgFood =
                    itemView.findViewById(
                            R.id.imgCartFood
                    );


            txtFoodName =
                    itemView.findViewById(
                            R.id.txtCartFoodName
                    );


            txtPrice =
                    itemView.findViewById(
                            R.id.txtCartPrice
                    );


            txtQuantity =
                    itemView.findViewById(
                            R.id.txtQuantity
                    );


            btnMinus =
                    itemView.findViewById(
                            R.id.btnMinus
                    );


            btnPlus =
                    itemView.findViewById(
                            R.id.btnPlus
                    );


            btnDelete =
                    itemView.findViewById(
                            R.id.btnDelete
                    );
        }
    }
}