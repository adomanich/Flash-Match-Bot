package flashmatch.userstrategy;

import flashmatch.bot.FlashMatch;
import flashmatch.entity.Interest;
import flashmatch.service.InterestService;
import flashmatch.service.KeyboardService;
import flashmatch.service.MessageSenderService;
import flashmatch.service.UserService;
import flashmatch.state.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.stream.Collectors;

import static flashmatch.util.CallBackConstant.*;

@Component
public class Client implements User {

    @Autowired
    @Qualifier("userPreSelectState")
    private UserChoseActivityState userChoseActivityState;
    @Autowired
    @Qualifier("userDoneChoiceState")
    private UserDoneChoice userDoneChoice;
    @Autowired
    @Qualifier("userWaitState")
    private UserWait userWait;
    @Autowired
    @Qualifier("userMatchState")
    private UserMatch userMatch;

    @Autowired
    private KeyboardService keyboardService;
    @Autowired
    private InterestService interestService;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageSenderService messageSenderService;
    @Autowired
    private StateController stateController;
    private long chatId;
    private Map<Long, List<Long>> matchedUsers = new HashMap<>();

    @Override
    public void doWork(Update update, boolean isMessage, long chatId) {
        stateController.log();
        this.chatId = chatId;
        if (!isMessage) {
            String callBackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            switch (callBackData) {
                case TURN_ON:
                    onTurnOnCallBack(messageId);
                    break;
                case TURN_OFF:
                    onTurnOffCallBack(chatId, messageId);
                    break;
                case CHOSE_INTERESTS:
                    interest(chatId, messageId);
                    break;
                case NOTIFICATIONS:
                    notification(chatId, messageId);
                    break;
                case HELP:
                    help(chatId);
                    break;
                case YES:
                    onYesCallback();
                    break;
                case NO:
                    onNoCallBack(chatId);
                    break;
                case BACK:
                    back(chatId, messageId);
                    break;
                default:
                    checkCallBack(update);
                    break;
            }
        } else {
            String message = update.getMessage().getText();
            if (message.equals("/start")) {
                start(chatId);
            } else {
                onDefault(chatId);
            }
        }
    }

    private void onTurnOnCallBack(int messageId) {
        flashmatch.entity.User user = userService.getUsersByChatId(chatId);
        if (user != null && !user.isNotification()) {
            user.setNotification(true);
            userService.update(user);
        }
        keyboardService.addMenuButtons(chatId, messageId);
        messageSenderService.sendMessageToConcreteChat(chatId, "Тепер ви отримуватимете нотифікації про з'єднання від людей, які шукають собі заняття :)");
    }

    private void onTurnOffCallBack(long chatId, int messageId) {
        flashmatch.entity.User user = userService.getUsersByChatId(chatId);
        if (user.isNotification()) {
            user.setNotification(false);
            userService.update(user);
        }
        keyboardService.addMenuButtons(chatId, messageId);
        messageSenderService.sendMessageToConcreteChat(chatId, "Тепер ви не отримуватимете нотифікації про з'єднання від людей, які шукають собі заняття." +
                "Вони автоматично з'являться коли ви захочете знайти когось");
    }

    private void onYesCallback() {
        matchPair();
    }

    private void matchPair() {
        matchedUsers.entrySet()
                .stream()
                .filter(current -> current.getValue()
                        .stream()
                        .anyMatch(c -> c.equals(chatId)))
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresentOrElse(id -> {
                    flashmatch.entity.User userWantMatch = userService.getUsersByChatId(chatId);
                    flashmatch.entity.User userCurrentlyWait = userService.getUsersByChatId(id);
                    matchTwoUsers(userWantMatch, userCurrentlyWait, interestService.getInterestById(userWantMatch.getInterestId()).getName());
                    matchedUsers.remove(id);
                }, () -> messageSenderService.sendMessageToConcreteChat(chatId, "Ми вже знайшли пару для цього солодня"));
    }

    private void onNoCallBack(long chatId) {
        matchedUsers.values()
                .stream()
                .filter(longs -> longs
                        .stream()
                        .anyMatch(c -> c.equals(chatId)))
                .findFirst()
                .ifPresent(element -> {
                    element.remove(chatId);
                    messageSenderService.sendMessageToConcreteChat(chatId, "Дякую за відповідь.");
                });
    }

    private void checkCallBack(Update update) {
        String callBackMessage = update.getCallbackQuery().getData();
        Optional<Interest> result = interestService.getInterestCorrespondingToCallBack(callBackMessage);
        if (result.isPresent()) {
            String interestOfCurrentUser = result.get().getName();
            flashmatch.entity.User user = getCurrentUserFromDataBase(chatId, result.get().getId(), update.getCallbackQuery().getFrom().getUserName());
            user.setInterestId(result.get().getId());
            stateController = new StateController(userDoneChoice);
            stateController.updateUserState(user);

            flashmatch.entity.User waitUser = userService.getWaitedUser(result.get().getId(), user.getChatId());
            if (!isSomebodyWaitOnMatch(waitUser, chatId)) {
                stateController = new StateController(userWait);
                stateController.updateUserState(user);
            } else {
                matchTwoUsers(user, waitUser, interestOfCurrentUser);
            }
        }
    }

