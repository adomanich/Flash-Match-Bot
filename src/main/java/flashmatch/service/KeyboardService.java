package flashmatch.service;

import flashmatch.entity.Interest;
import flashmatch.entity.User;
import flashmatch.state.StateController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static flashmatch.util.CallBackConstant.*;

@Service
public class KeyboardService {

    @Autowired
    private InterestService interestService;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageSenderService messageSenderService;

    public void addExistedInterestButtons(long chatId, StateController stateController, int messageId) {
        var message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText("Interests:");
        message.setMessageId(messageId);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<Interest> interests = interestService.getAllInterests();
        if (interests.isEmpty()) {
            messageSenderService.sendMessageToConcreteChat(chatId, "Sorry there is no one interest");
        } else {
            interests.forEach(interest -> rowsInline.add(List.of(createButton(interest.getName(), interest.getName() + CALL_BACK_ENDING))));
            rowsInline.add(List.of(createButton("Back", BACK)));
            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);
            messageSenderService.sendEditMessage(message);
        }
    }

    public void addExistedUsersButtons(long chatId, int messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId(messageId);
        message.setText("Users:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            messageSenderService.sendMessageToConcreteChat(chatId, "Sorry there is no one user");
        } else {
            users.forEach(interest -> rowsInline.add(List.of(createButton(interest.getUserName(), interest.getUserName() + CALL_BACK_ENDING))));
            rowsInline.add(List.of(createButton("Back", BACK)));
            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);
            messageSenderService.sendEditMessage(message);
        }
    }

    public void addButtons(long chatId) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Options:");
        Map<String, String> buttonMap = Map.of("Add New Interest", ADD_NEW_INTEREST, "Remove Interest", REMOVE_INTEREST, "Remove User", REMOVE_USER, "Show All Interest", GET_INTERESTS);
        InlineKeyboardMarkup markupInline = createInlineKeyboard(buttonMap);
        message.setReplyMarkup(markupInline);
        messageSenderService.sendSimpleMessage(message);
    }

    public void addButtons(long chatId, int messageId) {
        var message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("Options:");
        LinkedHashMap<String, String> buttonMap = new LinkedHashMap<>(Map.of("Add New Interest", ADD_NEW_INTEREST, "Remove Interest", REMOVE_INTEREST, "Remove User", REMOVE_USER, "Show All Interest", GET_INTERESTS));
        InlineKeyboardMarkup markupInline = createInlineKeyboard(buttonMap);
        message.setReplyMarkup(markupInline);
        messageSenderService.sendEditMessage(message);
    }

    public void addMenuButtons(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Меню:");
        LinkedHashMap<String, String> menu = new LinkedHashMap<>(Map.of("Interests", CHOSE_INTERESTS, "Notifications", NOTIFICATIONS, "Help", HELP));
        InlineKeyboardMarkup markupInline = createInlineKeyboard(menu);
        message.setReplyMarkup(markupInline);
        messageSenderService.sendSimpleMessage(message);
    }

    public void addMenuButtons(long chatId, int messageId) {
        var message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("Меню:");
        LinkedHashMap<String, String> menu = new LinkedHashMap<>(Map.of("Interests", CHOSE_INTERESTS, "Notifications", NOTIFICATIONS, "Help", HELP));
        InlineKeyboardMarkup markupInline = createInlineKeyboard(menu);
        message.setReplyMarkup(markupInline);
        messageSenderService.sendEditMessage(message);
    }


    private InlineKeyboardMarkup createInlineKeyboard(Map<String, String> buttons) {
        var markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        buttons.forEach((key, value) -> rowsInline.add(List.of(createButton(key, value))));
        return markupInline.setKeyboard(rowsInline);
    }

    public void addNotificationSenderButtons(long chatId, int messageId) {
        var message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText("Нотифікації:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(createButton("Turn on", TURN_ON), createButton("Turn off", TURN_OFF)));
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        messageSenderService.sendEditMessage(message);
    }

    public void addChooseButtons(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("З'єднати мене:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(createButton("Yes", YES), createButton("NO", NO)));
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        messageSenderService.sendSimpleMessage(message);
    }

    private InlineKeyboardButton createButton(String text, String callBackText) {
        return new InlineKeyboardButton().setText(text).setCallbackData(callBackText);
    }
}
