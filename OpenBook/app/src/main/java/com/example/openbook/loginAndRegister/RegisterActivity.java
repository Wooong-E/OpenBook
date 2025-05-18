package com.example.openbook.loginAndRegister;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends BaseActivity {

    private EditText et_Id, et_Password, et_Name;
    private Button btn_Register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        et_Id=findViewById(R.id.et_id);
        et_Password=findViewById(R.id.et_password);
        et_Name=findViewById(R.id.et_name);

        btn_Register = findViewById(R.id.btn_register);


        // 회원가입 버튼 클릭 시 수행
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = et_Id.getText().toString().trim();
                String userPasswd = et_Password.getText().toString().trim();
                String userName = et_Name.getText().toString().trim();

                // ✅ 1. 빈칸 여부 검사
                if (userID.isEmpty() || userPasswd.isEmpty() || userName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "회원가입 실패: 모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ 2. 학번: 숫자만, 8자 이상 10자 이하
                if (!userID.matches("^\\d{8,10}$")) {
                    Toast.makeText(getApplicationContext(), "회원가입 실패: 학번은 8자 이상 10자 이하 숫자여야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ 3. 비밀번호: 영문+숫자 포함, 8자 이상 16자 이하
                if (!userPasswd.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")) {
                    Toast.makeText(getApplicationContext(), "회원가입 실패: 비밀번호는 영문+숫자 포함 8자 이상 16자 이하로 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ 4. 이름: 1~10자
                if (userName.length() > 10) {
                    Toast.makeText(getApplicationContext(), "회원가입 실패: 이름은 10자 이하로 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ 서버 통신
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                Toast.makeText(getApplicationContext(), "회원 등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                String reason = jsonObject.optString("reason", "unknown");
                                if ("duplicate".equals(reason)) {
                                    Toast.makeText(getApplicationContext(), "회원가입 실패: 이미 존재하는 학번입니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "회원가입 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                RegisterRequest registerRequest = new RegisterRequest(userID, userPasswd, userName,
                        responseListener,
                        error -> {
                            Toast.makeText(getApplicationContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                        }
                );
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(registerRequest);
            }
        });




    }
}