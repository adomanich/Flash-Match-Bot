package flashmatch.state;

import flashmatch.bot.FlashMatch;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class UserChoseActivityState implements State {
    private final int STATE_ID = 0;

    @Override
    public State getNext() {
        return null;
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
