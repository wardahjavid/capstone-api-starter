package org.yearup.data;

import org.yearup.models.Category;

import java.sql.SQLException;
import java.util.List;

public interface CategoryDao
{
    List<Category> getAllCategories() throws SQLException;
    Category getById(int categoryId) throws SQLException;
    Category create(Category category) throws SQLException;
    void update(int categoryId, Category category) throws SQLException;
    void delete(int categoryId) throws SQLException;
}
