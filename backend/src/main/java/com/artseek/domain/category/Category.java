package com.artseek.domain.category;

import com.artseek.domain.category.exceptions.InvalidCategoryNameException;
import com.artseek.domain.category.exceptions.InvalidCategoryTypeException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A category shared by artworks. */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    /** Required by JPA. Domain code should use the public constructor. */
    protected Category() {
    }

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
