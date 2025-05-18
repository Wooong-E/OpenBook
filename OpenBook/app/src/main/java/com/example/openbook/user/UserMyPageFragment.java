package com.example.openbook.user;

import com.example.openbook.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;

import com.example.openbook.loginAndRegister.LoginActivity;
import android.widget.Switch;
import android.widget.CompoundButton;
import com.example.openbook.TokenUploader;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UserMyPageFragment extends Fragment {
    private View view;
    private Button btn_AccountInfo;
    private Button btn_Logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_user_mypage, container, false);

        // FCM 알림 스위치 처리
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchFCM = view.findViewById(R.id.switch_fcm);
        btn_AccountInfo = view.findViewById(R.id.btn_account_info);
        SharedPreferences fcmPref = requireActivity().getSharedPreferences("FCM_PREF", getContext().MODE_PRIVATE);
        boolean isFcmEnabled = fcmPref.getBoolean("enabled", true); // 기본값은 ON
        switchFCM.setChecked(isFcmEnabled);


        btn_AccountInfo.setOnClickListener(v -> {
            SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
            String userID = sharedPref.getString("userID", "알 수 없음");
            String name = sharedPref.getString("userName", "이름 없음");

            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("계정 정보")
                    .setMessage("학번: " + userID + "\n이름: " + name)
                    .setPositiveButton("확인", null)
                    .show();
        });

        switchFCM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = fcmPref.edit();
                editor.putBoolean("enabled", isChecked);
                editor.apply();

                if (isChecked) {
                    // FCM 허용, 서버에 토큰 등록
                    TokenUploader.uploadFCMToken(requireContext());
                } else {
                    SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
                    String userID = sharedPref.getString("userID", null);

                    if (userID != null) {
                        new Thread(() -> {
                            try {
                                URL url = new URL("http://whdnd5725.dothome.co.kr/DeleteToken.php");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setDoOutput(true);

                                String postData = "userID=" + URLEncoder.encode(userID, "UTF-8");

                                OutputStream os = conn.getOutputStream();
                                os.write(postData.getBytes());
                                os.flush();
                                os.close();

                                conn.getResponseCode();  // 응답은 무시해도 됨
                                Log.d("FCM", "알림 끔 → 서버에 토큰 삭제 요청 완료");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }

                    Log.d("FCM", "사용자가 알림 비활성화함");
                }

            }
        });

        // 로그아웃 버튼 처리
        btn_Logout = view.findViewById(R.id.btn_logout);
        btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserInfo", getContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        return view;
    }
}
