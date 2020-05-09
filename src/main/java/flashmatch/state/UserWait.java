package flashmatch.state;

import flashmatch.bot.FlashMatch;
import org.springframework.stereotype.Component;

@Component
public class UserWait implements State {
    private final int STATE_ID = 2;

    @Override
    public State getNext() {
        return new UserMatch();
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
