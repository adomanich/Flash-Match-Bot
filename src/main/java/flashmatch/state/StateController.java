package flashmatch.state;

import flashmatch.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StateController {

    private State state;

    @Autowired
    public StateController(State state) {
        this.state = state;
    }

    public State updateUserState(User user) {
        return state.updateUserState(user);
    }

    public void log() {
        state.log();
    }

    public int getCurrentStateId() {
        return state.getCurrentStateID();
    }
}
