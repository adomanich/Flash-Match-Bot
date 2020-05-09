package flashmatch.bot;

import flashmatch.entity.User;
import flashmatch.service.InterestService;
import flashmatch.service.UserService;
import flashmatch.state.UserChoseActivityState;
import flashmatch.state.StateController;
import flashmatch.userstrategy.Admin;
import flashmatch.userstrategy.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

@Component
@PropertySource("telegram.properties")
public class FlashMatch extends TelegramWebhookBot {

    private static final Logger LOGGER = LogManager.getLogger(FlashMatch.class);
    @Value("${flashmatch.bot.name}")
    private String botName;
    @Value("${flashmatch.bot.token}")
    private String botToken;
    @Value("${flashmatch.bot.path}")
    private String botPath;

    @Autowired
    private UserService userService;
    @Autowired
    private InterestService interestService;
    @Autowired
    private Admin admin;
    @Autowired
    private Client client;
    @Autowired
    private UserChoseActivityState userChoseActivityState;

    private boolean isMessage;

    public static Logger getLogger() {
        return LOGGER;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        Map<String, String> userData = getUserData(update);
        getLogger().info(Thread.currentThread().getName() + Thread.currentThread().getId() + "started work with " + userData.get("username"));
        long chat_id = Long.parseLong(userData.get("id"));
        StateController stateController = new StateController(new UserChoseActivityState());
        if (isUserAdmin(chat_id, userData.get("username"))) {
            flashmatch.userstrategy.User adminStrategy = admin;
            adminStrategy.doWork(update, stateController, isMessage, chat_id);
        } else {
            flashmatch.userstrategy.User userStrategy = client;
            userStrategy.doWork(update, stateController, isMessage, chat_id);
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    public void sendMessageToConcreteChat(long chat_id, String text) {
        SendMessage sendMessage = new SendMessage()
                .setChatId(chat_id)
                .setText(text);
        sendSimpleMessage(sendMessage);
    }

    public void sendSimpleMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendEditMessage(EditMessageText sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            FlashMatch.getLogger().info("Can not to send message");
        }
    }

    private Map<String, String> getUserData(Update update) {
        if (update.hasCallbackQuery()) {
            isMessage = false;
            org.telegram.telegrambots.meta.api.objects.User user = update.getCallbackQuery().getFrom();
            return Map.of("id", user.getId().toString(), "username", user.getUserName());
        } else {
            isMessage = true;
            Chat chat = update.getMessage().getChat();
            return Map.of("id", chat.getId().toString(), "username", chat.getUserName());
        }
    }

    private boolean isUserAdmin(long chatId, String userName) {
        User user = userService.getUsersByChatId(chatId);
        if (user == null) {
            return false;
        } else {
            return userName.equals("");
        }
    }
}
