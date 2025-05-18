package com.example.openbook;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
public class BaseActivity extends AppCompatActivity {
    private final OnBackPressedCallback backCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, backCallback);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
