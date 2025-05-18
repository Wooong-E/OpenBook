package com.example.openbook;

public class Book {
    private String title, author, isbn;
    private int totalCount, availableCount;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCount = -1; // 초기값 (불러오기 전)
        this.availableCount = -1;
    }


    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
