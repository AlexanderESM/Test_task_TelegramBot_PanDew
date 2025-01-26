package net.orekhov.pandew.telegrambot.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность Category, представляющая категорию в иерархии категорий.
 * Каждая категория может иметь родительскую категорию (parent) и дочерние категории (children).
 * Используется в контексте работы с базой данных через JPA для создания иерархической структуры категорий.
 *
 * Обратите внимание, что уникальность каждой категории определяется сочетанием имени и родительской категории.
 */
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "parent_id"})
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * Родительская категория, если она существует.
     * Важно: категория может быть дочерней, но не обязательно.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * Список дочерних категорий.
     * Дочерние категории автоматически удаляются при удалении родительской категории.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    /**
     * Конструктор без параметров для JPA.
     */
    public Category() {}

    /**
     * Конструктор для создания новой категории с заданным именем и родительской категорией.
     *
     * @param name название категории
     * @param parent родительская категория
     */
    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }
}
