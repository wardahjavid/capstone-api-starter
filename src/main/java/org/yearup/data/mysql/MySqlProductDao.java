package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//This class is the **MySQL/JDBC data access layer for products**:
// it contains all the SQL needed to create, read, update, delete, and search
// products in the database. Controllers call its methods to retrieve products
// (with filters, by ID, by category, or by genre) or to modify product records,
// while this class handles opening connections, running SQL safely with prepared
// statements, and converting database rows into `Product` objects.
@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String subCategory)
    {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products " +
                "WHERE (category_id = ? OR ? = -1) " +
                "  AND (price >= ? OR ? = -1) " +
                "  AND (price <= ? OR ? = -1) " +
                "  AND (LOWER(subcategory) = LOWER(?) OR ? = '') ";

        int category = (categoryId == null) ? -1 : categoryId;
        BigDecimal minimum = (minPrice == null) ? new BigDecimal("-1") : minPrice;
        BigDecimal maximum = (maxPrice == null) ? new BigDecimal("-1") : maxPrice;

        String genre = (subCategory == null) ? "" : subCategory.trim();

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, category);
            statement.setInt(2, category);

            statement.setBigDecimal(3, minimum);
            statement.setBigDecimal(4, minimum);

            statement.setBigDecimal(5, maximum);
            statement.setBigDecimal(6, maximum);

            statement.setString(7, genre);
            statement.setString(8, genre);

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                products.add(mapRow(row));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }

    // NEW: return distinct genres
    @Override
    public List<String> getGenres()
    {
        List<String> genres = new ArrayList<>();

        String sql =
                "SELECT DISTINCT subcategory " +
                        "FROM products " +
                        "WHERE subcategory IS NOT NULL AND subcategory <> '' " +
                        "ORDER BY subcategory";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                genres.add(row.getString("subcategory"));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return genres;
    }

    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();

        String query = "SELECT * FROM products WHERE category_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                products.add(mapRow(row));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }

    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            ResultSet row = statement.executeQuery();

            if (row.next())
            {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Product create(Product product)
    {
        String sql = "INSERT INTO products(name, price, category_id, description, subcategory, image_url, stock, featured) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0)
            {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next())
                {
                    int productId = generatedKeys.getInt(1);
                    return getById(productId);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void update(int productId, Product product)
    {
        String sql = "UPDATE products " +
                "SET name = ? " +
                "  , price = ? " +
                "  , category_id = ? " +
                "  , description = ? " +
                "  , subcategory = ? " +
                "  , image_url = ? " +
                "  , stock = ? " +
                "  , featured = ? " +
                "WHERE product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int productId)
    {
        String query = "DELETE FROM products WHERE product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String subCategory = row.getString("subcategory");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl);
    }
}
