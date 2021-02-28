package com.home.bot.handler.command;

import com.home.bot.State;
import com.home.bot.handler.QuizHandler;
import com.home.bot.handler.RegistrationHandler;
import com.home.bot.util.TelegramUtil;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.home.bot.util.TelegramUtil.createUserInlineKeyboardMarkupWithOneButton;

@Slf4j
public enum UserCommand {
    CHANGE_USER_INFO("Змінити дані"){
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, String.format("Вам потрібна допомога, %s?", user.getName()));
            sendMessage.setReplyMarkup(createUserInlineKeyboardMarkupWithOneButton("Змінити ім'я", RegistrationHandler.NAME_CHANGE));
            return sendMessage;
        }
    },

    QUIZ_RESTART("Почати тест"){
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, String.format("%s, Ви дійсно хочете почати тест спочатку?", user.getName()));
            sendMessage.setReplyMarkup(createUserInlineKeyboardMarkupWithOneButton("Почати тест", QuizHandler.QUIZ_START));
            return sendMessage;
        }
    },

    MOTIVATION("Отримати \nмотивацію"){
        @Override
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            log.info("from Give motivation handle");
            SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, String.format("%s, введіть число від 1 до 50", user.getName()));
            user.setBotState(State.GIVE_MOTIVATION);
            jpaUserRepository.save(user);
            return sendMessage;
        }
    },

    GIVE_CONTACTS("Я ХОЧУ СВОЮ КАРТУ БАЖАНЬ!"){
        public BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository) {
            return TelegramUtil.createMessageTemplate(user, String.format("%s, вітаємо!\n" +
                    "Це перший важливий крок до Вашої <b>МРІЇ</b>!\n" +
                    "Ви вожете подзвонити чи написати за номером <b>+380971286806</b>\n" +
                    "або заповнити форму на сайті: " +
                    "https://dreamup.online/", user.getName()));
        }
    };

    String userCommand;

    UserCommand(String userCommand){
        this.userCommand = userCommand;
    }

    public String getUserCommand() {
        return userCommand;
    }

    public static List<String> getAllUserCommands(){
        return Stream.of(UserCommand.values())
                .map(com -> com.userCommand)
                .collect(Collectors.toList());
    }

    public abstract BotApiMethod<?> handle(User user, String message, JpaUserRepository jpaUserRepository);
}
