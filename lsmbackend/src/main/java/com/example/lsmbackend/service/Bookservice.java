package com.example.lsmbackend.service;

import com.example.lsmbackend.model.Book;
import com.example.lsmbackend.repository.Bookrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Bookservice {

    @Autowired
    private Bookrepo bookrepo;

    public Book addBook(Book book) {
        book.setAvailablecopies(book.getTotalcopies());
        return bookrepo.save(book);
    }

    public List<Book> getAllBooks() {
        return bookrepo.findAll();
    }

    public Book getBookByTitle(String title) {
        return bookrepo.findByTitle(title).orElse(null);
    }

    public Book getBookByBarcode(String barcode) {
        return bookrepo.findByBarcode(barcode).orElse(null);
    }
    public void reduceAvailableCopies(Long bookId) {
        Book book = bookrepo.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailablecopies() > 0) {
            book.setAvailablecopies(book.getAvailablecopies() - 1);
            bookrepo.save(book);
        }
    }
    public void increaseAvailableCopies(Long bookId) {
        Book book = bookrepo.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailablecopies() < book.getTotalcopies()) {
            book.setAvailablecopies(book.getAvailablecopies() + 1);
            bookrepo.save(book);
        }
    }

    public Book getBookById(Long bookId) {
        return bookrepo.findByBookId(bookId).orElse(null);
    }
}
