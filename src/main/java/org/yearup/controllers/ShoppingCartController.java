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

@RestController
@RequestMapping("/cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController
{
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao)
    {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // GET http://localhost:8080/cart
    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        try {
            int userId = getUserId(principal);
            ShoppingCart shoppingCartcart = shoppingCartDao.getByUserId(userId);
            if (shoppingCartcart == null) {
                shoppingCartcart = new ShoppingCart();
            }
            return shoppingCartcart;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // POST http://localhost:8080/cart/products/15
    @PostMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addProduct(@PathVariable int productId, Principal principal)
    {
        try
        {
            int userId = getUserId(principal);

            // make sure product exists
            if (productDao.getById(productId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }
            shoppingCartDao.addProduct(userId, productId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // PUT http://localhost:8080/cart/products/15
    // BODY: { "quantity": 3 }
    @PutMapping("/products/{productId}")
    public void updateProduct(@PathVariable int productId,
                              @RequestBody ShoppingCartItem item,
                              Principal principal)
    {
        try
        {
            int userId = getUserId(principal);

            if (productDao.getById(productId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }

            if (item == null || item.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0.");
            }
            shoppingCartDao.updateProductQuantity(userId, productId, item.getQuantity());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // DELETE http://localhost:8080/cart
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal)
    {
        try {
            int userId = getUserId(principal);
            shoppingCartDao.clearCart(userId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    private int getUserId(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }

        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        }
        return user.getId();
    }
}
