package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// convert this class to a REST controller
// only logged in users should have access to these actions
@RestController
@RequestMapping("/cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController {
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping
    public ShoppingCart getCart(Principal principal) {
        try {
            // get the currently logged in username
            int userId = getUserId(principal);

            // use the shoppingcartDao to get all items in the cart and return the cart
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            return (cart != null) ? cart : new ShoppingCart();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{productId}")
    public ShoppingCart addProductToCart(@PathVariable int productId, Principal principal) {
        try {
            if (productDao.getById(productId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }
            int userId = getUserId(principal);
            shoppingCartDao.addProduct(userId, productId);
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            return (cart != null) ? cart : new ShoppingCart();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping("/products/{productId}")
    public ShoppingCart updateProductInCart(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal
            principal) {
        try {
            if (productDao.getById(productId) == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            if (item == null || item.getQuantity() <= 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity has to be greater than 0.");
            int userId = getUserId(principal);
            shoppingCartDao.updateProductQuantity(userId, productId, item.getQuantity());
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            return (cart != null) ? cart : new ShoppingCart();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping
    public ShoppingCart clearCart(Principal principal) {
        try {
            int userId = getUserId(principal);
            shoppingCartDao.clearCart(userId);
            return new ShoppingCart();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    private int getUserId(Principal principal) {
        if (principal == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        User user = userDao.getByUserName(principal.getName());
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        return user.getId();
    }
}










