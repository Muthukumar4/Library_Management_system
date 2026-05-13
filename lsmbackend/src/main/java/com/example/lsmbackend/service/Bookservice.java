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
        Integer totalCopies = book.getTotalcopies();
        if (totalCopies == null || totalCopies < 0) {
            throw new RuntimeException("Total copies must be 0 or more");
        }

        Integer availableCopies = book.getAvailablecopies();
        if (availableCopies == null) {
            availableCopies = totalCopies;
        }
        if (availableCopies < 0) {
            throw new RuntimeException("Available copies must be 0 or more");
        }
        if (availableCopies > totalCopies) {
            throw new RuntimeException("Available copies cannot exceed total copies");
        }

        book.setAvailablecopies(availableCopies);
        book.setAvailable(availableCopies > 0);
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
