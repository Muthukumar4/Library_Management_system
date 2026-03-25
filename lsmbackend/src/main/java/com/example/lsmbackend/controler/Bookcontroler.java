package com.example.lsmbackend.controler;

import com.example.lsmbackend.service.Bookservice;
import com.example.lsmbackend.model.Book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin("*")
public class Bookcontroler {

    @Autowired
    private Bookservice booksrv;


    @PostMapping("/add")
    public Book addBook(@RequestBody Book book) {
        return booksrv.addBook(book);
    }

    @GetMapping("/all")
    public List<Book> getAllBooks() {
        return booksrv.getAllBooks();
    }

    @GetMapping("/bookId/{id}")
    public Book getBookById(@PathVariable Long bookId) {
        return booksrv.getBookById(bookId);
    }
    @GetMapping("/title/{title}")
    public Book getBookByTitle(@PathVariable String title) {
        return booksrv.getBookByTitle(title);
    }

    @GetMapping("/barcode/{barcode}")
    public Book getBookByBarcode(@PathVariable String barcode) {
        return booksrv.getBookByBarcode(barcode);
    }

    @GetMapping("/{bookId}/availability")
    public Integer getAvailability(@PathVariable Long bookId) {
        return booksrv.getBookById(bookId).getAvailablecopies();
    }



}
