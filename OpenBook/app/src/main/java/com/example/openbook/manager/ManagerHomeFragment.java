package com.example.openbook.manager;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openbook.R;
import com.example.openbook.loginAndRegister.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ManagerHomeFragment extends Fragment {

    private View view;
    private Button btn_RegisterAnnouncement;
    private Button btn_Logout;
    private Button btn_OverdueUsers;
    private TextView tv_OverdueResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_manager_home, container, false);
        btn_RegisterAnnouncement = view.findViewById(R.id.btn_register_announcement);
        btn_OverdueUsers = view.findViewById(R.id.btn_overdue_users);
        btn_Logout = view.findViewById(R.id.btn_logout_manager);
        tv_OverdueResult = view.findViewById(R.id.tv_overdue_result);

        btn_RegisterAnnouncement.setOnClickListener(v -> showRegisterDialog());

        btn_Logout.setOnClickListener(v -> {
            SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        btn_OverdueUsers.setOnClickListener(v -> fetchOverdueUsers());

        return view;
    }

    private void showRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("공지사항 등록");

        final EditText input = new EditText(getContext());
        input.setHint("공지사항 내용을 입력하세요");
        input.setMinLines(3);
        input.setGravity(Gravity.TOP | Gravity.LEFT);
        builder.setView(input);

        builder.setPositiveButton("등록", (dialog, which) -> {
            String content = input.getText().toString().trim();
            if (!TextUtils.isEmpty(content)) {
                sendNoticeToServer(content);
            } else {
                Toast.makeText(getContext(), "내용이 비어있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private void sendNoticeToServer(String content) {
        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/RegisterAnnouncement.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "content=" + URLEncoder.encode(content, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                String message = json.getString("message");

                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "서버 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchOverdueUsers() {
        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/GetOverdueUsers.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) builder.append(line);
                reader.close();

                JSONArray jsonArray = new JSONArray(builder.toString());

                StringBuilder resultText = new StringBuilder();
                if (jsonArray.length() == 0) {
                    resultText.append("연체자가 없습니다.");
                } else {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        resultText.append("📌 ID: ").append(obj.getString("userID"))
                                .append(" | 이름: ").append(obj.getString("userName"))
                                .append(" | 연체권수: ").append(obj.getInt("overdueCount"))
                                .append("\n");
                    }
                }

                requireActivity().runOnUiThread(() -> {
                    tv_OverdueResult.setVisibility(View.VISIBLE);
                    tv_OverdueResult.setText(resultText.toString().trim());
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    tv_OverdueResult.setVisibility(View.VISIBLE);
                    tv_OverdueResult.setText("⚠ 연체자 목록을 불러오는 데 실패했습니다.");
                });
            }
        }).start();
    }
}
