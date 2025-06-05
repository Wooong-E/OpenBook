package com.example.openbook.user;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.openbook.BaseActivity;
import com.example.openbook.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UserTopReadersActivity extends BaseActivity {
    private TextView tv_RankInfo;
    private TableLayout table;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_top_readers);

        table = findViewById(R.id.table_layout);
        tv_RankInfo = findViewById(R.id.tv_rank_info);

        SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userID = pref.getString("userID", "");

        new Thread(() -> {
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/GetTopReaders.php?userID=" + URLEncoder.encode(userID, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                if (json.getBoolean("success")) {
                    JSONArray topUsers = json.getJSONArray("topUsers");
                    int yourRank = json.getInt("yourRank");
                    double percent = json.getDouble("yourPercent");

                    List<JSONObject> userList = new ArrayList<>();
                    for (int i = 0; i < topUsers.length(); i++) {
                        userList.add(topUsers.getJSONObject(i));
                    }

                    runOnUiThread(() -> {
                        // ✅ 헤더 추가
                        TableRow header = new TableRow(UserTopReadersActivity.this);
                        String[] titles = {"순위", "이름", "대출 권수"};
                        for (String title : titles) {
                            TextView cell = new TextView(UserTopReadersActivity.this);
                            cell.setText(title);
                            cell.setTextSize(18);
                            cell.setPadding(16, 16, 16, 16);
                            cell.setTypeface(null, android.graphics.Typeface.BOLD);
                            header.addView(cell);
                        }
                        table.addView(header);

                        for (int i = 0; i < userList.size(); i++) {
                            JSONObject user = userList.get(i);

                            TableRow row = new TableRow(UserTopReadersActivity.this);
                            row.setPadding(0, 8, 0, 8);

                            TextView rank = new TextView(UserTopReadersActivity.this);
                            TextView name = new TextView(UserTopReadersActivity.this);
                            TextView total = new TextView(UserTopReadersActivity.this);

                            rank.setText((i + 1) + "등");
                            rank.setTextSize(16);
                            rank.setPadding(16, 8, 16, 8);

                            try {
                                name.setText(user.getString("userName"));
                                total.setText(user.getString("userTotalBorrow") + "권");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            name.setTextSize(16);
                            name.setPadding(16, 8, 16, 8);

                            total.setTextSize(16);
                            total.setPadding(16, 8, 16, 8);

                            row.addView(rank);
                            row.addView(name);
                            row.addView(total);
                            table.addView(row);
                        }

                        tv_RankInfo.setText("당신은 전체 중 " + yourRank + "등이며 상위 " + percent + "%입니다.");
                    });

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
