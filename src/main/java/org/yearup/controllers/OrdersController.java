package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrdersDao;
import org.yearup.data.UserDao;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@RequestMapping
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class OrdersController {
    private OrdersDao ordersDao;
    private UserDao userDao;

    public OrdersController(UserDao userDao, OrdersDao ordersDao) {
        this.userDao = userDao;
        this.ordersDao = ordersDao;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public int checkout(Principal principal) {
        User user = userDao.getByUserName(principal.getName());
        if(user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        try {
            return ordersDao.checkout(user.getId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
}



}
