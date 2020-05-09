package flashmatch.userstrategy;

import flashmatch.state.StateController;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface User {
    void doWork(Update update, StateController stateController, boolean isMessage, long chatId);
}
