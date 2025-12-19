package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//CONCEPT: DAO Implementation (MySQL + JDBC)
//- This class is the MySQL/JDBC implementation of the CategoryDao interface.
// - Controllers never write SQL; they call CategoryDao methods.
// - This class contains the SQL needed to read/write categories in the database.

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
            // CONCEPT: ResultSet iteration
            // Each call to row.next() moves to the next returned record.
            while (row.next()) {
                // CONCEPT: Mapping
                // Convert the current ResultSet row into a Category model.
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
            // If no rows were inserted, creation failed.
            if (rows <= 0) {
                return null;
            }
            // Retrieve generated key(s)
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
    //HELPER METHOD: mapRow(ResultSet row)
    //- Converts the current ResultSet row into a Category model object.
    private Category mapRow(ResultSet row) throws SQLException
    {
        Category category = new Category();
        category.setCategoryId(row.getInt("category_id"));
        category.setName(row.getString("name"));
        category.setDescription(row.getString("description"));
        return category;
    }

    //That method safely closes any JDBC resource by first checking for `null` and then calling `close()` inside a try/catch so cleanup errors don’t crash the application. It was commonly used before try-with-resources, which now automatically handles this cleanup and makes the helper unnecessary.
    private void close(AutoCloseable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
