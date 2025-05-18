package com.example.openbook.loginAndRegister;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {
    private Map<String,String> map;

    public RegisterRequest(String userID, String userPassword, String userName,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {

        super(Method.POST, "http://whdnd5725.dothome.co.kr/Register.php", listener, errorListener); //  errorListener 추가
        map = new HashMap<>();
        map.put("userID", userID);
        map.put("userPassword", userPassword);
        map.put("userName", userName);
    }

    @Override
    protected Map<String,String> getParams(){
        return map;
    }
}

