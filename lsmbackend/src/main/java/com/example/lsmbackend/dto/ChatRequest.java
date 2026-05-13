package com.example.lsmbackend.dto;

public class ChatRequest {
    private String message;
    private String lastTopic;
    private String lastBookTitle;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, String lastTopic, String lastBookTitle) {
        this.message = message;
        this.lastTopic = lastTopic;
        this.lastBookTitle = lastBookTitle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLastTopic() {
        return lastTopic;
    }

    public void setLastTopic(String lastTopic) {
        this.lastTopic = lastTopic;
    }

    public String getLastBookTitle() {
        return lastBookTitle;
    }

    public void setLastBookTitle(String lastBookTitle) {
        this.lastBookTitle = lastBookTitle;
    }
}
