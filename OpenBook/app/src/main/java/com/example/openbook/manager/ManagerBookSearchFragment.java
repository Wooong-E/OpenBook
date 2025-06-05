package com.example.openbook.manager;

import com.example.openbook.BookAdapter;
import com.example.openbook.BookSearchFragment;
import com.example.openbook.R;

public class ManagerBookSearchFragment extends BookSearchFragment {
    private BookAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_book_search;
    }
    @Override
    protected BookAdapter getAdapter() {
        if (adapter == null) adapter = new BookAdapter(getContext(), bookList);
        return adapter;
    }
}