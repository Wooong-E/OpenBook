package com.example.openbook.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ManagerBookStatisticsFragment extends Fragment {

    private TableLayout tableLayout;
    private CheckBox checkboxFilterUnreturned;
    private List<Loan> loanList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager_book_statistics, container, false);
        tableLayout = view.findViewById(R.id.loan_table);
        checkboxFilterUnreturned = view.findViewById(R.id.checkbox_filter_unreturned);

        fetchLoanData();

        checkboxFilterUnreturned.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                displayFilteredLoans();
            } else {
                displayLoansSorted();
            }
        });

        return view;
    }

    private void fetchLoanData() {
        new Thread(() -> {
            List<Loan> loans = new ArrayList<>();
            try {
                URL url = new URL("http://whdnd5725.dothome.co.kr/GetLoanStatistics.php");
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
                    int id = obj.getInt("id");
                    String userId = obj.getString("userID");
                    String isbn = obj.getString("isbn");
                    String loanDate = obj.getString("loan_date");
                    String returnDate = obj.getString("return_date");
                    boolean returned = obj.getInt("returned") == 1;
                    loans.add(new Loan(id, userId, isbn, loanDate, returnDate, returned));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            requireActivity().runOnUiThread(() -> {
                loanList = loans;
                displayLoansSorted();
            });
        }).start();
    }

    private void displayLoansSorted() {
        Collections.sort(loanList, Comparator.comparingInt(Loan::getId));
        updateTable(loanList);
    }

    private void displayFilteredLoans() {
        List<Loan> filtered = new ArrayList<>();
        for (Loan loan : loanList) {
            if (!loan.isReturned()) {
                filtered.add(loan);
            }
        }
        Collections.sort(filtered, Comparator.comparing(Loan::getReturnDate));
        updateTable(filtered);
    }

    private void updateTable(List<Loan> loans) {
        tableLayout.removeAllViews();

        TableRow header = new TableRow(getContext());
        String[] headers = {"대출번호", "사용자", "ISBN", "대출일", "반납일", "반납여부"};
        for (String h : headers) {
            TextView cell = createCell(h, true);
            header.addView(cell);
        }
        tableLayout.addView(header);

        for (Loan loan : loans) {
            TableRow row = new TableRow(getContext());
            row.addView(createCell(String.valueOf(loan.getId()), false));
            row.addView(createCell(loan.getUserId(), false));
            row.addView(createCell(loan.getIsbn(), false));
            row.addView(createCell(loan.getLoanDate(), false));
            row.addView(createCell(loan.getReturnDate(), false));
            row.addView(createCell(loan.isReturned() ? "O" : "X", false));
            tableLayout.addView(row);
        }
    }

    private TextView createCell(String text, boolean isHeader) {
        TextView cell = new TextView(getContext());
        cell.setText(text);
        cell.setPadding(16, 8, 16, 8);
        cell.setTextSize(isHeader ? 16 : 14);
        return cell;
    }

    static class Loan {
        private int id;
        private String userId;
        private String isbn;
        private String loanDate;
        private String returnDate;
        private boolean returned;

        public Loan(int id, String userId, String isbn, String loanDate, String returnDate, boolean returned) {
            this.id = id;
            this.userId = userId;
            this.isbn = isbn;
            this.loanDate = loanDate;
            this.returnDate = returnDate;
            this.returned = returned;
        }

        public int getId() { return id; }
        public String getUserId() { return userId; }
        public String getIsbn() { return isbn; }
        public String getLoanDate() { return loanDate; }
        public String getReturnDate() { return returnDate; }
        public boolean isReturned() { return returned; }
    }
}
