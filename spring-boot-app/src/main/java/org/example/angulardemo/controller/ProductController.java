package org.example.angulardemo.controller;

import org.example.angulardemo.entity.Product;
import org.example.angulardemo.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost")
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository productRepository;

    @Autowired
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public Iterable<Product> getAllProducts() {
       return productRepository.findAll();
    }

    @PostMapping
    public Product addProduct(@RequestBody Product newProduct) {
        return productRepository.save(newProduct);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public void updateProduct(@PathVariable Long id, @RequestBody Product productUpdate) {

        productRepository.findById(id)
                .map(product -> {
                    product.setExtId(productUpdate.getExtId());
                    product.setName(productUpdate.getName());
                    product.setDescription(productUpdate.getDescription());
                    return productRepository.save(product);
                });
    }
}