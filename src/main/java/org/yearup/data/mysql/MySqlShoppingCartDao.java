package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    public MySqlShoppingCartDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) throws SQLException
    {
        ShoppingCart cart = new ShoppingCart();

        String query = "SELECT product_id, quantity FROM shopping_cart WHERE user_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setInt(1, userId);

            try (ResultSet row = statement.executeQuery())
            {
                while (row.next())
                {
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProductId(row.getInt("product_id"));
                    item.setQuantity(row.getInt("quantity"));
                    cart.addItem(item);
                }
            }
        }
        return cart;
    }

    @Override
    public void addProduct(int userId, int productId) throws SQLException {
        String sql =
                "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1) " +
                        "ON DUPLICATE KEY UPDATE quantity = quantity + 1;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity) throws SQLException
    {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);
            statement.executeUpdate();
        }
    }

    @Override
    public void clearCart(int userId) throws SQLException{
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }
}
