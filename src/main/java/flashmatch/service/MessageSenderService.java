package flashmatch.service;

import flashmatch.bot.FlashMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class MessageSenderService {

    @Autowired
    private FlashMatch flashMatch;

    public void sendMessageToConcreteChat(long chat_id, String text) {
        SendMessage sendMessage = new SendMessage()
                .setChatId(chat_id)
                .setText(text);
        sendSimpleMessage(sendMessage);
    }

    public void sendSimpleMessage(SendMessage message) {
        try {
            flashMatch.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendEditMessage(EditMessageText sendMessage) {
        try {
            flashMatch.execute(sendMessage);
        } catch (TelegramApiException e) {
            FlashMatch.getLogger().info("Can not to send message");
        }
    }
}
