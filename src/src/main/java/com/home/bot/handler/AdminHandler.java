package com.home.bot.handler;

import com.home.bot.State;
import com.home.bot.util.TelegramUtil;
import com.home.model.Role;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.home.bot.State.*;
import static com.home.bot.handler.command.AdminCommand.*;

@Slf4j
@Component
public class AdminHandler implements Handler {

    @Override
    public BotApiMethod<?> handle(User user, String message, JpaUserRepository userRepository) {
        log.info("from admin handle");

        if (user.getBotState().equals(ADMIN_CHANGE_ROLE)) {
            return changeRoleCommand(user, message, userRepository);
        } else if (user.getBotState().equals(ADMIN_DELETE_USER)) {
            return deleteUserCommand(user, message, userRepository);
        } else if (user.getBotState().equals(ADMIN_CHECK_USER_FROM_TO)) {
            return getUserFromToCommand(user, message, userRepository);
        } else {
            return DELETE_USER.handle(user, message, userRepository);
        }
    }

    private BotApiMethod<?> getUserFromToCommand(User user, String message, JpaUserRepository userRepository) {
        log.info("from admin getUserFromTo");
        String[] fromTo = message.split(" ");

        if (fromTo.length == 2
                && fromTo[0].matches("[\\d]+")
                && fromTo[1].matches("[\\d]+")
                && Long.parseLong(fromTo[0]) < Long.parseLong(fromTo[1])) {
            Optional<User> opUserFrom = userRepository.getById(Long.parseLong(fromTo[0]));
            Optional<User> opUserTo = userRepository.getById(Long.parseLong(fromTo[1]));

            if (opUserFrom.isPresent() && opUserTo.isPresent()) {
                List<User> list = userRepository.getUsersByIdBetween(Long.parseLong(fromTo[0]), Long.parseLong(fromTo[1]));
                StringBuilder sb = new StringBuilder();
                list.forEach(u -> sb.append(u.toString()).append("\n"));
                SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, sb.toString());
                user.setBotState(NONE);
                userRepository.save(user);
                return sendMessage;
            } else {
                return CHECK_USER_FROM_TO.handle(user, message, userRepository);
            }
        } else {
            return CHECK_USER_FROM_TO.handle(user, message, userRepository);
        }
    }

    private BotApiMethod<?> changeRoleCommand(User user, String message, JpaUserRepository userRepository) {
        log.info("from admin changeRoleCommand");
        if (message.matches("[\\d]+")) {
            Optional<User> opUser = userRepository.getById(Long.parseLong(message));
            if (opUser.isPresent()) {
                User userForChange = opUser.get();
                userRepository.save(changeRole(userForChange));
                SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, "The role have been changed");
                user.setBotState(State.NONE);
                userRepository.save(user);
                return sendMessage;
            } else {
                return CHANGE_ROLE.handle(user, message, userRepository);
            }
        } else {
            return CHANGE_ROLE.handle(user, message, userRepository);
        }
    }

    private User changeRole(User user) {
        if (user.getRole().equals(Role.USER)) {
            user.setRole(Role.ADMIN);
        } else user.setRole(Role.USER);
        return user;
    }

    private BotApiMethod<?> deleteUserCommand(User user, String message, JpaUserRepository userRepository) {
        log.info("from admin deleteUserCommand");
        if (message.matches("[\\d]+")) {
            Optional<User> opUser = userRepository.getById(Long.parseLong(message));
            if (opUser.isPresent()) {
                User userForDelete = opUser.get();
                userRepository.delete(userForDelete);
                SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, "The user have been deleted");
                user.setBotState(NONE);
                return sendMessage;
            } else {
                return DELETE_USER.handle(user, message, userRepository);
            }
        } else {
            return DELETE_USER.handle(user, message, userRepository);
        }
    }

    @Override
    public List<State> operatedBotState() {
        return List.of(ADMIN_CHANGE_ROLE, ADMIN_DELETE_USER, ADMIN_CHECK_USER_FROM_TO);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
