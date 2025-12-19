package org.yearup.data;

import org.yearup.models.ShoppingCart;

import java.sql.SQLException;

//This interface defines what actions can be done with a shopping cart.
// It lists methods for getting a user’s cart, adding products, updating product
// quantities, and clearing the cart, while the actual database work is handled by
// the class that implements this interface.
public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId) throws SQLException;
    void addProduct (int userID, int productId) throws SQLException;
    void updateProductQuantity(int userId, int productId, int quantity) throws SQLException;
    void clearCart(int userId) throws SQLException;
}
