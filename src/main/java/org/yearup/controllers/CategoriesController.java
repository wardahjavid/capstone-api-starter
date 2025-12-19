package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

//SYNTAX NOTE: @RestController tells Spring this class:
//1) handles web requests
//2) automatically converts return values to JSON
//@RequestMapping("/categories") sets the base URL path for all methods
//@CrossOrigin allows requests from other origins (frontend apps)

@RestController
@RequestMapping("/categories")
@CrossOrigin
public class CategoriesController
{
    private CategoryDao categoryDao;
    private ProductDao productDao;


        //CONCEPT: Constructor Injection (Preferred Pattern)

        //- Spring calls this constructor automatically.
       // - It supplies the required DAO implementations.
       // - This ensures the controller cannot exist without its dependencies.
    //`@Autowired` is there because the controller depends on DAOs, so Spring must inject those DAOs automatically,
        // which prevents you from having to manually create the objects yourself.
    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao){
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    //ENDPOINT: GET /categories
    //- Returns a list of all categories in the system.

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Category> getAll() {
        return categoryDao.getAllCategories();
    }

    //ENDPOINT: GET /categories/{id}
    //- Retrieves a single category by its ID.
    //- If the category does not exist, returns 404 Not Found.
    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Category getById(@PathVariable int id) {
        Category category = categoryDao.getById(id);
        // If the DAO returns null, the category does not exist.
        if (category == null) {
            // CONCEPT: HTTP Error Handling
            // ResponseStatusException immediately stops execution
            // and sends the given HTTP status to the client.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        return category;
    }

    //ENDPOINT: GET /categories/{categoryId}/products
    //- Returns all products that belong to a specific category.
    @GetMapping("{categoryId}/products")
    @PreAuthorize("permitAll()")
    public List<Product> getProductsById(@PathVariable int categoryId) {
        // Validate that the category exists first.
        Category category = categoryDao.getById(categoryId);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        // If category exists, return products in that category.
        return productDao.listByCategoryId(categoryId);
    }

    //ENDPOINT: POST /categories
    //- Creates a new category in the database.
    //- POST requests create new resources. This endpoint is restricted to ADMIN users only.
    //@PreAuthorize("hasRole('ADMIN')") ensures only admins can create categories.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Category addCategory(@RequestBody Category category) {
        return categoryDao.create(category);
    }

    //ENDPOINT: PUT /categories/{id}
    // - Updates an existing category.
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateCategory(@PathVariable int id, @RequestBody Category category) {
        // Check if category exists before updating.
        Category existing = categoryDao.getById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        // Ensure the ID used for update matches the URL.
        category.setCategoryId(id);
        categoryDao.update(id, category);
    }

    //ENDPOINT: DELETE /categories/{id}
    //- Deletes a category by ID.
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(@PathVariable int id) {
        //Verify the category exists before deleting.
        Category existing = categoryDao.getById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        categoryDao.delete(id);
    }
}
