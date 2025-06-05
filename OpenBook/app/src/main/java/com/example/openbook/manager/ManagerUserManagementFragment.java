package com.example.openbook.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.openbook.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ManagerUserManagementFragment extends Fragment {

    private TableLayout userTable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager_user_management, container, false);

        userTable = new TableLayout(getContext());
        userTable.setStretchAllColumns(true);
        ((ViewGroup) view.findViewById(R.id.user_container)).addView(userTable);


        fetchUserData();
        return view;
    }

    private void fetchUserData() {
        new Thread(() -> {
            List<User> users = new ArrayList<>();
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/GetStudentUsers.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(result.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String userId = obj.getString("userID");
                    String userName = obj.getString("userName");
                    int userTotalBorrow = obj.getInt("userTotalBorrow");
                    users.add(new User(userId, userName, userTotalBorrow));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            requireActivity().runOnUiThread(() -> updateUserTable(users));
        }).start();
    }

    private void updateUserTable(List<User> users) {
        userTable.removeAllViews();

        TableRow header = new TableRow(getContext());
        header.addView(createCell("아이디", true));
        header.addView(createCell("이름", true));
        header.addView(createCell("총 대출 수", true));
        userTable.addView(header);

        for (User user : users) {
            TableRow row = new TableRow(getContext());
            row.addView(createCell(user.getUserId(), false));
            row.addView(createCell(user.getUserName(), false));
            row.addView(createCell(String.valueOf(user.getUserTotalBorrow()), false));
            userTable.addView(row);
        }
    }

    private TextView createCell(String text, boolean isHeader) {
        TextView cell = new TextView(getContext());
        cell.setText(text);
        cell.setPadding(16, 8, 16, 8);
        cell.setTextSize(isHeader ? 16 : 14);
        return cell;
    }

    static class User {
        private final String userId;
        private final String userName;
        private final int userTotalBorrow;

        public User(String userId, String userName, int userTotalBorrow) {
            this.userId = userId;
            this.userName = userName;
            this.userTotalBorrow = userTotalBorrow;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public int getUserTotalBorrow() {
            return userTotalBorrow;
        }
    }
}