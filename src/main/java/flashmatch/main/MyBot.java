package flashmatch.main;

import flashmatch.config.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
@EnableScheduling
@Component
public class MyBot extends Configuration {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(MyBot.class, args);
    }
}
