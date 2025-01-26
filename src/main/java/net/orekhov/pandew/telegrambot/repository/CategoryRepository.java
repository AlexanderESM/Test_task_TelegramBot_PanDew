package net.orekhov.pandew.telegrambot.repository;

import net.orekhov.pandew.telegrambot.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Category.
 * Наследует интерфейс JpaRepository, что предоставляет стандартные операции с базой данных.
 */
@Repository  // Аннотация для определения класса как репозитория Spring
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Найти категорию по названию.
     *
     * @param name название категории
     * @return Optional<Category> — категория, если найдена, иначе пустой Optional
     */
    Optional<Category> findByName(String name);

    /**
     * Проверить, существует ли категория с данным родителем и названием.
     *
     * @param parent родительская категория
     * @param name название категории
     * @return true, если категория с таким родителем и названием существует, иначе false
     */
    boolean existsByParentAndName(Category parent, String name);

   /**
            * Найти все категории по списку названий.
            *
            * @param names список названий категорий
     * @return List<Category> — список категорий, чьи имена совпадают с элементами списка
     */
    List<Category> findByNameIn(List<String> names);
}
