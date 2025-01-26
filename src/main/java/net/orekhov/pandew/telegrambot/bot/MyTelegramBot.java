package net.orekhov.pandew.telegrambot.bot;

import net.orekhov.pandew.telegrambot.service.CommandHandler;
import net.orekhov.pandew.telegrambot.service.ExcelImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Этот класс представляет собой Телеграм-бота, который обрабатывает сообщения и загрузку файлов.
 * Он расширяет класс TelegramLongPollingBot и предоставляет логику для обработки команд и загрузки файлов.
 */
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    // Логгер для записи ошибок и информации
    private static final Logger logger = LoggerFactory.getLogger(MyTelegramBot.class);

    // Сервисы для обработки команд и импорта данных из Excel
    @Autowired
    private ExcelImportService excelImportService;

    @Lazy
    @Autowired
    private CommandHandler commandHandler;

    // Данные для аутентификации бота
    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    /**
     * Возвращает имя бота.
     *
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Возвращает токен бота.
     *
     * @return токен бота
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Обрабатывает обновления от Телеграма, такие как сообщения и загрузку документов.
     * Обрабатывает текстовые сообщения с командами и загрузку файлов Excel.
     *
     * @param update обновление, полученное от Телеграма
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();

            try {
                // Обрабатываем текстовые сообщения
                if (message.hasText()) {
                    String responseText = commandHandler.handleCommand(message.getText(), chatId);
                    sendTextMessage(chatId, responseText);
                }
                // Обрабатываем загрузку документов (в данный момент только файлы Excel)
                else if (message.hasDocument()) {
                    handleDocument(message.getDocument(), chatId);
                }
            } catch (Exception e) {
                logger.error("Ошибка обработки обновления", e);
                sendTextMessage(chatId, "Произошла ошибка. Пожалуйста, повторите попытку.");
            }
        }
    }

    /**
     * Отправляет текстовое сообщение в чат.
     *
     * @param chatId идентификатор чата для отправки сообщения
     * @param text   текст сообщения
     */
    private void sendTextMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка отправки сообщения", e);
        }
    }

    /**
     * Обрабатывает загруженный документ. В данный момент проверяет, является ли документ файлом Excel,
     * и пытается обработать его.
     *
     * @param document загруженный документ
     * @param chatId   идентификатор чата для отправки ответа
     */
    private void handleDocument(Document document, String chatId) {
        try {
            // Проверяем MIME-тип файла
            String mimeType = document.getMimeType();
            if (mimeType != null && !mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                sendTextMessage(chatId, "Пожалуйста, загрузите файл в формате Excel (.xlsx).");
                return;
            }

            // Скачиваем файл Excel
            byte[] fileBytes = downloadFileFromTelegram(document.getFileId());
            logger.error("Скачиваем файл Excel с document.getFileId(){}", document.getFileId());
            if (fileBytes.length == 0) {
                sendTextMessage(chatId, "Не удалось загрузить файл.");
                return;
            }

            // Импортируем категории из Excel файла
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBytes);
            excelImportService.importCategoriesFromExcel(byteArrayInputStream);

            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            response.setText("Дерево категорий успешно загружено из файла.");
            execute(response);
        } catch (Exception e) {
            logger.error("Ошибка при загрузке Excel файла", e);

            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            response.setText("Произошла ошибка при загрузке дерева категорий из файла.");
            try {
                execute(response);
            } catch (TelegramApiException ex) {
                logger.error("Ошибка при отправке сообщения", ex);
            }
        }
    }

    /**
     * Скачивает файл с сервера Telegram по идентификатору файла.
     *
     * @param fileId идентификатор файла для скачивания
     * @return массив байтов, содержащий данные файла
     */
    public byte[] downloadFileFromTelegram(String fileId) {
        try {
            // Получаем информацию о файле с сервера Telegram
            GetFile getFile = new GetFile(fileId);
            File file = execute(getFile);  // Получаем объект File

            String filePath = file.getFilePath();
            if (filePath == null) {
                throw new TelegramApiException("Путь к файлу отсутствует");
            }

            // Скачиваем файл по URL
            URL fileUrl = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath);
            InputStream inputStream = fileUrl.openStream();

            byte[] fileBytes = inputStream.readAllBytes();
            inputStream.close();

            return fileBytes; // Возвращаем файл как массив байтов
        } catch (TelegramApiException | IOException e) {
            logger.error("Ошибка при скачивании файла", e);
            return new byte[0];  // Возвращаем пустой массив в случае ошибки
        }
    }
}
