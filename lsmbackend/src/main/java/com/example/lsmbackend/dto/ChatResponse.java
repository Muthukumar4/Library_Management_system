package com.example.lsmbackend.dto;

public class ChatResponse {
    private String reply;
    private String matchedTopic;
    private String matchedBookTitle;

    public ChatResponse() {
    }

    public ChatResponse(String reply) {
        this.reply = reply;
    }

    public ChatResponse(String reply, String matchedTopic, String matchedBookTitle) {
        this.reply = reply;
        this.matchedTopic = matchedTopic;
        this.matchedBookTitle = matchedBookTitle;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getMatchedTopic() {
        return matchedTopic;
    }

    public void setMatchedTopic(String matchedTopic) {
        this.matchedTopic = matchedTopic;
    }

    public String getMatchedBookTitle() {
        return matchedBookTitle;
    }

    public void setMatchedBookTitle(String matchedBookTitle) {
        this.matchedBookTitle = matchedBookTitle;
    }
}
