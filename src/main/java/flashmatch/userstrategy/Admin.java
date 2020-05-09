package flashmatch.userstrategy;

import flashmatch.bot.FlashMatch;
import flashmatch.entity.Interest;
import flashmatch.manager.ButtonManager;
import flashmatch.service.InterestService;
import flashmatch.service.UserService;
import flashmatch.state.StateController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static flashmatch.util.CallBackConstant.*;

@Component
public class Admin implements User {

    @Autowired
    private InterestService interestService;
    @Autowired
    private UserService userService;
    @Autowired
    private FlashMatch flashMatch;
    @Autowired
    private ButtonManager buttonManager;

    @Override
    public void doWork(Update update, StateController stateController, boolean isMessage, long chatId) {
        if (!isMessage) {
            String message = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            switch (message) {
                case ADD_NEW_INTEREST:
                    flashMatch.sendMessageToConcreteChat(chatId, "Plese input new interest");
                    break;
                case REMOVE_INTEREST:
                    flashMatch.sendMessageToConcreteChat(chatId, "Please remove interest from list");
                    buttonManager.addExistedInterestButtons(chatId, stateController, messageId);
                    break;
                case GET_INTERESTS:
                    showAllInterests(chatId, stateController);
                    break;
                case BACK:
                    buttonManager.addButtons(chatId, messageId);
                    break;
                case REMOVE_USER:
                    buttonManager.addExistedUsersButtons(chatId, messageId);
                    break;
                default:
                    if (!checkRemoveInterestCallBack(update, chatId, stateController, messageId)) {
                        checkRemoveUserCallBack(update, chatId, stateController, messageId);
                    }
                    break;
            }
        } else {
            Message message = update.getMessage();
            if (message.getText().equals("/start")) {
                buttonManager.addButtons(chatId);
            } else {
                Interest interest = new Interest(message.getText());
                interestService.addInterest(interest);
                buttonManager.addButtons(chatId);
                FlashMatch.getLogger().info(message.getText() + " was added");
            }
        }
    }

    private void showAllInterests(long chatId, StateController stateController) {
        List<Interest> interests = interestService.getAllInterests();
        if (interests.isEmpty()) {
            flashMatch.sendMessageToConcreteChat(chatId, "Sorry there is no one interest");
        } else {
            String message = interests.stream()
                    .map(Interest::getName)
                    .collect(Collectors.joining("\n"));
            flashMatch.sendMessageToConcreteChat(chatId, message);
        }
    }

    private boolean checkRemoveInterestCallBack(Update update, long chatId, StateController stateController, int messageId) {
        String callBackMessage = update.getCallbackQuery().getData();
        Optional<Interest> result = interestService.getAllInterests()
                .stream()
                .filter(interest -> (interest.getName() + CALL_BACK_ENDING).equals(callBackMessage))
                .findAny();
        boolean isPresent = result.isPresent();
        if (isPresent) {
            interestService.delete(result.get());
            buttonManager.addExistedInterestButtons(chatId, stateController, messageId);
            FlashMatch.getLogger().info(result.get().getName() + " was deleted");
        }
        return isPresent;
    }

    private void checkRemoveUserCallBack(Update update, long chatId, StateController stateController, int messageId) {
        String callBackMessage = update.getCallbackQuery().getData();
        Optional<flashmatch.entity.User> result = userService.getAllUsers()
                .stream()
                .filter(user -> (user.getUserName() + CALL_BACK_ENDING).equals(callBackMessage))
                .findAny();
        if (result.isPresent()) {
            userService.delete(result.get());
            buttonManager.addExistedInterestButtons(chatId, stateController, messageId);
            FlashMatch.getLogger().info(result.get().getUserName() + " was deleted");
        }
    }
}
