package net.orekhov.pandew.telegrambot.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * Сервис для работы с файлами.
 * Этот сервис предназначен для экспорта и импорта категорий в формате Excel.
 */
@Component
public class FileService {

    // Логгер для записи сообщений о процессе работы сервиса
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private ExcelExportService excelExportService; // Сервис для экспорта данных в Excel

    @Autowired
    private ExcelImportService excelImportService; // Сервис для импорта данных из Excel

    /**
     * Экспортирует категории в формат Excel.
     * Делегирует выполнение задачи сервису ExcelExportService.
     *
     * @return ByteArrayInputStream поток данных Excel-файла.
     * @throws IOException если произошла ошибка при создании или записи в Excel файл.
     */
    public ByteArrayInputStream exportCategoriesToExcel() throws IOException {
        // Используем метод excelExportService для получения Excel файла с категориями
        return excelExportService.exportCategoriesToExcel();
    }

    /**
     * Импортирует категории из Excel.
     * Делегирует выполнение задачи сервису ExcelImportService.
     *
     * @param fileStream входной поток данных Excel-файла.
     * @throws IOException если произошла ошибка при чтении Excel файла.
     */
    public void importCategoriesFromExcel(ByteArrayInputStream fileStream) throws IOException {
        // Используем метод excelImportService для импорта категорий из переданного Excel файла
        excelImportService.importCategoriesFromExcel(fileStream);
    }
}

