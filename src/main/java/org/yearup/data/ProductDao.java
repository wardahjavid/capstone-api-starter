package org.yearup.data;

import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

//This interface defines what actions can be done with products in the system.
// It lists methods for searching and filtering products, getting products by ID
// or category, creating new products, updating existing ones, deleting products,
// and retrieving available genres, while the database logic is handled by the class
// that implements it.
public interface ProductDao
{
    List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String subCategory);

    // return all distinct genres (subcategory values)
    List<String> getGenres();

    List<Product> listByCategoryId(int categoryId);
    Product getById(int productId);
    Product create(Product product);
    void update(int productId, Product product);
    void delete(int productId);
}
