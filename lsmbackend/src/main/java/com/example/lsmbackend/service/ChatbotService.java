package com.example.lsmbackend.service;

import com.example.lsmbackend.dto.ChatRequest;
import com.example.lsmbackend.dto.ChatResponse;
import com.example.lsmbackend.model.Book;
import com.example.lsmbackend.repository.Bookrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Set<String> NOISE_WORDS = new LinkedHashSet<>(Arrays.asList(
            "a", "an", "the", "i", "im", "i'm", "want", "need", "give", "me", "show", "find", "search",
            "suggest", "recommend", "recommended", "please", "can", "you", "tell", "which", "what", "where",
            "book", "books", "bokk", "bokk", "bk", "title", "titles", "about", "for", "on", "of", "to",
            "do", "have", "has", "is", "are", "there", "any", "available", "availability", "with", "and",
            "in", "at", "from", "by", "number", "no", "rack", "shelf", "shelfno", "rackno", "self", "selfno",
            "location", "locate", "place", "placed", "kept", "keep"
    ));

    @Autowired
    private Bookrepo bookrepo;

    public ChatResponse getReply(ChatRequest request) {
        String message = safeTrim(request != null ? request.getMessage() : null);
        if (message.isEmpty()) {
            return new ChatResponse(
                    "Please type a book title, topic, author, rack number, or shelf number question.",
                    "",
                    ""
            );
        }

        String normalizedMessage = normalizeText(message);
        String lastTopic = cleanQuery(request != null ? request.getLastTopic() : null);
        String lastBookTitle = safeTrim(request != null ? request.getLastBookTitle() : null);

        if (isLocationQuestion(normalizedMessage)) {
            return buildLocationReply(normalizedMessage, lastTopic, lastBookTitle);
        }

        if (isAvailabilityQuestion(normalizedMessage)) {
            return buildAvailabilityReply(normalizedMessage, lastTopic, lastBookTitle);
        }

        return buildSearchReply(normalizedMessage, lastTopic);
    }

    private ChatResponse buildSearchReply(String normalizedMessage, String lastTopic) {
        String topic = cleanQuery(normalizedMessage);
        if (topic.isEmpty()) {
            topic = lastTopic;
        }

        if (topic.isEmpty()) {
            return new ChatResponse(
                    "Please tell me a topic, title, or author. For example: java books, data science, or C programming.",
                    "",
                    ""
            );
        }

        List<Book> matches = findBestMatches(topic);
        if (matches.isEmpty()) {
            return new ChatResponse(
                    "No books found for \"" + topic + "\". Try a title, author, or topic like java, database, python, or networks.",
                    topic,
                    ""
            );
        }

        if (matches.size() == 1) {
            Book book = matches.get(0);
            return new ChatResponse(
                    formatSingleBookSummary(book),
                    topic,
                    safeTrim(book.getTitle())
            );
        }

        String reply = "I found these books for \"" + topic + "\": " +
                matches.stream()
                        .limit(5)
                        .map(this::formatCompactBookSummary)
                        .collect(Collectors.joining("; ")) +
                ". You can also ask: which rack, shelf number, or is it available?";

        return new ChatResponse(reply, topic, "");
    }

    private ChatResponse buildLocationReply(String normalizedMessage, String lastTopic, String lastBookTitle) {
        String query = resolveContextQuery(normalizedMessage, lastTopic, lastBookTitle);
        if (query.isEmpty()) {
            return new ChatResponse(
                    "Tell me the book title or topic first, then I can give you the rack number and shelf number.",
                    "",
                    ""
            );
        }

        List<Book> matches = findBestMatches(query);
        if (matches.isEmpty()) {
            return new ChatResponse(
                    "I couldn't find any book location for \"" + query + "\".",
                    query,
                    ""
            );
        }

        if (matches.size() == 1 || !lastBookTitle.isEmpty()) {
            Book book = pickBestBook(matches, lastBookTitle);
            return new ChatResponse(
                    formatLocationReply(book),
                    query,
                    safeTrim(book.getTitle())
            );
        }

        String reply = "For \"" + query + "\", I found these locations: " +
                matches.stream()
                        .limit(5)
                        .map(this::formatLocationLine)
                        .collect(Collectors.joining("; ")) +
                ". If you want one exact title, type the title name.";

        return new ChatResponse(reply, query, "");
    }

    private ChatResponse buildAvailabilityReply(String normalizedMessage, String lastTopic, String lastBookTitle) {
        String query = resolveContextQuery(normalizedMessage, lastTopic, lastBookTitle);
        if (query.isEmpty()) {
            return new ChatResponse(
                    "Tell me the book title or topic to check availability.",
                    "",
                    ""
            );
        }

        List<Book> matches = findBestMatches(query);
        if (matches.isEmpty()) {
            return new ChatResponse(
                    "I couldn't find a matching book for \"" + query + "\".",
                    query,
                    ""
            );
        }

        if (matches.size() == 1 || !lastBookTitle.isEmpty()) {
            Book book = pickBestBook(matches, lastBookTitle);
            return new ChatResponse(
                    formatAvailabilityReply(book),
                    query,
                    safeTrim(book.getTitle())
            );
        }

        String reply = "I found these matching books for \"" + query + "\": " +
                matches.stream()
                        .limit(5)
                        .map(this::formatAvailabilityLine)
                        .collect(Collectors.joining("; ")) +
                ". Ask with the exact title if you want one specific book.";

        return new ChatResponse(reply, query, "");
    }

    private List<Book> findBestMatches(String query) {
        String cleanedQuery = cleanQuery(query);
        if (cleanedQuery.isEmpty()) {
            return List.of();
        }

        Map<String, Book> orderedMatches = new LinkedHashMap<>();

        bookrepo.findByTitleIgnoreCase(cleanedQuery)
                .ifPresent(book -> orderedMatches.put(buildBookKey(book), book));

        for (Book book : bookrepo.findByTitleContainingIgnoreCase(cleanedQuery)) {
            orderedMatches.putIfAbsent(buildBookKey(book), book);
        }

        for (Book book : bookrepo.searchBooks(cleanedQuery)) {
            orderedMatches.putIfAbsent(buildBookKey(book), book);
        }

        if (orderedMatches.isEmpty()) {
            for (String token : cleanedQuery.split("\\s+")) {
                if (token.length() < 3) {
                    continue;
                }
                for (Book book : bookrepo.searchBooks(token)) {
                    orderedMatches.putIfAbsent(buildBookKey(book), book);
                }
            }
        }

        return new ArrayList<>(orderedMatches.values());
    }

    private Book pickBestBook(List<Book> matches, String preferredTitle) {
        if (preferredTitle != null && !preferredTitle.isBlank()) {
            for (Book book : matches) {
                if (preferredTitle.equalsIgnoreCase(safeTrim(book.getTitle()))) {
                    return book;
                }
            }
        }
        return matches.get(0);
    }

    private String resolveContextQuery(String normalizedMessage, String lastTopic, String lastBookTitle) {
        String directQuery = cleanQuery(normalizedMessage);
        if (!directQuery.isEmpty()) {
            return directQuery;
        }
        if (!lastBookTitle.isEmpty()) {
            return lastBookTitle;
        }
        return lastTopic;
    }

    private boolean isLocationQuestion(String normalizedMessage) {
        return normalizedMessage.contains("rack")
                || normalizedMessage.contains("shelf")
                || normalizedMessage.contains("self")
                || normalizedMessage.contains("location")
                || normalizedMessage.contains("where is")
                || normalizedMessage.contains("where can i find");
    }

    private boolean isAvailabilityQuestion(String normalizedMessage) {
        return normalizedMessage.contains("available")
                || normalizedMessage.contains("availability")
                || normalizedMessage.contains("do you have")
                || normalizedMessage.contains("in stock")
                || normalizedMessage.contains("can i get");
    }

    private String cleanQuery(String text) {
        String normalized = normalizeText(text);
        if (normalized.isEmpty()) {
            return "";
        }

        List<String> words = Arrays.stream(normalized.split("\\s+"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .filter(token -> !NOISE_WORDS.contains(token))
                .collect(Collectors.toList());

        return String.join(" ", words).trim();
    }

    private String normalizeText(String text) {
        String value = safeTrim(text).toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            return "";
        }
        return value
                .replace("rack no", "rack")
                .replace("rack number", "rack")
                .replace("shelf no", "shelf")
                .replace("shelf number", "shelf")
                .replace("self no", "shelf")
                .replace("self number", "shelf")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String formatSingleBookSummary(Book book) {
        String title = safeValue(book.getTitle(), "Untitled");
        String author = safeValue(book.getAuthor(), "Unknown author");
        String category = safeValue(book.getCategory(), "General");
        String rack = safeValue(book.getRackNumber(), "Not assigned");
        String shelf = safeValue(book.getShelfNumber(), "Not assigned");
        return title + " by " + author + " (" + category + "). Rack " + rack + ", Shelf " + shelf + ". " +
                availabilityText(book) + ".";
    }

    private String formatCompactBookSummary(Book book) {
        return safeValue(book.getTitle(), "Untitled") +
                " by " + safeValue(book.getAuthor(), "Unknown author") +
                " [Rack " + safeValue(book.getRackNumber(), "-") +
                ", Shelf " + safeValue(book.getShelfNumber(), "-") + "]";
    }

    private String formatLocationReply(Book book) {
        return "\"" + safeValue(book.getTitle(), "This book") + "\" is in Rack " +
                safeValue(book.getRackNumber(), "Not assigned") +
                ", Shelf " + safeValue(book.getShelfNumber(), "Not assigned") +
                ". " + availabilityText(book) + ".";
    }

    private String formatLocationLine(Book book) {
        return safeValue(book.getTitle(), "Untitled") +
                " - Rack " + safeValue(book.getRackNumber(), "-") +
                ", Shelf " + safeValue(book.getShelfNumber(), "-");
    }

    private String formatAvailabilityReply(Book book) {
        return "\"" + safeValue(book.getTitle(), "This book") + "\" " + availabilityText(book) +
                ". Rack " + safeValue(book.getRackNumber(), "Not assigned") +
                ", Shelf " + safeValue(book.getShelfNumber(), "Not assigned") + ".";
    }

    private String formatAvailabilityLine(Book book) {
        return safeValue(book.getTitle(), "Untitled") +
                " - " + availabilityText(book) +
                " (Rack " + safeValue(book.getRackNumber(), "-") +
                ", Shelf " + safeValue(book.getShelfNumber(), "-") + ")";
    }

    private String availabilityText(Book book) {
        Integer availableCopies = book.getAvailablecopies();
        if (availableCopies != null && availableCopies > 0) {
            return availableCopies == 1 ? "1 copy is available" : availableCopies + " copies are available";
        }
        return "currently not available";
    }

    private String buildBookKey(Book book) {
        if (book.getBookId() != null) {
            return "bookId:" + book.getBookId();
        }
        return "title:" + safeTrim(book.getTitle()).toLowerCase(Locale.ROOT);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeValue(String value, String fallback) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
