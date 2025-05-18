package com.example.openbook.loginAndRegister;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.openbook.BaseActivity;
import com.example.openbook.R;
import com.example.openbook.TokenUploader;
import com.example.openbook.manager.ManagerMainActivity;
import com.example.openbook.user.UserMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity {

    private EditText et_Id, et_Password;
    private Button btn_Login, btn_Register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // 반드시 먼저 호출

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 자동 로그인 체크는 onCreate 이후 안전하게 처리
        checkAutoLogin();

        et_Id = findViewById(R.id.et_id);
        et_Password = findViewById(R.id.et_password);
        btn_Login = findViewById(R.id.btn_login);
        btn_Register = findViewById(R.id.btn_register);

        btn_Register.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btn_Login.setOnClickListener(view -> {
            String userID = et_Id.getText().toString().trim();
            String userPasswd = et_Password.getText().toString().trim();

            // 유효성 검사
            if (!userID.matches("^\\d{8,10}$")) {
                Toast.makeText(getApplicationContext(), "학번은 8자 이상 10자 이하 숫자만 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!userPasswd.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")) {
                Toast.makeText(getApplicationContext(), "비밀번호는 영문+숫자 포함 8자 이상 16자 이하로 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서버 응답 처리
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        TokenUploader.uploadFCMToken(LoginActivity.this);  // 로그인 성공 직후에 토큰 서버 전송

                        String userName = jsonObject.getString("userName");
                        String userType = jsonObject.getString("userType");

                        SharedPreferences sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userID", userID);
                        editor.putString("userName", userName);
                        editor.putString("userType", userType);
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                        Intent intent;
                        if ("manager".equals(userType)) {
                            intent = new Intent(LoginActivity.this, ManagerMainActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, UserMainActivity.class);
                        }

                        intent.putExtra("userID", userID);
                        intent.putExtra("userName", userName);
                        intent.putExtra("userType", userType);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "학번 또는 비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_SHORT).show();
                        et_Id.setText("");
                        et_Password.setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "로그인 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                }
            };

            LoginRequest loginRequest = new LoginRequest(userID, userPasswd, responseListener);
            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
            queue.add(loginRequest);
        });
    }

    private void checkAutoLogin() {
        SharedPreferences sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            TokenUploader.uploadFCMToken(this); //  토큰 업로드
            new Handler(Looper.getMainLooper()).post(() -> {
                String userType = sharedPref.getString("userType", "");
                Intent intent;

                if ("manager".equals(userType)) {
                    intent = new Intent(LoginActivity.this, ManagerMainActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, UserMainActivity.class);
                }

                intent.putExtra("userID", sharedPref.getString("userID", ""));
                intent.putExtra("userName", sharedPref.getString("userName", ""));
                intent.putExtra("userType", userType);

                startActivity(intent);
                finish();
            });
        }
    }
}
