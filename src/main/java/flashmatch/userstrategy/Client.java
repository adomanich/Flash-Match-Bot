package flashmatch.userstrategy;

import flashmatch.bot.FlashMatch;
import flashmatch.entity.Interest;
import flashmatch.manager.ButtonManager;
import flashmatch.service.InterestService;
import flashmatch.service.UserService;
import flashmatch.state.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.stream.Collectors;

import static flashmatch.util.CallBackConstant.*;

@Component
public class Client implements User {

    @Autowired
    private UserDoneChoice userDoneChoice;
    @Autowired
    private UserWait userWait;
    @Autowired
    private UserMatch userMatch;
    @Autowired
    private ButtonManager buttonManager;
    @Autowired
    private FlashMatch flashMatch;
    @Autowired
    private InterestService interestService;
    @Autowired
    private UserService userService;

    private StateController stateController;
    private long chatId;
    private Map<Long, List<Long>> matchedUsers = new HashMap<>();

    @Override
    public void doWork(Update update, StateController stateController, boolean isMessage, long chatId) {
        this.stateController = stateController;
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
        buttonManager.addMenuButtons(chatId, messageId);
        flashMatch.sendMessageToConcreteChat(chatId, "Тепер ви отримуватимете нотифікації про з'єднання від людей, які шукають собі заняття :)");
    }

    private void onTurnOffCallBack(long chatId, int messageId) {
        flashmatch.entity.User user = userService.getUsersByChatId(chatId);
        if (user.isNotification()) {
            user.setNotification(false);
            userService.update(user);
        }
        buttonManager.addMenuButtons(chatId, messageId);
        flashMatch.sendMessageToConcreteChat(chatId, "Тепер ви не отримуватимете нотифікації про з'єднання від людей, які шукають собі заняття." +
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
                    userCurrentlyWait.setTime(0L);
                    userCurrentlyWait.setStateId(3);
                    userService.update(userCurrentlyWait);
                    matchedUsers.remove(id);
                }, () -> flashMatch.sendMessageToConcreteChat(chatId, "Ми вже знайшли пару для цього солодня"));
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
                    flashMatch.sendMessageToConcreteChat(chatId, "Дякую за відповідь.");
                });
    }

    private void checkCallBack(Update update) {
        String callBackMessage = update.getCallbackQuery().getData();
        Optional<Interest> result = getInterestCorrespondingToCallBack(callBackMessage);
        if (result.isPresent()) {
            String interestOfCurrentUser = result.get().getName();
            flashmatch.entity.User user = getCurrentUserFromDataBase(chatId, result.get().getId(), update.getCallbackQuery().getFrom().getUserName());
            updateInterestOfUser(user, result.get().getId());

            flashmatch.entity.User waitUser = userService.getWaitedUser(result.get().getId(), user.getChatId());
            if (isSomebodyWaitOnMatch(waitUser, chatId)) {
                putUserIntoQueue(user);
            } else {
                matchTwoUsers(user, waitUser, interestOfCurrentUser);
            }
        }
    }

    private flashmatch.entity.User getCurrentUserFromDataBase(long chatId, int interestId, String userName) {
        stateController = new StateController(new UserDoneChoice());
        stateController.log();
        flashmatch.entity.User user = userService.getUsersByChatId(chatId);
        if (user == null) {
            user = new flashmatch.entity.User(chatId, interestId, stateController.getCurrentStateId(), userName);
            userService.addNew(user);
        }
        return user;
    }

    private Optional<Interest> getInterestCorrespondingToCallBack(String callBackData) {
        return interestService.getAllInterests()
                .stream()
                .filter(interest -> (interest.getName() + CALL_BACK_ENDING).equals(callBackData))
                .findAny();
    }

    private void updateInterestOfUser(flashmatch.entity.User user, int actualInterestId) {
        if (user.getInterestId() != actualInterestId) {
            user.setInterestId(actualInterestId);
            user.setNotification(true);
            user.setTime(0L);
            userService.update(user);
        }
    }

    private boolean isSomebodyWaitOnMatch(flashmatch.entity.User user, long chatId) {
        return user == null || user.getChatId() == chatId;
    }

    private void putUserIntoQueue(flashmatch.entity.User user) {
        stateController = new StateController(new UserWait());
        stateController.log();
        updateUserState(user, stateController.getCurrentStateId());
        user.setTime(new Date().getTime());
        userService.update(user);
        flashMatch.sendMessageToConcreteChat(user.getChatId(), "Біжу шукати тобі партнера, зажди трішки");
        FlashMatch.getLogger().info(Thread.currentThread().getName() + " wait with " + user.getUserName());
    }

    private void matchTwoUsers(flashmatch.entity.User userOne, flashmatch.entity.User userTwo, String commonInterest) {
        FlashMatch.getLogger().info(Thread.currentThread().getName() + "mathed with users :" + userOne.getUserName() + " and " + userTwo.getUserName());
        stateController = new StateController(new UserMatch());
        stateController.log();
        flashMatch.sendMessageToConcreteChat(userTwo.getChatId(), "Я знайшов пару для тебе, " + "@" + userOne.getUserName() +
                " також хоче " + commonInterest);
        flashMatch.sendMessageToConcreteChat(userOne.getChatId(), "Я знайшов пару для тебе, " + "@" + userTwo.getUserName() +
                " також хоче " + commonInterest);
        updateUserState(userOne, stateController.getCurrentStateId());
        updateUserState(userTwo, stateController.getCurrentStateId());
    }

    private void updateUserState(flashmatch.entity.User user, int newState) {
        user.setStateId(newState);
        userService.update(user);
    }

    private void start(long chatId) {
        flashMatch.sendMessageToConcreteChat(chatId, "Привіт, я бот для пошуку людей по інтересах натисни щоб почати");
        buttonManager.addMenuButtons(chatId);
    }

    private void interest(long chatId, int messageId) {
        buttonManager.addExistedInterestButtons(chatId, stateController, messageId);
    }

    private void notification(long chatId, int messageId) {
        buttonManager.addNotificationSenderButtons(chatId, messageId);
    }

    private void back(long chatId, int messageId) {
        buttonManager.addMenuButtons(chatId, messageId);
    }

    private void help(long chatId) {
        flashMatch.sendMessageToConcreteChat(chatId, "Сам ще не розумію, що тут відбувається, тому вибачся!");
    }

    private void onDefault(long chatId) {
        flashMatch.sendMessageToConcreteChat(chatId, "Вибачте, занадто нерозумний, " +
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
                                flashMatch.sendMessageToConcreteChat(user.getChatId(), "@" + timeOffUser.getUserName() + " хоче " + waitedInterest.getName());
                                FlashMatch.getLogger().info("Message was send to " + user.getUserName());
                                buttonManager.addChooseButtons(user.getChatId());
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
                        proceedUserToInitialState(user);
                        return false;
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private void proceedUserToInitialState(flashmatch.entity.User user) {
        stateController = new StateController(new UserChoseActivityState());
        matchedUsers.remove(user.getChatId());
        user.setTime(0L);
        user.setStateId(0);
        userService.update(user);
        stateController.log();
        flashMatch.sendMessageToConcreteChat(chatId, "Вибач, та нажаль не можемо знайти для тебе пари прямо зараз, спробуй пізніше");
    }
}
