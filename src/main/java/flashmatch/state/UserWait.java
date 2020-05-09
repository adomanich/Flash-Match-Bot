package flashmatch.state;

import flashmatch.bot.FlashMatch;
import flashmatch.entity.User;
import flashmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("userWaitState")
public class UserWait implements State {

    @Autowired
    private UserService userService;

    private final int STATE_ID = 2;

    @Override
    public State updateUserState(User user) {
        user.setStateId(getCurrentStateID());
        user.setTime(new Date().getTime());
        userService.update(user);
        FlashMatch.getLogger().info(user.getUserName() + " user waiting for matching");
        return this;
    }

    @Override
    public void log() {
        FlashMatch.getLogger().info("User wait for matching...");
    }

    @Override
    public int getCurrentStateID() {
        return STATE_ID;
    }
}
