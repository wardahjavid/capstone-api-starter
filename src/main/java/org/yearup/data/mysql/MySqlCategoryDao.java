package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {
    public MySqlCategoryDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT category_id, name, description FROM categories;";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet row = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            row = statement.executeQuery();
            while (row.next()) {
                categories.add(mapRow(row));
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(row);
            close(statement);
            close(connection);
        }
        return categories;
    }

    @Override
    public Category getById(int categoryId)
    {
        String sql = "SELECT category_id, name, description FROM categories WHERE category_id = ?;";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet row = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);
            row = statement.executeQuery();

            if (row.next()) {
                return mapRow(row);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(row);
            close(statement);
            close(connection);
        }
    }

    @Override
    public Category create(Category category)
    {
        String query = "INSERT INTO categories (name, description) VALUES (?, ?);";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet keys = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            int rows = statement.executeUpdate();

            if (rows <= 0) {
                return null;
            }

            keys = statement.getGeneratedKeys();

            if (keys.next()) {
                int newId = keys.getInt(1);
                return getById(newId);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(keys);
            close(statement);
            close(connection);
        }
    }

    @Override
    public void update(int categoryId, Category category) {
        String query = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?;";

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(statement);
            close(connection);
        }
    }

    @Override
    public void delete(int categoryId) {
        String query = "DELETE FROM categories WHERE category_id = ?;";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(statement);
            close(connection);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        Category category = new Category();
        category.setCategoryId(row.getInt("category_id"));
        category.setName(row.getString("name"));
        category.setDescription(row.getString("description"));
        return category;
    }

    private void close(AutoCloseable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception e)
            {
                // ignore
            }
        }
    }
}
