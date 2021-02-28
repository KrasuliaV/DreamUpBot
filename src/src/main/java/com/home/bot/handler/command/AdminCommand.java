package com.home.bot.handler.command;

import com.home.bot.State;
import com.home.bot.util.TelegramUtil;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public enum AdminCommand {
    ALL_USER("User's info") {
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            log.info("from User's info");
            List<User> list = jpaUserRepository.findAll();
            StringBuilder sb = new StringBuilder();
            list.forEach(u -> sb.append(u.toString()).append("\n"));
            return TelegramUtil.createMessageTemplate(user, sb.toString());
        }
    },

    LAST_TEN_USERS("Last ten") {
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            log.info("from Last ten");
            List<User> list = jpaUserRepository.findAll();
            SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, "");
            if (list.size() > 10) {
                StringBuilder sb = new StringBuilder();

                Collections.reverse(list);
                list.stream()
                        .limit(10)
                        .forEach(u -> sb.append(u.toString()).append("\n"));

                sendMessage.setText(sb.toString());
            } else {
                return ALL_USER.handle(user, message, jpaUserRepository);
            }
            return sendMessage;
        }
    },

    USER_QUANTITY("User's quantity") {
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            log.info("from User's quantity");
            return TelegramUtil.createMessageTemplate(user, String.valueOf(jpaUserRepository.findAll().size()));
        }
    },

    CHANGE_ROLE("Change role") {
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            log.info("from Change role");
            user.setBotState(State.ADMIN_CHANGE_ROLE);
            jpaUserRepository.save(user);
            return TelegramUtil.createMessageTemplate(user, "Enter user id for change ROLE");
        }
    },

    DELETE_USER("Delete user") {
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            log.info("from Delete user");
            user.setBotState(State.ADMIN_DELETE_USER);
            jpaUserRepository.save(user);
            return TelegramUtil.createMessageTemplate(user, "Enter user id for deleting");
        }
    },

    CHECK_USER_FROM_TO("Check user from to") {
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            log.info("from Check user from to");
            user.setBotState(State.ADMIN_CHECK_USER_FROM_TO);
            jpaUserRepository.save(user);
            return TelegramUtil.createMessageTemplate(user, "Enter user id from and id to for getting");
        }
    };

    String adminCommand;

    AdminCommand(String adminCommand) {
        this.adminCommand = adminCommand;
    }

    public String getAdminCommand() {
        return adminCommand;
    }

    public static List<String> getAllAdminCommands() {
        return Stream.of(AdminCommand.values())
                .map(com -> com.adminCommand)
                .collect(Collectors.toList());
    }

    public abstract BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository);
}
