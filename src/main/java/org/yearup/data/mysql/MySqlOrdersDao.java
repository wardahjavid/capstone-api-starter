package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrdersDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;


@Component
public class MySqlOrdersDao extends MySqlDaoBase implements OrdersDao {
    private ShoppingCartDao shoppingCartDao;
    private ProfileDao profileDao;
    private BigDecimal discountAmount;


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
        if (profile == null) {
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
        String clearCartSql = "DELETE FROM shopping_cart WHERE user_id = ?";
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            int orderId;


            //insert order
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

            //insert line items
            try (PreparedStatement ps = connection.prepareStatement(insertLineSql)) {
                for (ShoppingCartItem item : shoppingCart.getItems().values()) {
                    int productId = item.getProductId();
                    int quantity = item.getQuantity();

                    BigDecimal salesPrice = item.getProduct().getPrice();
                    BigDecimal lineSubtotal = salesPrice.multiply(item.getDiscountPercent());

                    ps.setInt(1, orderId);
                    ps.setInt(2, productId);
                    ps.setBigDecimal(3, salesPrice);
                    ps.setInt(4, quantity);
                    ps.setBigDecimal(5, discountAmount);
                    ps.executeUpdate();
                }
            }
            //clear cart
            try (PreparedStatement ps = connection.prepareStatement(clearCartSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            connection.commit();
            return orderId;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
