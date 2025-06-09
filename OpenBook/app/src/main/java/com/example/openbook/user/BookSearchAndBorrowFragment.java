package com.example.openbook.user;

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

import com.example.openbook.Book;
import com.example.openbook.BookAdapter;
import com.example.openbook.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BookSearchAndBorrowFragment extends Fragment {
    private View view;
    private EditText editSearch;
    private RadioGroup radioGroup;
    private ListView listView;
    private List<Book> bookList = new ArrayList<>();
    private BookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_user_book_search, container, false);

        editSearch = view.findViewById(R.id.edit_search);
        radioGroup = view.findViewById(R.id.radioGroup);
        listView = view.findViewById(R.id.list_books);

        adapter = new BookAdapter(getContext(), bookList);
        listView.setAdapter(adapter);

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchBooks();
                return true;
            }
            return false;
        });

        return view;
    }

    private void searchBooks() {
        String keyword = editSearch.getText().toString().trim();
        if (keyword.isEmpty()) return;

        String type;
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_title) type = "title";
        else if (selectedId == R.id.radio_author) type = "author";
        else type = "isbn";

        new Thread(() -> {
            try {
                String urlStr = "http://whdnd5725.dothome.co.kr/SearchBooks.php?type=" + URLEncoder.encode(type, "UTF-8") + "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

                requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
