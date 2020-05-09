package flashmatch.state;

import flashmatch.bot.FlashMatch;
import flashmatch.entity.User;
import flashmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service("userPreSelectState")
public class UserChoseActivityState implements State {

    @Autowired
    private UserService userService;

    private final int STATE_ID = 0;

    @Override
    public State updateUserState(User user) {
        user.setNotification(true);
        user.setStateId(getCurrentStateID());
        user.setTime(0L);
        userService.update(user);
        FlashMatch.getLogger().info(user.getUserName() + " move to pre-select interest state");
        return this;
    }

    @Override
    public void log() {
        FlashMatch.getLogger().info("User choose the activity...");
    }

    @Override
    public int getCurrentStateID() {
        return STATE_ID;
    }
}
