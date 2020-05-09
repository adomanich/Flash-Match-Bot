package flashmatch.state;

import flashmatch.entity.User;

public interface State {

    State updateUserState(User user);

    void log();

    int getCurrentStateID();
}
