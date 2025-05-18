package com.example.openbook.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.openbook.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserHomeFragment extends Fragment {

    private View view;
    private TextView tv_Announcement;
    private Button btn_CurrentLoans;

    private Button btn_TopReaders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_home, container, false);
        tv_Announcement = view.findViewById(R.id.tv_announcement);
        btn_CurrentLoans = view.findViewById(R.id.btn_current_loans);
        btn_TopReaders = view.findViewById(R.id.btn_top_readers);

        loadAnnouncement();

        btn_CurrentLoans.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserCurrentLoanActivity.class);
            startActivity(intent);
        });

        btn_TopReaders.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserTopReadersActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadAnnouncement() {
        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/GetLatestAnnouncement.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                Log.d("ANNOUNCEMENT", "서버 응답: " + response);

                JSONObject json = new JSONObject(response.toString());
                boolean success = json.getBoolean("success");

                String content;
                if (success) {
                    content = json.getString("content");
                } else {
                    content = "공지사항 없음";
                }

                String finalContent = content;
                requireActivity().runOnUiThread(() -> tv_Announcement.setText(finalContent));

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> tv_Announcement.setText("공지사항 불러오기 실패"));
            }
        }).start();
    }
}
