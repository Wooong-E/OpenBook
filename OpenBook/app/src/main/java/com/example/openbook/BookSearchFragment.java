package com.example.openbook;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public abstract class BookSearchFragment extends Fragment {
    protected View view;
    protected EditText et_Search;
    protected RadioGroup radioGroup;
    protected ListView listView;
    protected List<Book> bookList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(getLayoutId(), container, false);

        et_Search = view.findViewById(R.id.et_search);
        radioGroup = view.findViewById(R.id.radioGroup);
        listView = view.findViewById(R.id.list_books);

        listView.setAdapter(getAdapter());

        et_Search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchBooks();
                return true;
            }
            return false;
        });

        return view;
    }

    protected void searchBooks() {
        String keyword = et_Search.getText().toString().trim();
        if (keyword.isEmpty()) return;

        String type;
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_title) type = "title";
        else if (selectedId == R.id.radio_author) type = "author";
        else type = "isbn";

        new Thread(() -> {
            try {
                String urlStr = "http://whdnd5725.dothome.co.kr/SearchBooks.php?type=" + URLEncoder.encode(type, "UTF-8") +
                        "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                JSONArray arr = new JSONArray(result.toString());
                bookList.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject bookJson = arr.getJSONObject(i);
                    String title = bookJson.getString("title");
                    String author = bookJson.getString("author");
                    String isbn = bookJson.getString("isbn");
                    bookList.add(new Book(title, author, isbn));
                }

                requireActivity().runOnUiThread(() -> getAdapter().notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    protected abstract int getLayoutId();
    protected abstract BookAdapter getAdapter();
}
