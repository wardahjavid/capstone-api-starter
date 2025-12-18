package org.yearup.data;

import org.yearup.models.Order;

import java.sql.SQLException;

public interface OrdersDao {
    Order checkout(int userId) throws SQLException;
}
