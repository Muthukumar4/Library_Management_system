package com.example.lsmbackend.repository;

import com.example.lsmbackend.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Bookrepo extends JpaRepository<Book,Long> {

    Optional<Book> findByTitle(String title);
    List<Book> findByTitleContainingIgnoreCase(String title);

    Optional<Book> findByTitleIgnoreCase(String title);

    Optional<Book> findByBarcode(String barcode);

    Optional<Book> findByBookId(Long bookId);

    @Query("SELECT SUM(b.Availablecopies) FROM Book b")
    Long totalAvailableBooks();
}
