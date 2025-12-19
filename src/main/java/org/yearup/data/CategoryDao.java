package org.yearup.data;

import org.yearup.models.Category;


import java.util.List;
//This interface defines what actions can be done with categories in the system.
// It lists the methods for getting, creating, updating, and deleting categories,
// while the actual database logic is handled by a separate class that implements
// this interface.
public interface CategoryDao
{
    List<Category> getAllCategories();
    Category getById(int categoryId);
    Category create(Category category);
    void update(int categoryId, Category category);
    void delete(int categoryId);
}
