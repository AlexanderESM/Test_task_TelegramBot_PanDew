package net.orekhov.pandew.telegrambot.service;

import net.orekhov.pandew.telegrambot.model.Category;
import net.orekhov.pandew.telegrambot.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления категориями.
 * Включает методы для получения, добавления, удаления и отображения дерева категорий.
 */
@Service // Аннотация для определения класса как сервисного компонента Spring
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository; // Репозиторий для работы с категориями

    /**
     * Получить все категории.
     *
     * @return Список всех категорий.
     */
    @Transactional // Обозначение метода как транзакционного
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(); // Возвращаем все категории из базы данных
    }

    /**
     * Отобразить дерево категорий в виде строки.
     * Строит дерево, начиная с корневых категорий.
     *
     * @return Строковое представление дерева категорий.
     */
    @Transactional // Обозначение метода как транзакционного
    public String viewTree() {
        logger.info("Отображение дерева категорий.");
        List<Category> categories = getAllCategories(); // Получаем все категории
        if (categories.isEmpty()) {
            return "Дерево категорий пусто."; // Если категорий нет, возвращаем сообщение
        }

        StringBuilder builder = new StringBuilder("Дерево категорий:\n");
        for (Category category : categories) {
            if (category.getParent() == null) { // Только корневые категории
                builder.append(printTreeElement(category, 0)); // Строим дерево
            }
        }
        return builder.toString(); // Возвращаем строку с деревом категорий
    }

    /**
     * Рекурсивный метод для отображения категории и её дочерних элементов.
     *
     * @param category Категория, которую нужно отобразить.
     * @param level Уровень вложенности категории в дереве.
     * @return Строковое представление категории с её дочерними элементами.
     */
    private String printTreeElement(Category category, int level) {
        StringBuilder builder = new StringBuilder();
        builder.append("  ".repeat(level)).append("- ").append(category.getName()).append("\n"); // Отступ для вложенности
        for (Category child : category.getChildren()) {
            builder.append(printTreeElement(child, level + 1)); // Рекурсивно добавляем детей
        }
        return builder.toString();
    }

    /**
     * Найти категорию по названию.
     *
     * @param name Название категории.
     * @return Опциональный объект, содержащий категорию с данным названием (если найдена).
     */
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name); // Используем репозиторий для поиска категории по названию
    }

    /**
     * Добавить новую категорию.
     * Включает проверку на существование дочерней категории с таким же названием у родителя.
     *
     * @param name Название новой категории.
     * @param parent Родительская категория.
     * @return Добавленная категория.
     * @throws IllegalArgumentException если родительская категория не найдена или дочерняя категория с таким названием уже существует.
     */
    @Transactional // Обозначение метода как транзакционного
    public Category addCategory(String name, Category parent) {
        Category category = new Category(); // Создаем новую категорию
        category.setName(name); // Устанавливаем название
        category.setParent(parent); // Устанавливаем родительскую категорию

        if (parent != null) {
            // Загружаем родительскую категорию из базы данных
            parent = categoryRepository.findById(parent.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена."));

            // Загружаем коллекцию дочерних категорий, чтобы избежать LazyInitializationException
            parent.getChildren().size();  // Принудительная загрузка дочерних категорий

            // Проверяем, существует ли уже дочерняя категория с таким же названием
            if (categoryRepository.existsByParentAndName(parent, name)) {
                throw new IllegalArgumentException("Дочерняя категория с таким названием уже существует.");
            }

            // Добавляем категорию к родительской
            parent.getChildren().add(category);
            categoryRepository.save(category); // Сохраняем новую категорию
            categoryRepository.save(parent); // Сохраняем обновленную родительскую категорию
        } else {
            // Если родитель не указан, сохраняем категорию без родителя
            categoryRepository.save(category);
        }

        return category; // Возвращаем добавленную категорию
    }

    /**
     * Удалить категорию по имени, включая все дочерние категории.
     * Включает удаление потомков и родительской категории.
     *
     * @param name Название категории для удаления.
     */
    @Transactional // Обозначение метода как транзакционного
    public void deleteCategory(String name) {
        List<String> names = List.of(name);
        List<Category> categories = categoryRepository.findByNameIn(names);

        // Проверка, если не найдено категорий с данным именем
        if (categories.isEmpty()) {
            logger.info("Категория с именем '{}' не найдена.", name);
        } else {
            // Проходим по всем найденным категориям
            for (Category category : categories) {
                // Если категория не имеет дочерних элементов
                if (category.getChildren().isEmpty()) {
                    // Это категория-потомок, выводим только её имя
                    logger.info("Категория (потомок) с именем '{}' найдена. Перечень всех найденных элементов: {}", name, category.getName());
                    // Удаляем категорию-потомок
                    categoryRepository.delete(category);
                    logger.info("Категория (потомок) с именем '{}' удалена.", category.getName());
                } else {
                    // Это родительская категория, выводим всех потомков (включая вложенные)
                    List<Category> allDescendants = getAllDescendants(category);
                    String allDescendantNames = allDescendants.stream()
                            .map(Category::getName)  // Получаем имя каждого потомка
                            .reduce((name1, name2) -> name1 + ", " + name2)  // Объединяем имена потомков в одну строку
                            .orElse("Нет потомков");  // Если нет потомков
                    logger.info("Родительская категория с именем '{}' найдена. Список всех потомков: {}", name, allDescendantNames);

                    // Удаляем все потомков
                    for (Category descendant : allDescendants) {
                        category.getChildren().remove(descendant);  // Удаляем потомка из родительской категории
                        categoryRepository.delete(descendant);  // Удаляем саму категорию из базы данных
                        logger.info("Потомок с именем '{}' удален из категории с именем '{}'.", descendant.getName(), category.getName());
                    }

                    // Теперь удаляем родительскую категорию
                    categoryRepository.delete(category);
                    logger.info("Родительская категория с именем '{}' удалена.", category.getName());
                }
            }
        }
    }

    /**
     * Рекурсивный метод для получения всех потомков категории.
     *
     * @param parent Родительская категория.
     * @return Список всех потомков категории (включая вложенные).
     */
    private List<Category> getAllDescendants(Category parent) {
        List<Category> descendants = new ArrayList<>(parent.getChildren());
        for (Category child : parent.getChildren()) {
            descendants.addAll(getAllDescendants(child));  // Рекурсивный вызов для дочерних категорий
        }
        return descendants;
    }
}
