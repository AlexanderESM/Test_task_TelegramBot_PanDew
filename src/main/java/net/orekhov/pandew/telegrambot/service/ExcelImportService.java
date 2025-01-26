package net.orekhov.pandew.telegrambot.service;

import net.orekhov.pandew.telegrambot.model.Category;
import net.orekhov.pandew.telegrambot.repository.CategoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для импорта категорий из Excel.
 * Этот сервис использует Apache POI для работы с файлами формата .xlsx.
 */
@Service
public class ExcelImportService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Импортирует категории из Excel-файла.
     * Читает данные из Excel, создает объекты Category и сохраняет их в базе данных.
     * Предполагается, что файл Excel содержит два столбца: имя категории и имя родительской категории.
     *
     * @param inputStream входной поток данных Excel-файла.
     * @throws IOException если произошла ошибка при чтении Excel файла.
     */
    public void importCategoriesFromExcel(InputStream inputStream) throws IOException {
        // Открываем Excel-файл из входного потока
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            // Получаем первый лист из книги
            Sheet sheet = workbook.getSheetAt(0);
            List<Category> categories = new ArrayList<>();

            // Проходим по всем строкам листа (начиная с 1, так как 0 — это заголовки)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                // Извлекаем имя категории и имя родительской категории
                String name = row.getCell(0).getStringCellValue();
                String parentName = row.getCell(1).getStringCellValue();

                // Находим родительскую категорию в базе данных (если она указана)
                Category parent = null;
                if (!parentName.isEmpty()) {
                    parent = categoryRepository.findByName(parentName)
                            .orElseGet(() -> {
                                // Если родительская категория не найдена, создаем новую
                                Category newParent = new Category(parentName, null);
                                categoryRepository.save(newParent);
                                return newParent;
                            });
                }

                final Category finalParent = parent; // Делаем переменную final (или effectively final)

                // Ищем или создаем новую категорию
                Category category = categoryRepository.findByName(name)
                        .orElseGet(() -> {
                            Category newCategory = new Category(name, finalParent);
                            categoryRepository.save(newCategory);
                            return newCategory;
                        });

                // Добавляем категорию в список
                categories.add(category);
            }

            // Сохраняем все категории в базе данных
            categoryRepository.saveAll(categories);
        }
    }
}

