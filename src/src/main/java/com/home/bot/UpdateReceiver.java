package com.home.bot;

import com.home.bot.handler.Handler;
import com.home.bot.handler.command.AdminCommand;
import com.home.bot.handler.command.UserCommand;
import com.home.bot.util.TelegramUtil;
import com.home.model.Role;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@PropertySource("classpath:telegram.properties")
public class UpdateReceiver {

    private final List<Handler> handlers;

    private final JpaUserRepository userRepository;

    @Value("${admin.chatId}")
    private Long adminId;

    public UpdateReceiver(List<Handler> handlers, JpaUserRepository userRepository) {
        this.handlers = handlers;
        this.userRepository = userRepository;
    }

    public BotApiMethod<?> handle(Update update) {
        try {
            if (isMessageWithText(update)) {
                log.info("from textMessage handle");
                final Message message = update.getMessage();

                User user = TelegramUtil.getUser(update, userRepository);
                if (user.getChatId().equals(adminId)) {
                    user.setRole(Role.ADMIN);
                }
                if (user.getRole().equals(Role.ADMIN) && AdminCommand.getAllAdminCommands().contains(message.getText())) {
                    return getHandlerByAdminCommand(message.getText()).handle(user, message.getText(), userRepository);
                }
                if (UserCommand.getAllUserCommands().contains(message.getText())) {
                    return getHandlerByUserCommand(message.getText()).handle(user, message.getText(), userRepository);
                }
                return getHandlerByState(user.getBotState()).handle(user, message.getText(), userRepository);
            } else if (update.hasCallbackQuery()) {
                log.info("from callbackQuery handle");
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                User user = TelegramUtil.getUser(update, userRepository);
                if (user.getChatId().equals(adminId)) {
                    user.setRole(Role.ADMIN);
                }
                return getHandlerByCallBackQuery(callbackQuery.getData()).handle(user, callbackQuery.getData(), userRepository);
            }

            throw new UnsupportedOperationException();
        } catch (UnsupportedOperationException ex) {
            return null;
        }
    }

    private Handler getHandlerByState(State state) {
        return handlers.stream()
                .filter(h -> h.operatedBotState() != null)
                .filter(h -> h.operatedBotState().contains(state))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private Handler getHandlerByCallBackQuery(String query) {
        return handlers.stream()
                .filter(h -> h.operatedCallBackQuery().stream()
                        .anyMatch(query::startsWith))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    public AdminCommand getHandlerByAdminCommand(String command) {
        return Stream.of(AdminCommand.values())
                .filter(com -> com.getAdminCommand().equals(command))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    public UserCommand getHandlerByUserCommand(String command) {
        return Stream.of(UserCommand.values())
                .filter(com -> com.getUserCommand().equals(command))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private boolean isMessageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }

}
