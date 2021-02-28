package com.home.bot;

import com.home.bot.util.TelegramUtil;
import com.home.repository.JpaUserRepository;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;

@Slf4j
@Setter
@Configuration
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramWebhookBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.webHookPath}")
    private String webHookPath;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }

    private final UpdateReceiver updateReceiver;

    private final JpaUserRepository userRepository;

    public ChatBot(UpdateReceiver updateReceiver, JpaUserRepository userRepository) {
        this.updateReceiver = updateReceiver;
        this.userRepository = userRepository;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        log.info("from onWebhookUpdateReceived");

        BotApiMethod<?> messagesToSend = updateReceiver.handle(update);
        if (messagesToSend != null) {
            SendMessage sendMessage = (SendMessage) messagesToSend;
            if (sendMessage.getReplyMarkup() == null) {
                sendMessage.setReplyMarkup(TelegramUtil.createReplyKeyboardMarkup(update, userRepository));
            }
        }
        return messagesToSend;
    }

    @SneakyThrows
    public void sendPhoto(String chatId, String photoNumber) {
        log.info("from sendPhoto");
        File image = ResourceUtils.getFile(String.format("src/main/resources/static/images/motivation/%s.jpeg", photoNumber));
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(image));
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption("Your motivation for today");
        try {
            execute(sendPhoto);
        } catch (TelegramApiRequestException e) {
            log.error(e.getApiResponse() + " " + e.getErrorCode());
        }
    }

    @SneakyThrows
    public void sendDocument(String chatId, String fileName) {
        log.info("from sendDocument");
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setCaption("Your result");
        File file = ResourceUtils.getFile(String.format("src/main/resources/static/images/result/%s", fileName));
        log.info(file.getName());
        sendDocument.setDocument(new InputFile(file));
        execute(sendDocument);
    }

}
