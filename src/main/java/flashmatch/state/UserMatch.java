package flashmatch.state;

import flashmatch.bot.FlashMatch;
import flashmatch.entity.User;
import flashmatch.service.MessageSenderService;
import flashmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("userMatchState")
public class UserMatch implements State {

    @Autowired
    private UserService userService;
    @Autowired
    private MessageSenderService messageSenderService;

    private final int STATE_ID = 3;

    @Override
    public State updateUserState(User user) {
        user.setTime(0L);
        user.setStateId(getCurrentStateID());
        userService.update(user);
        FlashMatch.getLogger().info(user.getUserName() + " have matched");
        return this;
    }

    @Override
    public void log() {
        FlashMatch.getLogger().info("User just have matched");
    }

    @Override
    public int getCurrentStateID() {
        return STATE_ID;
    }
}
