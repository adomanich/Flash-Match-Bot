package flashmatch.state;

import flashmatch.bot.FlashMatch;
import flashmatch.entity.User;
import flashmatch.service.MessageSenderService;
import flashmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("userDoneChoiceState")
public class UserDoneChoice implements State {

    @Autowired
    private UserService userService;
    @Autowired
    private MessageSenderService messageSenderService;

    private final int STATE_ID = 1;

    @Override
    public State updateUserState(User user) {
        user.setStateId(getCurrentStateID());
        userService.update(user);
        FlashMatch.getLogger().info(user.getUserName() + " have chosen an interest");
        messageSenderService.sendMessageToConcreteChat(user.getChatId(), "Біжу шукати тобі партнера, зажди трішки");
        return this;
    }

    @Override
    public void log() {
        FlashMatch.getLogger().info("User have chosen the activity");
    }

    @Override
    public int getCurrentStateID() {
        return STATE_ID;
    }
}
