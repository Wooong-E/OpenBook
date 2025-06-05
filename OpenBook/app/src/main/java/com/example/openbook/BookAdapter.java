package com.example.openbook;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {
    private Context context;
    private List<Book> bookList;

    public BookAdapter(Context context, List<Book> books) {
        super(context, 0, books);
        this.context = context;
        this.bookList = books;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);

        TextView title = convertView.findViewById(R.id.tv_title);
        TextView author = convertView.findViewById(R.id.tv_author);
        Button btnDetail = convertView.findViewById(R.id.btn_detail);


        Book book = bookList.get(position);
        title.setText(book.getTitle());
        author.setText(book.getAuthor());

        btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra("isbn", book.getIsbn());

            boolean isManager = context.getClass().getName().contains("manager");
            intent.putExtra("isManager", isManager);

            context.startActivity(intent);
        });


        return convertView;
    }
}
