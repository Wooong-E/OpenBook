package com.example.openbook.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;


import com.example.openbook.BaseActivity;
import com.example.openbook.R;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UserReviewWriteActivity extends BaseActivity {

    private EditText et_ReviewContent;
    private RatingBar ratingBar;
    private Button btn_Submit;

    private String isbn;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_review_write);

        isbn = getIntent().getStringExtra("isbn");
        SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userID = pref.getString("userID", "");

        et_ReviewContent = findViewById(R.id.edt_review_content);
        ratingBar = findViewById(R.id.rating_bar);
        btn_Submit = findViewById(R.id.btn_submit_review);

        btn_Submit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        String content = et_ReviewContent.getText().toString().trim();
        int rating = (int) ratingBar.getRating();

        if (content.length() < 10) {
            Toast.makeText(this, "리뷰는 10자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/InsertReview.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "userID=" + URLEncoder.encode(userID, "UTF-8") +
                        "&isbn=" + URLEncoder.encode(isbn, "UTF-8") +
                        "&content=" + URLEncoder.encode(content, "UTF-8") +
                        "&rating=" + rating;

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "리뷰 등록 실패", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
