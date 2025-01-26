package net.orekhov.pandew.telegrambot.service;

import net.orekhov.pandew.telegrambot.bot.MyTelegramBot;
import net.orekhov.pandew.telegrambot.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

/**
 * Класс обработчика команд для Telegram-бота.
 * Обрабатывает команды пользователей, взаимодействует с сервисами категорий и файлов,
 * а также отправляет ответы в чат.
 */
@Component
public class CommandHandler {

    private final CategoryService categoryService;
    private final FileService fileService;
    private final MyTelegramBot myTelegramBot;

    /**
     * Конструктор, инициализирующий сервисы.
     *
     * @param categoryService Сервис для работы с категориями.
     * @param fileService Сервис для работы с файлами.
     * @param myTelegramBot Инстанс Telegram-бота.
     */
    @Autowired
    public CommandHandler(CategoryService categoryService, FileService fileService, MyTelegramBot myTelegramBot) {
        this.categoryService = categoryService;
        this.fileService = fileService;
        this.myTelegramBot = myTelegramBot;
    }

    /**
     * Обрабатывает команду, введённую пользователем.
     * Разбирает команду и выполняет соответствующее действие.
     *
     * @param commandText Текст команды.
     * @param chatId Идентификатор чата для отправки сообщений.
     * @return Ответ на команду.
     */
    public String handleCommand(String commandText, String chatId) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return "Команда не может быть пустой. Используйте /help для получения списка доступных команд.";
        }

        String[] commandParts = commandText.trim().split(" ", 2);
        String command = commandParts[0];

        switch (command) {
            case "/start":
            case "/help":
                return showHelp();  // Показывает список доступных команд.

            case "/viewTree":
                return categoryService.viewTree();  // Показывает дерево категорий.

            case "/addElement":
                return commandParts.length > 1 ? addElement(commandParts[1]) :
                        "Неверный формат команды. Используйте:\n" +
                                "/addElement <название категории>\n" +
                                "/addElement <родительская категория> <дочерняя категория>";

            case "/removeElement":
                return commandParts.length > 1 ? removeElement(commandParts[1]) :
                        "Неверный формат команды. Используйте /removeElement <название категории>.";

            case "/download":
                return handleDownload(chatId);  // Генерация и отправка Excel файла.

            case "/upload":
                return "Пожалуйста, отправьте файл Excel для загрузки дерева категорий.";

            default:
                return "Неизвестная команда. Используйте /help для получения списка команд.";
        }
    }

    /**
     * Генерирует и отправляет файл Excel с деревом категорий.
     *
     * @param chatId Идентификатор чата для отправки файла.
     * @return Сообщение об успешной отправке или ошибке.
     */
    private String handleDownload(String chatId) {
        try {
            // Генерация Excel файла с помощью FileService.
            ByteArrayInputStream excelFile = fileService.exportCategoriesToExcel();

            // Подготовка файла для отправки через Telegram.
            InputStream fileInputStream = excelFile;
            String fileName = "categories.xlsx";
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(fileInputStream, fileName));

            // Отправка файла через бот.
            myTelegramBot.execute(sendDocument);

            return "Файл успешно сгенерирован и отправлен.";
        } catch (Exception e) {
            return "Произошла ошибка при создании или отправке файла. Попробуйте снова.";
        }
    }

    /**
     * Добавление новой категории.
     *
     * @param commandText Текст команды с названием категории.
     * @return Сообщение о результате добавления категории.
     */
    private String addElement(String commandText) {
        // Удаляем лишние пробелы в начале и конце строки.
        String cleanedCommand = commandText.trim().replaceAll("\\s+", " ");
        String[] elements = cleanedCommand.split(" ", 2);

        if (elements.length == 1) {
            // Создание корневой категории.
            String parentCategoryName = elements[0];
            try {
                categoryService.addCategory(parentCategoryName, null);
                return "Корневая категория '" + parentCategoryName + "' успешно добавлена.";
            } catch (Exception e) {
                return "Произошла ошибка при добавлении корневой категории. Попробуйте снова.";
            }
        } else if (elements.length == 2) {
            // Добавление дочерней категории.
            String parentCategoryName = elements[0];
            String childCategoryName = elements[1];
            try {
                Optional<Category> parentCategory = categoryService.getCategoryByName(parentCategoryName);
                if (parentCategory.isEmpty()) {
                    return "Родительская категория '" + parentCategoryName + "' не найдена.";
                }
                categoryService.addCategory(childCategoryName, parentCategory.get());
                return "Дочерняя категория '" + childCategoryName + "' успешно добавлена к родительской категории '" + parentCategoryName + "'.";
            } catch (Exception e) {
                return "Произошла ошибка при добавлении дочерней категории. Попробуйте снова.";
            }
        } else {
            // Неверный формат команды.
            return "Неверный формат команды. Используйте:\n" +
                    "/addElement <название категории>\n" +
                    "/addElement <родительская категория> <дочерняя категория>";
        }
    }

    /**
     * Обрабатывает команду удаления категории.
     *
     * @param commandText Текст команды для удаления категории.
     * @return Сообщение о результате удаления категории или ошибка в случае неверного формата.
     */
    private String removeElement(String commandText) {
        commandText = commandText.trim();

        // Проверяем, что после первого слова нет других символов, кроме пробелов.
        if (commandText.matches("^[^\\s]+(\\s*)$")) {
            // Если формат команды верный, удаляем указанную категорию.
            categoryService.deleteCategory(commandText);
            return "Категория " + commandText + " удалена!";
        } else {
            // Если команда не соответствует ожидаемому формату, возвращаем сообщение об ошибке.
            return "Неверный формат команды. Используйте /removeElement <название категории>";
        }
    }

    /**
     * Показывает список доступных команд.
     *
     * @return Список команд.
     */
    private String showHelp() {
        return "Доступные команды:\n" +
                "/start - Начало работы с ботом. Выводит список команд.\n" +
                "/help - Список доступных команд.\n" +
                "/viewTree - Отображает дерево категорий.\n" +
                "/addElement <название категории> - Добавить корневую категорию.\n" +
                "/addElement <родительская категория> <дочерняя категория> - Добавить дочернюю категорию.\n" +
                "/removeElement <название категории> - Удалить категорию и её дочерние элементы.\n" +
                "/download - Скачать дерево категорий в формате Excel.\n" +
                "/upload - Загрузить дерево категорий из Excel файла.";
    }
}
