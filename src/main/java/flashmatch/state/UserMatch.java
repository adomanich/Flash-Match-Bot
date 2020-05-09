package flashmatch.state;

import flashmatch.bot.FlashMatch;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class UserMatch implements State {
    private final int STATE_ID = 3;

    @Override
    public State getNext() {
        return null;
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
