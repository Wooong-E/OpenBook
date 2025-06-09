package com.example.openbook.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.openbook.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class BookDonateFragment extends Fragment {
    private View view;
    private EditText etISBN, etTitle, etAuthor, etKRC;
    private Button btnCheck, btnDonate;
    private boolean canRegister = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_user_book_donate, container, false);

        etISBN = view.findViewById(R.id.et_isbn);
        etTitle = view.findViewById(R.id.et_title);
        etAuthor = view.findViewById(R.id.et_author);
        etKRC = view.findViewById(R.id.et_krc);
        btnCheck = view.findViewById(R.id.btn_check_isbn);
        btnDonate = view.findViewById(R.id.btn_donate);

        btnCheck.setOnClickListener(v -> checkISBN());
        btnDonate.setOnClickListener(v -> {
            if (!canRegister) {
                Toast.makeText(getContext(), "먼저 ISBN 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                donateBook();
            }
        });

        return view;
    }

    private void checkISBN() {
        String isbn = etISBN.getText().toString().trim();

        if (isbn.length() != 13) {
            Toast.makeText(getContext(), "ISBN은 13자리여야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/CheckBook.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "isbn=" + URLEncoder.encode(isbn, "UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                boolean exists = json.getBoolean("exists");

                requireActivity().runOnUiThread(() -> {
                    if (exists) {
                        // 기존 ISBN 존재 시 → 별도로 서버에 전송
                        donateExistingBook(isbn);
                    } else {
                        Toast.makeText(getContext(), "제목, 저자, KRC를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        etISBN.setEnabled(false);
                        canRegister = true;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "서버 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }



    private void donateBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String isbn = etISBN.getText().toString().trim();
        String krc = etKRC.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) || TextUtils.isEmpty(krc)) {
            Toast.makeText(getContext(), "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.length() > 30 || author.length() > 30 || krc.length() > 20) {
            Toast.makeText(getContext(), "제목과 저자는 30자 이하, KRC는 20자 이하로 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/InsertBook.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "title=" + URLEncoder.encode(title, "UTF-8") +
                        "&author=" + URLEncoder.encode(author, "UTF-8") +
                        "&isbn=" + URLEncoder.encode(isbn, "UTF-8") +
                        "&krc=" + URLEncoder.encode(krc, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                boolean success = json.getBoolean("success");
                String message = json.getString("message");

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        askReviewAfterDonation(isbn);  // 리뷰 작성 유도 다이얼로그 호출
                        clearFields();
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "서버 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private void donateExistingBook(String isbn) {
        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/InsertBook.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // 이미 등록된 ISBN만 전달
                String data = "title=&author=&isbn=" + URLEncoder.encode(isbn, "UTF-8") + "&krc=";

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                boolean success = json.getBoolean("success");
                String message = json.getString("message");

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        askReviewAfterDonation(isbn);  // 리뷰 작성 유도 다이얼로그 호출
                        clearFields();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "서버 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void askReviewAfterDonation(String isbn) {
        new AlertDialog.Builder(getContext())
                .setTitle("리뷰 작성")
                .setMessage("도서 기부가 완료되었습니다. 리뷰를 작성하시겠습니까?")
                .setPositiveButton("작성할래요", (dialog, which) -> {
                    Intent intent = new Intent(getContext(), ReviewWriteActivity.class);
                    intent.putExtra("isbn", isbn);
                    startActivity(intent);
                })
                .setNegativeButton("다음에 할게요", null)
                .show();
    }


    private void clearFields() {
        etISBN.setText("");
        etTitle.setText("");
        etAuthor.setText("");
        etKRC.setText("");
        etISBN.setEnabled(true);
        canRegister = false;
    }
}
