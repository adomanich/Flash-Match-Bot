package flashmatch.userstrategy;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface User {
    void doWork(Update update, boolean isMessage, long chatId);
}
