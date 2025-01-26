package net.orekhov.pandew.telegrambot.dto;

import net.orekhov.pandew.telegrambot.model.Category;
import java.util.List;

/**
 * Класс Data Transfer Object (DTO) для категории.
 * Используется для передачи данных категории в ответах API, упрощая представление данных.
 */
public class CategoryDTO {

    // Название категории
    private String name;

    // Список дочерних категорий
    private List<CategoryDTO> children;

    /**
     * Конструктор, который создает DTO-объект на основе модели Category.
     *
     * @param category объект модели Category, из которого будет извлечена информация.
     */
    public CategoryDTO(Category category) {
        // Инициализация поля name значением из категории
        this.name = category.getName();

        // Инициализация списка дочерних категорий, преобразуем их в CategoryDTO
        this.children = category.getChildren().stream()
                .map(CategoryDTO::new) // Преобразуем каждую дочернюю категорию в DTO
                .toList(); // Собираем результат в список
    }

    /**
     * Получение имени категории.
     *
     * @return имя категории.
     */
    public String getName() {
        return name;
    }

    /**
     * Получение списка дочерних категорий.
     *
     * @return список дочерних категорий.
     */
    public List<CategoryDTO> getChildren() {
        return children;
    }
}
