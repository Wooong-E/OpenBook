package com.example.openbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class BookDetailActivity extends BaseActivity {

    private TextView tv_Title, tv_Author, tv_Isbn, tv_Total, tv_Available;
    private Button btn_Loan;
    private String isbn;
    private int availableCount;

    private LinearLayout reviewSection;
    private Button btn_SortLatest, btn_SortRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        tv_Title = findViewById(R.id.tv_title);
        tv_Author = findViewById(R.id.tv_author);
        tv_Isbn = findViewById(R.id.tv_isbn);
        tv_Total = findViewById(R.id.tv_total_count);
        tv_Available = findViewById(R.id.tv_available_count);
        btn_Loan = findViewById(R.id.btn_loan);
        reviewSection = findViewById(R.id.review_section);
        btn_SortLatest = findViewById(R.id.btn_sort_latest);
        btn_SortRating = findViewById(R.id.btn_sort_rating);

        loadReviews("latest");


        btn_SortLatest.setOnClickListener(v -> loadReviews("latest"));
        btn_SortRating.setOnClickListener(v -> loadReviews("rating"));

        Intent intent = getIntent();
        isbn = intent.getStringExtra("isbn");

        loadBookDetail();

        loadAverageRating(isbn);

        boolean isManager = getIntent().getBooleanExtra("isManager", false);
        if (isManager) {
            btn_Loan.setEnabled(false);
            btn_Loan.setText("관리자는 대출할 수 없습니다.");
        } else {
            btn_Loan.setOnClickListener(v -> attemptLoan());
        }

    }

    private void loadBookDetail() {
        new Thread(() -> {
            try {
                String urlStr = "http://whdnd5725.dothome.co.kr/GetBookDetail.php?isbn=" + URLEncoder.encode(isbn, "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                boolean success = json.getBoolean("success");

                if (success) {
                    String title = json.getString("title");
                    String author = json.getString("author");
                    int total = json.getInt("totalCount");
                    availableCount = json.getInt("availableCount");

                    runOnUiThread(() -> {
                        tv_Title.setText(title);
                        tv_Author.setText(author);
                        tv_Isbn.setText(isbn);
                        tv_Total.setText("총 권수: " + total);
                        tv_Available.setText("대출 가능: " + availableCount);
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "도서 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "서버 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void attemptLoan() {
        if (availableCount <= 0) {
            Toast.makeText(this, "대출 가능한 수량이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userID = pref.getString("userID", "");

        if (userID.isEmpty()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/AttemptLoan.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "userID=" + URLEncoder.encode(userID, "UTF-8") +
                        "&isbn=" + URLEncoder.encode(isbn, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                boolean success = json.getBoolean("success");
                String message = json.getString("message");

                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        finish();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "대출 처리 중 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadReviews(String order) {
        new Thread(() -> {
            try {
                String urlStr = "http://whdnd5725.dothome.co.kr/GetReviews.php?isbn=" + URLEncoder.encode(isbn, "UTF-8") + "&order=" + URLEncoder.encode(order, "UTF-8");
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                if (json.getBoolean("success")) {
                    JSONArray reviews = json.getJSONArray("reviews");

                    runOnUiThread(() -> {
                        reviewSection.removeAllViews();
                        for (int i = 0; i < reviews.length(); i++) {
                            try{
                                JSONObject r = reviews.getJSONObject(i);
                                String user = r.getString("userID");
                                String content = r.getString("content");
                                int rating = r.getInt("rating");
                                String createdAt = r.getString("created_at");

                                TextView tv = new TextView(this);
                                tv.setText("👤 " + user + "  |  평점: " + rating + "\n" + content + "\n작성일: " + createdAt);
                                tv.setPadding(0, 16, 0, 16);
                                reviewSection.addView(tv);
                            }catch(JSONException e){
                                e.printStackTrace();
                            }

                        }
                    });
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadAverageRating(String isbn) {
        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/GetAverageRating.php?isbn=" + URLEncoder.encode(isbn, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                if (json.getBoolean("success")) {
                    double average = json.getDouble("average");
                    runOnUiThread(() -> {
                        TextView avgText = findViewById(R.id.tv_avg_rating);
                        avgText.setText("⭐ 평균: " + String.format("%.1f", average));
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}