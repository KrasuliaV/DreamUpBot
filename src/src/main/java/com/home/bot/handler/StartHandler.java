package com.home.bot.handler;

import com.home.bot.State;
import com.home.bot.util.TelegramUtil;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;

@Component
public class StartHandler implements Handler {

    @Value("${bot.name}")
    private String botUserName;

    @Override
    public BotApiMethod<?> handle(User user, String message, JpaUserRepository userRepository) {

        SendMessage registrationMessage = TelegramUtil.createMessageTemplate(user,
                String.format("Вітаємо! Я <strong>%s</strong>\n" +
                        "Ведіть Ваше ім'я, щоб почати",
                        botUserName));
        user.setBotState(State.ENTER_NAME);
        userRepository.save(user);
        return registrationMessage;
    }

    @Override
    public List<State> operatedBotState() {
        return List.of(State.START);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
