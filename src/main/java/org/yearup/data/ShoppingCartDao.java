package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    // add additional method signatures here
    void addProduct (int userID, int productId);
    void updateProductQuantity(int userId, int productId, int quantity);
    void ClearCart(int userId);
}
