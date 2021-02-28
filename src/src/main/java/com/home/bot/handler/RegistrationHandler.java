package com.home.bot.handler;

import com.home.bot.State;
import com.home.bot.util.TelegramUtil;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

import static com.home.bot.util.TelegramUtil.*;

@Slf4j
@Component
public class RegistrationHandler implements Handler {
    //Store CallBackQuery for RegistrationHandler
    public static final String NAME_ACCEPT = "/enter_name_accept";
    public static final String NAME_CHANGE = "/enter_name";
    public static final String NAME_CHANGE_CANCEL = "/enter_name_cancel";
    public static final String NUMBER_ACCEPT = "/enter_number_accept";
    public static final String NUMBER_CHANGE = "/enter_number";
    public static final String NUMBER_CHANGE_CANCEL = "/enter_number_cancel";

    @Override
    public BotApiMethod<?> handle(User user, String message, JpaUserRepository userRepository) {
        log.info("from handle");

        // Check message type
        if (message.equalsIgnoreCase(NUMBER_ACCEPT) || message.equalsIgnoreCase(NUMBER_CHANGE_CANCEL)) {
            return accept(user, userRepository);
        } else if (message.equalsIgnoreCase(NAME_CHANGE)) {
            return changeName(user, userRepository);
        } else if (message.equalsIgnoreCase(NUMBER_CHANGE)) {
            return changeNumber(user, userRepository);
        }
        if (user.getBotState().equals(State.ENTER_NAME)) {
            return checkName(user, message, userRepository);
        } else {
            return checkNumber(user, message, userRepository);
        }
    }

    private BotApiMethod<?> accept(User user, JpaUserRepository userRepository) {
        user.setBotState(State.NONE);
        userRepository.save(user);
        SendMessage sendMessage = TelegramUtil.createMessageTemplate(user,
                String.format("Ваші ім'я і телефон збережені як: %s %s", user.getName(), user.getTelNumber()));
        sendMessage.setReplyMarkup(createUserInlineKeyboardMarkupWithOneButton("Почати ТЕСТ", QuizHandler.QUIZ_START));
        return sendMessage;
    }

    private BotApiMethod<?> checkName(User user, String message, JpaUserRepository userRepository) {
        user.setName(message);
        userRepository.save(user);
        SendMessage sendMessage = TelegramUtil.createMessageTemplate(user,
                String.format("Ви ввели ім'я: <b>%s</b>\n" +
                        "Якщо це правильне ім'я - натисніть кнопку.\n +" +
                        "Або введіть нове ім'я", user.getName()));
        sendMessage.setReplyMarkup(createUserInlineKeyboardMarkupWithOneButton("Прийняти", NUMBER_CHANGE));
        return sendMessage;
    }

    private BotApiMethod<?> changeName(User user, JpaUserRepository userRepository) {
        user.setBotState(State.ENTER_NAME);
        userRepository.save(user);
        SendMessage sendMessage = TelegramUtil.createMessageTemplate(user,
                String.format("Ваше поточне ім'я: <b>%s</b>\n" +
                        "Ввeдіть нове ім'я або натисніть кнопку", user.getName()));
        sendMessage.setReplyMarkup(createUserInlineKeyboardMarkupWithOneButton("Продовжити", NUMBER_ACCEPT));
        return sendMessage;
    }

    private BotApiMethod<?> checkNumber(User user, String message, JpaUserRepository userRepository) {
        user.setTelNumber(message);
        userRepository.save(user);
        SendMessage sendMessage = TelegramUtil.createMessageTemplate(user,
                String.format("Ви ввели телефон: <b>%s</b>\n" +
                        "Якщо це правильний номер - натисніть кнопку.\n +" +
                        "Або введіть новий номер", user.getTelNumber()));
        sendMessage.setReplyMarkup(createUserInlineKeyboardMarkupWithOneButton("Прийняти", NUMBER_ACCEPT));
        return sendMessage;
    }

    private BotApiMethod<?> changeNumber(User user, JpaUserRepository userRepository) {
        log.info("from method changeNumber");
        user.setBotState(State.ENTER_PHONE);
        userRepository.save(user);

        SendMessage sendMessage = TelegramUtil.createMessageTemplate(user,
                String.format("Ваш поточний номер: <b>%s</b>\n" +
                        "Введіть новий номер або натисніть кнопку", user.getTelNumber()));
        sendMessage.setReplyMarkup(createUserInlineKeyboardMarkupWithOneButton("Продовжити", NUMBER_ACCEPT));
        return sendMessage;
    }

    @Override
    public List<State> operatedBotState() {
        return List.of(State.ENTER_PHONE, State.ENTER_NAME);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(NAME_ACCEPT, NAME_CHANGE, NAME_CHANGE_CANCEL, NUMBER_ACCEPT, NUMBER_CHANGE, NUMBER_CHANGE_CANCEL);
    }
}
