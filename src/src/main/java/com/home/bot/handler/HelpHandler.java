package com.home.bot.handler;

import com.home.bot.ChatBot;
import com.home.bot.State;
import com.home.bot.handler.command.UserCommand;
import com.home.bot.util.TelegramUtil;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class HelpHandler implements Handler {

    private final ChatBot chatBot;

    public HelpHandler(@Lazy ChatBot chatBot) {
        this.chatBot = chatBot;
    }

    @Override
    public BotApiMethod<?> handle(User user, String message, JpaUserRepository userRepository) {
        if (user.getBotState().equals(State.GIVE_MOTIVATION)) {
            return giveMotivationPictures(user, message, userRepository);
        } else {
            return UserCommand.QUIZ_RESTART.handle(user, message, userRepository);
        }
    }

    public BotApiMethod<?> giveMotivationPictures(User user, String message, JpaUserRepository userRepository) {
        log.info("from giveMotivationPictures - " + message);
        log.info("from giveMotivationPictures - " + message.matches("\\d"));

        if (message.matches("[\\d]+")) {
            log.info("message.matches(d)");
            int pictureNumber = Integer.parseInt(message);
            if (pictureNumber > 0 && pictureNumber < 51) {
                log.info("pictureNumber > 0 && pictureNumber < 51");
                SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, "");
                user.setLastNumberMotivation(message);
                chatBot.sendPhoto(user.getChatId().toString(), message);
                user.setBotState(State.NONE);
                userRepository.save(user);

                return sendMessage;
            } else {
                return UserCommand.MOTIVATION.handle(user, message, userRepository);
            }
        } else {
            return UserCommand.MOTIVATION.handle(user, message, userRepository);
        }
    }

    @Override
    public List<State> operatedBotState() {
        return List.of(State.NONE, State.GIVE_MOTIVATION);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
