package com.home.bot.util;

import com.home.bot.handler.command.AdminCommand;
import com.home.bot.handler.command.UserCommand;
import com.home.model.Role;
import com.home.model.User;
import com.home.repository.JpaUserRepository;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TelegramUtil {

    private TelegramUtil() {
    }

    public static SendMessage createMessageTemplate(User user, String text) {
        return createMessageTemplateWithId(String.valueOf(user.getChatId()), text);
    }

    // Create boilerplate SendMessage
    public static SendMessage createMessageTemplateWithId(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.enableHtml(true);
        return message;
    }

    // Create button
    public static InlineKeyboardButton createInlineKeyboardButton(String text, String command) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(command);
        return inlineKeyboardButton;
    }

    // Create inline keyboard if it absent
    public static ReplyKeyboardMarkup createReplyKeyboardMarkup(Update update, JpaUserRepository userRepository) {
        if (getUser(update, userRepository).getRole().equals(Role.ADMIN)) {
            return createReplyKeyboardMarkup(AdminCommand.getAllAdminCommands(), Role.ADMIN);
        } else {
            return createReplyKeyboardMarkup(UserCommand.getAllUserCommands(), Role.USER);
        }
    }

    public static ReplyKeyboardMarkup createReplyKeyboardMarkup(List<String> commands, Role role) {
        ReplyKeyboardMarkup replyKeyboardMarkup = createInlineKeyboardMarkupTemplate();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardFirstRow.addAll(commands.subList(0, 3));
        if (role.equals(Role.USER)) {
            keyboardSecondRow.addAll(commands.subList(3, commands.size()));
        } else {
            keyboardSecondRow.add(commands.get(commands.size() - 1));
        }
        replyKeyboardMarkup.setKeyboard(List.of(keyboardFirstRow, keyboardSecondRow));
        return replyKeyboardMarkup;

    }

    public static ReplyKeyboardMarkup createInlineKeyboardMarkupTemplate() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }

    public static InlineKeyboardMarkup createUserInlineKeyboardMarkupWithOneButton(String buttonText, String callBackQuery) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton(buttonText, callBackQuery));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        return inlineKeyboardMarkup;
    }

    public static User getUser(Update update, JpaUserRepository userRepository) {

        final Long chatId;
        final String userName;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
        } else {
            userName = update.getCallbackQuery().getFrom().getFirstName() + " " + update.getCallbackQuery().getFrom().getLastName();
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }
        return userRepository.getByChatId(chatId)
                .orElseGet(() -> userRepository.save(new User(chatId, userName)));
    }

    public static String getTestResult(List<String> results) {
        Optional<Map.Entry<String, Long>> maxEntry = results.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max((entryOne, entryTwo) -> entryOne.getValue() > entryTwo.getValue() ? 1 : -1);
        if (maxEntry.isPresent()) {
            return maxEntry.get().getKey();
        } else {
            Collections.sort(results);
            return results.get(0);
        }
    }

    @SneakyThrows
    public static String returnTextFromFile(String fileName) {
        String filePath = String.format("src/main/resources/static/images/result/%s", fileName);
        Path path = Paths.get(filePath);
        List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (String line : allLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

}
