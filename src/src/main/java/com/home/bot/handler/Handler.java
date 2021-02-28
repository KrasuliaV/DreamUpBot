package com.home.bot.handler;

import com.home.bot.State;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.List;

public interface Handler {

    // common method for handle user actions
    BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository);

    // method for operating bot's State
    List<State> operatedBotState();

    // method for choosing CallBackQuery from Handler
    List<String> operatedCallBackQuery();
}
