package com.example.openbook.loginAndRegister;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {
    private Map<String,String> map;

    public LoginRequest(String userID, String userPassword, Response.Listener<String> listener){
        super(Method.POST, "http://whdnd5725.dothome.co.kr/Login.php", listener, null);
        map = new HashMap<>();
        map.put("userID", userID);
        map.put("userPassword", userPassword);
    }

    @Override
    protected Map<String,String> getParams(){
        return map;
    }
}
