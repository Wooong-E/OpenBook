package com.example.openbook.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openbook.BaseActivity;
import com.example.openbook.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class CurrentLoanActivity extends BaseActivity {

    private ListView listView;
    private LoanAdapter adapter;
    private List<LoanBook> loanList = new ArrayList<>();
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_loans);

        listView = findViewById(R.id.loan_list_view);
        adapter = new LoanAdapter();
        listView.setAdapter(adapter);

        SharedPreferences sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userID = sharedPref.getString("userID", "");

        loadLoans();
    }

    private void loadLoans() {
        new Thread(() -> {
            try {
                String urlStr = "http://whdnd5725.dothome.co.kr/GetCurrentLoans.php?userID=" + URLEncoder.encode(userID, "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                boolean success = json.getBoolean("success");

                if (success) {
                    JSONArray loans = json.getJSONArray("loans");
                    loanList.clear();

                    for (int i = 0; i < loans.length(); i++) {
                        JSONObject book = loans.getJSONObject(i);
                        loanList.add(new LoanBook(
                                book.getString("title"),
                                book.getString("author"),
                                book.getString("isbn"),
                                book.getString("loan_date"),
                                book.getString("return_date")
                        ));
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "대출 중인 도서가 없습니다.", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "서버 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void returnBook(String isbn) {
        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/ReturnBook.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "userID=" + URLEncoder.encode(userID, "UTF-8") +
                        "&isbn=" + URLEncoder.encode(isbn, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
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
                        askReviewAfterReturn(isbn);  // 리뷰 작성 유도
                        loadLoans(); // 목록 갱신
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "반납 실패", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    class LoanAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return loanList.size();
        }

        @Override
        public Object getItem(int position) {
            return loanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_loan_book, parent, false);

            TextView title = convertView.findViewById(R.id.tv_title);
            TextView author = convertView.findViewById(R.id.tv_author);
            TextView date = convertView.findViewById(R.id.tv_loan_date);
            Button btnReturn = convertView.findViewById(R.id.btn_return);

            LoanBook book = loanList.get(position);
            title.setText(book.title);
            author.setText(book.author);
            date.setText("대출일: " + book.loanDate);

            btnReturn.setOnClickListener(v -> returnBook(book.isbn));

            TextView returnDateView = convertView.findViewById(R.id.tv_return_date);
            returnDateView.setText("반납 예정일: " + book.returnDate);

            return convertView;
        }
    }

    private void askReviewAfterReturn(String isbn) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("리뷰 작성")
                .setMessage("도서를 반납했습니다. 리뷰를 작성하시겠습니까?")
                .setPositiveButton("작성할래요", (dialog, which) -> {
                    Intent intent = new Intent(CurrentLoanActivity.this, ReviewWriteActivity.class);
                    intent.putExtra("isbn", isbn);
                    startActivity(intent);
                })
                .setNegativeButton("다음에 할게요", null)
                .show();
    }
    static class LoanBook {
        String title, author, isbn, loanDate, returnDate;

        LoanBook(String t, String a, String i, String d, String r) {
            title = t;
            author = a;
            isbn = i;
            loanDate = d;
            returnDate = r;
        }
    }
}