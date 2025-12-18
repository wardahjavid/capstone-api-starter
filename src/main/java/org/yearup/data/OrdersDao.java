package org.yearup.data;

import java.sql.SQLException;

public interface OrdersDao {
    int checkout(int userId) throws SQLException;
}
