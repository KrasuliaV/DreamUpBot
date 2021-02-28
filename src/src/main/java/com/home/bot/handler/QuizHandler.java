package com.home.bot.handler;

import com.home.bot.State;

import com.home.bot.handler.command.UserCommand;
import com.home.bot.util.TelegramUtil;
import com.home.model.Question;
import com.home.model.ResultEnum;
import com.home.model.User;
import com.home.repository.JpaQuestionRepository;
import com.home.repository.JpaUserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.home.bot.util.TelegramUtil.*;

@Slf4j
@Component
public class QuizHandler implements Handler {

    public static final String QUIZ_QUESTION = "/quiz_correct";
    public static final String LAST_QUIZ_QUESTION = "/last_quiz_question";
    public static final String QUIZ_START = "/quiz_start";

    private static final List<String> OPTIONS = List.of("A", "B", "C", "D");

    private final JpaQuestionRepository questionRepository;

    public QuizHandler(JpaQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public BotApiMethod<?> handle(User user, String message, JpaUserRepository userRepository) {
        log.info("from quizHandler handle");
        if (message.startsWith(QUIZ_QUESTION)) {
            return correctAnswer(user, message, userRepository);
        } else if (message.startsWith(LAST_QUIZ_QUESTION)) {
            return lastAnswer(user, message, userRepository);
        } else return startNewQuiz(user, message, userRepository);
    }

    private BotApiMethod<?> correctAnswer(User user, String message, JpaUserRepository userRepository) {
        log.info("from correctAnswer");
        String[] str = message.split(" ");
        String numberButton = str[2];
        user.getResultsList().add(numberButton);
        int currentScore = user.getCurrentTestNumber() + 1;
        log.info("from correctAnswer " + currentScore);
        user.setCurrentTestNumber(currentScore);
        userRepository.save(user);

        return nextQuestion(user, message, userRepository);
    }

    @SneakyThrows
    private BotApiMethod<?> lastAnswer(User user, String message, JpaUserRepository userRepository) {
        log.info("from lastAnswer");
        String[] str = message.split(" ");
        String numberButton = str[2];
        user.getResultsList().add(numberButton);
        String result = getTestResult(user.getResultsList());
        String resultName = ResultEnum.getResultName(result).name().toLowerCase();
        user.setLastResult(resultName);
        user.getResultsList().clear();
        user.setBotState(State.NONE);
        userRepository.save(user);

        // Create button for new test
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Спробувати ще раз?", QUIZ_START));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, returnTextFromFile(resultName));
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    private BotApiMethod<?> startNewQuiz(User user, String message, JpaUserRepository userRepository) {
        user.setCurrentTestNumber(0);
        user.setBotState(State.PLAYING_QUIZ);
        userRepository.save(user);

        return nextQuestion(user, message, userRepository);
    }

    private BotApiMethod<?> nextQuestion(User user, String message, JpaUserRepository userRepository) {
        List<Question> questionList = questionRepository.findAll();
        Question question;
        if (user.getCurrentTestNumber() < questionList.size()) {
            question = questionList.get(user.getCurrentTestNumber());
        } else {
            return UserCommand.QUIZ_RESTART.handle(user, message, userRepository);
        }

        List<String> options = new ArrayList<>(List.of(question.getOptionOne(), question.getOptionTwo(), question.getOptionThree(), question.getOptionFour()));

        // Create message
        StringBuilder sb = new StringBuilder();
        sb.append("<b>")
                .append(question.getQuestion())
                .append("</b>\n\n");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        // Create buttons row
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowTwo = new ArrayList<>();

        // Create message and write CallBackData on buttons
        for (int i = 0; i < options.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();

            final String callbackData = user.getCurrentTestNumber() == (questionList.size() - 1) ? LAST_QUIZ_QUESTION : QUIZ_QUESTION;
            button.setText(OPTIONS.get(i));
            button.setCallbackData(String.format("%s %d %d", callbackData, question.getId(), i));

            if (i < 2) {
                inlineKeyboardButtonsRowOne.add(button);
            } else {
                inlineKeyboardButtonsRowTwo.add(button);
            }
            sb.append("<b>")
                    .append(OPTIONS.get(i))
                    .append(".</b> ")
                    .append(options.get(i))
                    .append("\n");
        }
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne, inlineKeyboardButtonsRowTwo));
        SendMessage sendMessage = TelegramUtil.createMessageTemplate(user, sb.toString());
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    @Override
    public List<State> operatedBotState() {
        return List.of(State.PLAYING_QUIZ);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(QUIZ_START, QUIZ_QUESTION, LAST_QUIZ_QUESTION);
    }
}
