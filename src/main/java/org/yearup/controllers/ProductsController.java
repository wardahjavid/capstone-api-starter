package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductsController
{
    private ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Product> search(
            @RequestParam(name="cat", required = false) Integer categoryId,
            @RequestParam(name="minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name="maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name="subCategory", required = false) String subCategory
    )
    {
        try
        {
            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id)
    {
        try
        {
            Product product = productDao.getById(id);

            if (product == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }

            return product;
        }
        catch (ResponseStatusException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product addProduct(@RequestBody Product product)
    {
        try
        {
            return productDao.create(product);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        try
        {
            Product existing = productDao.getById(id);

            if (existing == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }

            product.setProductId(id);
            productDao.update(id, product);

            return productDao.getById(id);
        }
        catch (ResponseStatusException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable int id)
    {
        try
        {
            Product product = productDao.getById(id);
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }
            productDao.delete(id);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
