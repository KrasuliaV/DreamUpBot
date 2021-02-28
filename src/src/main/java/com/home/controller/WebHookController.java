package com.home.controller;

import com.home.bot.ChatBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RestController
public class WebHookController {
    private final ChatBot chatBot;

    public WebHookController(ChatBot chatBot){
        this.chatBot = chatBot;
    }

    @PostMapping(value = "/")
    public BotApiMethod<?> onWebhookUpdateReceived(@RequestBody Update update){
        log.info("from onWebhookUpdateReceived");
        return chatBot.onWebhookUpdateReceived(update);
    }
}
