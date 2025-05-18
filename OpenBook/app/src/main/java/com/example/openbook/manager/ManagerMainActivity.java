package com.example.openbook.manager;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.openbook.BaseActivity;
import com.example.openbook.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ManagerMainActivity extends BaseActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private ManagerBookSearchFragment bookSearchFragment;
    private ManagerBookRegistrationFragment bookRegistrationFragment;
    private ManagerBookStatisticsFragment bookStatisticsFragment;
    private ManagerHomeFragment managerHomeFragment;
    private ManagerUserManagementFragment userManagementFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_main);

        bottomNavigationView = findViewById(R.id.managerBottomNavi);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_book_search) {
                    setFrag(0);
                    return true;
                } else if (itemId == R.id.action_book_registration) {
                    setFrag(1);
                    return true;
                } else if (itemId == R.id.action_home) {
                    setFrag(2);
                    return true;
                } else if (itemId == R.id.action_book_statistics) {
                    setFrag(3);
                    return true;
                } else if (itemId == R.id.action_user_management) {
                    setFrag(4);
                    return true;
                }

                return false;
            }
        });

        bookRegistrationFragment = new ManagerBookRegistrationFragment();
        bookSearchFragment = new ManagerBookSearchFragment();
        bookStatisticsFragment = new ManagerBookStatisticsFragment();
        managerHomeFragment = new ManagerHomeFragment();
        userManagementFragment = new ManagerUserManagementFragment();
        setFrag(2);
    }


    // fragment 교체가 일어나는 실행문.
    private void setFrag(int n){
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch(n){
            case 0:
                ft.replace(R.id.main_frame, bookSearchFragment);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.main_frame, bookRegistrationFragment);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.main_frame, managerHomeFragment);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.main_frame, bookStatisticsFragment);
                ft.commit();
                break;
            case 4:
                ft.replace(R.id.main_frame, userManagementFragment);
                ft.commit();
                break;
        }
    }
}
