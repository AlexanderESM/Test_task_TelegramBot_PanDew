package net.orekhov.pandew.telegrambot.config;

import net.orekhov.pandew.telegrambot.bot.MyTelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Конфигурация для настройки и регистрации Телеграм-бота.
 * Здесь создается и настраивается TelegramBotsApi, который управляет взаимодействием с Телеграмом.
 */
@Configuration
public class BotConfig {

    /**
     * Создает и настраивает экземпляр TelegramBotsApi для работы с Телеграм-ботом.
     * Регистрирует бота для обработки обновлений.
     *
     * @param myTelegramBot экземпляр бота, который будет зарегистрирован в TelegramBotsApi
     * @return экземпляр TelegramBotsApi
     * @throws TelegramApiException если произошла ошибка при регистрации бота
     */
    @Bean
    public TelegramBotsApi telegramBotsApi(MyTelegramBot myTelegramBot) throws TelegramApiException {
        // Создаем объект TelegramBotsApi с использованием сессии по умолчанию
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        // Регистрируем бота для обработки обновлений
        botsApi.registerBot(myTelegramBot);

        return botsApi;  // Возвращаем объект TelegramBotsApi для дальнейшего использования
    }
}
