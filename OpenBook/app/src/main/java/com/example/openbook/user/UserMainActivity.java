package com.example.openbook.user;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.openbook.BaseActivity;
import com.example.openbook.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserMainActivity extends BaseActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private UserBookDonateFragment bookDonateFragment;
    private UserBookSearchAndBorrowFragment bookSearchAndBorrowFragment;
    private UserMyPageFragment myPageFragment;
    private UserHomeFragment userHomeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_main);

        bottomNavigationView = findViewById(R.id.userBottomNavi);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_book_search_and_borrow) {
                    setFrag(0);
                    return true;
                } else if (itemId == R.id.action_home) {
                    setFrag(1);
                    return true;
                } else if (itemId == R.id.action_book_donate) {
                    setFrag(2);
                    return true;
                } else if (itemId == R.id.action_my_page) {
                    setFrag(3);
                    return true;
                }
                return false;
            }
        });

        bookSearchAndBorrowFragment = new UserBookSearchAndBorrowFragment();
        userHomeFragment = new UserHomeFragment();
        bookDonateFragment = new UserBookDonateFragment();
        myPageFragment = new UserMyPageFragment();
        setFrag(1);
    }


    // fragment 교체가 일어나는 실행문.
    private void setFrag(int n){
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch(n){
            case 0:
                ft.replace(R.id.main_frame, bookSearchAndBorrowFragment);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.main_frame, userHomeFragment);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.main_frame, bookDonateFragment);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.main_frame, myPageFragment);
                ft.commit();
                break;
        }
    }

}
