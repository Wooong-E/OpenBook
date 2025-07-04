package com.example.openbook.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Toast;

import com.example.openbook.BookFormFragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UserBookDonateFragment extends BookFormFragment {
    @Override
    protected void onDuplicateISBN(String isbn) {
        donateExistingBook(isbn);
    }

    @Override
    protected void onSubmit() {
        String title = et_Title.getText().toString().trim();
        String author = et_Author.getText().toString().trim();
        String isbn = et_Isbn.getText().toString().trim();
        String krc = et_Krc.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || krc.isEmpty()) {
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
                        askReviewAfterDonation(isbn);
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
                        askReviewAfterDonation(isbn);
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
                    Intent intent = new Intent(getContext(), UserReviewWriteActivity.class);
                    intent.putExtra("isbn", isbn);
                    startActivity(intent);
                })
                .setNegativeButton("다음에 할게요", null)
                .show();
    }

    private void clearFields() {
        et_Isbn.setText("");
        et_Title.setText("");
        et_Author.setText("");
        et_Krc.setText("");
        et_Isbn.setEnabled(true);
        canRegister = false;
    }
}
