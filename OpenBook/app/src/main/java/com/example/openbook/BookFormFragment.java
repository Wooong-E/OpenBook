package com.example.openbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public abstract class BookFormFragment extends Fragment {
    protected View view;
    protected EditText et_Isbn, et_Title, et_Author, et_Krc;
    protected Button btn_Check, btn_Submit;
    protected boolean canRegister = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_book_form, container, false);

        et_Isbn = view.findViewById(R.id.et_isbn);
        et_Title = view.findViewById(R.id.et_title);
        et_Author = view.findViewById(R.id.et_author);
        et_Krc = view.findViewById(R.id.et_krc);
        btn_Check = view.findViewById(R.id.btn_check_isbn);
        btn_Submit = view.findViewById(R.id.btn_donate);

        btn_Check.setOnClickListener(v -> checkISBN());
        btn_Submit.setOnClickListener(v -> {
            if (!canRegister) {
                Toast.makeText(getContext(), "먼저 ISBN 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                onSubmit();
            }
        });

        return view;
    }

    private void checkISBN() {
        String isbn = et_Isbn.getText().toString().trim();

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
                        onDuplicateISBN(isbn);
                    } else {
                        Toast.makeText(getContext(), "제목, 저자, KRC를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        et_Isbn.setEnabled(false);
                        canRegister = true;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "서버 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    protected abstract void onDuplicateISBN(String isbn);
    protected abstract void onSubmit();
}
