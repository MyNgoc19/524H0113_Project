
package com.example.project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.api.ApiClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class ChatActivity extends AppCompatActivity {

    private EditText edtMessage;
    private Button btnSend;
    private int orderId;
    private String token;

    interface ChatApi {

        @FormUrlEncoded
        @POST("order/send_message.php")
        Call<ResponseBody> sendMessage(
                @Header("Authorization") String authorization,
                @Field("order_id") int orderId,
                @Field("message") String message
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSendMessage);

        orderId = getIntent().getIntExtra("order_id", -1);

        SharedPreferences preferences = getSharedPreferences(
                "SushiZenAccount",
                MODE_PRIVATE
        );

        token = preferences.getString("access_token", "");

        if (orderId <= 0 || token.isEmpty()) {
            Toast.makeText(
                    this,
                    "Thiếu đơn hàng hoặc token đăng nhập",
                    Toast.LENGTH_LONG
            ).show();
            btnSend.setEnabled(false);
            return;
        }

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {

        String message = edtMessage.getText().toString().trim();

        if (message.isEmpty()) {
            edtMessage.setError("Vui lòng nhập tin nhắn");
            return;
        }

        btnSend.setEnabled(false);

        ChatApi api = ApiClient
                .getClient()
                .create(ChatApi.class);

        api.sendMessage(
                "Bearer " + token,
                orderId,
                message
        ).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(
                    @NonNull Call<ResponseBody> call,
                    @NonNull Response<ResponseBody> response
            ) {
                btnSend.setEnabled(true);

                if (response.isSuccessful()) {
                    edtMessage.setText("");

                    Toast.makeText(
                            ChatActivity.this,
                            "Gửi tin nhắn thành công",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            ChatActivity.this,
                            "Gửi thất bại (HTTP " + response.code() + ")",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<ResponseBody> call,
                    @NonNull Throwable t
            ) {
                btnSend.setEnabled(true);

                Toast.makeText(
                        ChatActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
