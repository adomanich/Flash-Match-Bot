package flashmatch.controller;


import flashmatch.bot.FlashMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    private FlashMatch flashMatch;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return flashMatch.onWebhookUpdateReceived(update);
    }
}
