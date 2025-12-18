package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrdersDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;


@Component
public class MySqlOrdersDao extends MySqlDaoBase implements OrdersDao {
    private ShoppingCartDao shoppingCartDao;
    private ProfileDao profileDao;


    public MySqlOrdersDao(DataSource dataSource, ShoppingCartDao shoppingCartDao, ProfileDao profileDao) {
        super(dataSource);
        this.shoppingCartDao = shoppingCartDao;
        this.profileDao = profileDao;
    }

    @Override
    public int checkout(int userId) {
            ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);
            if (shoppingCart == null || shoppingCart.getItems().isEmpty()) {
                throw new RuntimeException("Cart is empty.");
            }

            Profile profile = profileDao.getByUserId(userId);
            if(profile == null) {
                throw new RuntimeException("Profile not found.");
            }

            String insertOrderSql = """
                    INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
            String insertLineSql = """
                    INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            String clearCartsql = "DELETE FROM shopping_cart WHERE user_id = ?";
            try (Connection connection = getConnection()) {
                connection.setAutoCommit(false);
                int orderId;

                try (PreparedStatement ps = connection.prepareStatement(insertOrderSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(3, profile.getAddress());
                    ps.setString(4, profile.getCity());
                    ps.setString(5, profile.getState());
                    ps.setString(6, profile.getZip());
                    ps.setBigDecimal(7, BigDecimal.ZERO);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next())
                            throw new RuntimeException("Order insert failed (no generated key).");
                        orderId = keys.getInt(1);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


    }
}
