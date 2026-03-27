package com.example.lsmbackend.service;

import com.example.lsmbackend.model.Book;
import com.example.lsmbackend.repository.Bookrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private Bookrepo bookrepo;

    public String getReply(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Please type a book title or topic.";
        }

        String msg = userMessage.trim().toLowerCase(Locale.ROOT);

        if (msg.contains("do you have") || msg.contains("available")) {
            String title = extractTitle(msg);
            return checkBookAvailability(title);
        }

        String topic = extractSearchTopic(msg);
        if (topic.isBlank()) {
            return "Please type a book title or topic.";
        }

        List<Book> books = bookrepo.searchBooks(topic);
        if (books.isEmpty()) {
            return "No books found for \"" + topic + "\".";
        }

        return "Here are some books: " +
                books.stream()
                        .limit(5)
                        .map(Book::getTitle)
                        .collect(Collectors.joining(", "));
    }

    private String extractSearchTopic(String msg) {
        return msg.toLowerCase(Locale.ROOT)
                .replace("suggest", "")
                .replace("recommend", "")
                .replace("show", "")
                .replace("find", "")
                .replace("search", "")
                .replace("give me", "")
                .replace("i want", "")
                .replace("need", "")
                .replace("books", "")
                .replace("book", "")
                .replace("about", "")
                .trim();
    }

    private String extractTitle(String msg) {
        return msg.toLowerCase(Locale.ROOT)
                .replace("do you have", "")
                .replace("is", "")
                .replace("available", "")
                .replace("book", "")
                .replace("title", "")
                .replace("?", "")
                .trim();
    }

    private String checkBookAvailability(String title) {
        if (title == null || title.isBlank()) {
            return "Please type the book title you want to check.";
        }

        Book exactMatch = bookrepo.findByTitleIgnoreCase(title).orElse(null);
        if (exactMatch != null) {
            Integer availableCopies = exactMatch.getAvailablecopies();
            if (availableCopies != null && availableCopies > 0) {
                return "\"" + exactMatch.getTitle() + "\" is available.";
            }
            return "\"" + exactMatch.getTitle() + "\" is currently not available.";
        }

        List<Book> matches = bookrepo.findByTitleContainingIgnoreCase(title);
        if (matches.isEmpty()) {
            return "I couldn't find a book titled \"" + title + "\".";
        }

        Book book = matches.get(0);
        Integer availableCopies = book.getAvailablecopies();
        if (availableCopies != null && availableCopies > 0) {
            return "\"" + book.getTitle() + "\" is available.";
        }
        return "\"" + book.getTitle() + "\" is currently not available.";
    }
}
