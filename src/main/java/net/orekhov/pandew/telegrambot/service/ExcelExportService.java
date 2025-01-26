package net.orekhov.pandew.telegrambot.service;

import net.orekhov.pandew.telegrambot.model.Category;
import net.orekhov.pandew.telegrambot.repository.CategoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.List;

/**
 * Сервис для экспорта категорий в формат Excel.
 * Этот сервис использует Apache POI для работы с файлами формата .xlsx.
 */
@Service
public class ExcelExportService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Экспортирует все категории в формат Excel.
     * Создает Excel-файл, где каждая категория будет записана в отдельную строку.
     * В первой ячейке будет имя категории, во второй — имя родительской категории (если есть).
     *
     * @return ByteArrayInputStream поток данных Excel-файла.
     * @throws IOException если произошла ошибка при создании или записи в Excel файл.
     */
    public ByteArrayInputStream exportCategoriesToExcel() throws IOException {
        // Получаем список всех категорий из базы данных
        List<Category> categories = categoryRepository.findAll();

        // Создаем новый Excel-файл
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Categories");

            // Создаем строку заголовков
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name"); // Название категории
            headerRow.createCell(1).setCellValue("Parent Name"); // Название родительской категории

            // Заполняем файл данными о категориях
            int rowIdx = 1;
            for (Category category : categories) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(category.getName()); // Имя категории
                // Имя родительской категории или пустая строка, если родителя нет
                row.createCell(1).setCellValue(category.getParent() != null ? category.getParent().getName() : "");
            }

            // Записываем данные в ByteArrayOutputStream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out); // Запись Excel-файла в поток
            return new ByteArrayInputStream(out.toByteArray()); // Возвращаем поток данных
        }
    }
}