    private flashmatch.entity.User getCurrentUserFromDataBase(long chatId, int interestId, String userName) {
        flashmatch.entity.User user = userService.getUsersByChatId(chatId);
        if (user == null) {
            user = new flashmatch.entity.User(chatId, interestId, stateController.getCurrentStateId(), userName);
            userService.addNew(user);
        }
        return user;
    }

    private boolean isSomebodyWaitOnMatch(flashmatch.entity.User user, long chatId) {
        return user != null && user.getChatId() != chatId;
    }

    private void matchTwoUsers(flashmatch.entity.User userOne, flashmatch.entity.User userTwo, String commonInterest) {
        FlashMatch.getLogger().info(Thread.currentThread().getName() + "mathed with users :" + userOne.getUserName() + " and " + userTwo.getUserName());
        stateController = new StateController(new UserMatch());
        stateController.log();
        messageSenderService.sendMessageToConcreteChat(userTwo.getChatId(), "Я знайшов пару для тебе, " + "@" + userOne.getUserName() +
                " також хоче " + commonInterest);
        messageSenderService.sendMessageToConcreteChat(userOne.getChatId(), "Я знайшов пару для тебе, " + "@" + userTwo.getUserName() +
                " також хоче " + commonInterest);
        stateController = new StateController(userMatch);
        stateController.updateUserState(userOne);
        stateController.updateUserState(userTwo);
    }

    private void start(long chatId) {
        messageSenderService.sendMessageToConcreteChat(chatId, "Привіт, я бот для пошуку людей по інтересах натисни щоб почати");
        keyboardService.addMenuButtons(chatId);
    }

    private void interest(long chatId, int messageId) {
        keyboardService.addExistedInterestButtons(chatId, stateController, messageId);
    }

    private void notification(long chatId, int messageId) {
        keyboardService.addNotificationSenderButtons(chatId, messageId);
    }

    private void back(long chatId, int messageId) {
        keyboardService.addMenuButtons(chatId, messageId);
    }

    private void help(long chatId) {
        messageSenderService.sendMessageToConcreteChat(chatId, "Сам ще не розумію, що тут відбувається, тому вибачся!");
    }

    private void onDefault(long chatId) {
        messageSenderService.sendMessageToConcreteChat(chatId, "Вибачте, занадто нерозумний, " +
                "щоб зрозуміти це повідомлення, будь ласка, спробуйте ще раз.");
    }

    @Scheduled(fixedRate = 10000)
    private void receiveMatchRequest() {
        List<flashmatch.entity.User> timeOffUsers = getUserWitTimeEnd();
        if (!timeOffUsers.isEmpty()) {
            FlashMatch.getLogger().info("Found user with time off " + Thread.currentThread().getName() + Thread.currentThread().getId());
            timeOffUsers.forEach(timeOffUser -> {
                if (matchedUsers.get(timeOffUser.getChatId()) == null) {
                    List<Long> matchedUsersChatId = new ArrayList<>();
                    Interest waitedInterest = interestService.getInterestById(timeOffUser.getInterestId());
                    List<flashmatch.entity.User> users = userService.getAlreadyMatchedUsers(timeOffUser.getInterestId(), timeOffUser.getChatId());
                    users.stream()
                            .filter(flashmatch.entity.User::isNotification)
                            .forEach(user -> {
                                messageSenderService.sendMessageToConcreteChat(user.getChatId(), "@" + timeOffUser.getUserName() + " хоче " + waitedInterest.getName());
                                FlashMatch.getLogger().info("Message was send to " + user.getUserName());
                                keyboardService.addChooseButtons(user.getChatId());
                                matchedUsersChatId.add(user.getChatId());
                            });
                    matchedUsers.put(timeOffUser.getChatId(), matchedUsersChatId);
                }
            });
        }
    }

    private List<flashmatch.entity.User> getUserWitTimeEnd() {
        FlashMatch.getLogger().info("Try to get users by time end " + Thread.currentThread().getName() + Thread.currentThread().getId());
        long timeNow = new Date().getTime();
        int minWaitTime = 30;
        int maxWaitTime = 60;
        List<flashmatch.entity.User> usersListWithTime = userService.getUsersWithTime();
        return usersListWithTime.stream()
                .filter(user -> {
                    long userWaitTime = (timeNow - user.getTime()) / 1000;
                    if (userWaitTime >= minWaitTime && userWaitTime < maxWaitTime) {
                        return true;
                    } else if (userWaitTime >= maxWaitTime) {
                        matchedUsers.remove(user.getChatId());
                        stateController = new StateController(userChoseActivityState);
                        stateController.updateUserState(user);
                        messageSenderService.sendMessageToConcreteChat(user.getChatId(), "Вибач, та нажаль не можемо знайти для тебе пари прямо зараз, спробуй пізніше");
                        return false;
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
}
