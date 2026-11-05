package com.artseek.domain.category;

import com.artseek.domain.category.exceptions.InvalidCategoryNameException;
import com.artseek.domain.category.exceptions.InvalidCategoryTypeException;

/** A category shared by artworks. */
public class Category {

    private final Long id;

    private String name;

    private CategoryType type;

    /** Creates a new category. Its identifier stays null until it is first saved. */
    public Category(String name, CategoryType type) {
        this(null, name, type);
    }

    private Category(Long id, String name, CategoryType type) {
        this.id = id;
        this.name = validateName(name);
        this.type = validateType(type);
    }

    /**
     * Rebuilds an existing category from persisted values. Validation runs again, so stored
     * data that breaks a current rule fails here instead of entering the domain.
     */
    public static Category restore(Long id, String name, CategoryType type) {
        return new Category(id, name, type);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryType getType() {
        return type;
    }

    public void rename(String name) {
        this.name = validateName(name);
    }

    public void changeType(CategoryType type) {
        this.type = validateType(type);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidCategoryNameException(name);
        }
        return name.trim();
    }

    private static CategoryType validateType(CategoryType type) {
        if (type == null) {
            throw new InvalidCategoryTypeException(null);
        }
        return type;
    }
}
