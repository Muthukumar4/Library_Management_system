package com.example.lsmbackend.controler;

import com.example.lsmbackend.dto.ChatRequest;
import com.example.lsmbackend.dto.ChatResponse;
import com.example.lsmbackend.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatbotControler {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = chatbotService.getReply(request.getMessage());
        return new ChatResponse(reply);
    }
}
