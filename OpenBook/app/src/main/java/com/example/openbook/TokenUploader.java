package com.example.openbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TokenUploader {
    public static void uploadFCMToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String userID = pref.getString("userID", null);

        if (userID == null) {
            Log.e("FCM", "User ID not found in SharedPreferences");
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.d("FCM", "Token: " + token);
                sendTokenToServer(userID, token);
            } else {
                Log.e("FCM", "Token retrieval failed");
            }
        });
    }

    private static void sendTokenToServer(String userID, String token) {
        new Thread(() -> {
            try {
                URL url = new URL( "http://whdnd5725.dothome.co.kr/SaveToken.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String postData = "userID=" + URLEncoder.encode(userID, "UTF-8") +
                        "&token=" + URLEncoder.encode(token, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("FCM", "Token sent. Response: " + responseCode);
            } catch (Exception e) {
                Log.e("FCM", "Error sending token to server", e);
            }
        }).start();
    }
}

