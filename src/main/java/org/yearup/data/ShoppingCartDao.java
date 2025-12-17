package org.yearup.data;

import org.yearup.models.ShoppingCart;

import java.sql.SQLException;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId) throws SQLException;
    // add additional method signatures here
    void addProduct (int userID, int productId) throws SQLException;
    void updateProductQuantity(int userId, int productId, int quantity) throws SQLException;
    void clearCart(int userId) throws SQLException;
}
