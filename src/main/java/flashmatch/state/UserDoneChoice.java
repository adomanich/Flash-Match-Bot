package flashmatch.state;

import flashmatch.bot.FlashMatch;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class UserDoneChoice implements State {
    private final int STATE_ID = 1;

    @Override
    public State getNext() {
        return new UserWait();
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
