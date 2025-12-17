package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT category_id, name, description FROM categories;";

        try(Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet row = statement.executeQuery()) {
            while (row.next()) {
                categories.add(mapRow(row));
            }
        }
        return categories;
    }

    @Override
    public Category getById(int categoryId) throws SQLException
    {
        String query = "SELECT category_id, name, description FROM categories WHERE category_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setInt(1, categoryId);
            try (ResultSet row = statement.executeQuery()) {
                if (row.next()){
                    return mapRow(row);
                }
            }

        }
        // get category by id
        return null;
    }

    @Override
    public Category create(Category category) throws SQLException {
        String query = "INSERT INTO categories (name, description) VALUES (?, ?);";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsUpdate = statement.executeUpdate();

            if (rowsUpdate <= 0){
                return null;
            }
            try (ResultSet keys = statement.getGeneratedKeys()){
                if (keys.next()){
                    int newId = keys.getInt(1);
                    return getById(newId);
                }
            }
        }
        return null;
    }

    @Override
    public void update(int categoryId, Category category) throws SQLException
    {
        String query = "UPDATE categories SET name = ? , description = ? WHERE category_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);

            statement.executeUpdate();
        }
    }

    @Override
    public void delete(int categoryId) throws SQLException
    {
        String query = "DELETE FROM categories WHERE category_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        }
        // delete category
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};
        return category;
    }
}
