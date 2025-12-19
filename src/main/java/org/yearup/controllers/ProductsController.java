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

//CONCEPT: Controller Layer (Spring REST Controller)
//- This controller manages all HTTP requests related to products.
//- It exposes endpoints under the /products URL.
//- It handles filtering, lookup by ID, and admin-only CRUD operations.
//- It delegates ALL database logic to ProductDao.

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductsController
{
    // CONCEPT: Dependency Injection (DAO Layer)
    //- ProductDao is an interface.
    //- Spring injects MySqlProductDao at runtime.
    //- This keeps the controller separate from database details.
    private ProductDao productDao;

    //CONCEPT: Constructor Injection
    //- Spring calls this constructor and supplies ProductDao.
    //- This ensures the controller always has its required dependency.
    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    //  ENDPOINT: GET /products/genres
    //- Returns a list of unique genre values from the products table.
    //- This is a read-only GET endpoint.
    //- Often used to populate dropdown filters on the frontend.
    @GetMapping("/genres")
    @PreAuthorize("permitAll()")
    public List<String> getGenres()
    {
        try
        {
            return productDao.getGenres();
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    // GET /products (filters)
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Product> search(
            @RequestParam(name = "cat", required = false) Integer categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "subCategory", required = false) String subCategory
    )
    {
        try
        {
            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    // GET /products/{id}
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id)
    {
        try
        {
            Product product = productDao.getById(id);

            if (product == null)
            {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found."
                );
            }

            return product;
        }
        catch (ResponseStatusException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    // POST /products
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
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    // PUT /products/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        try
        {
            Product existing = productDao.getById(id);

            if (existing == null)
            {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found."
                );
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
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    // DELETE /products/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable int id)
    {
        try
        {
            Product product = productDao.getById(id);

            if (product == null)
            {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found."
                );
            }

            productDao.delete(id);
        }
        catch (ResponseStatusException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }
}
