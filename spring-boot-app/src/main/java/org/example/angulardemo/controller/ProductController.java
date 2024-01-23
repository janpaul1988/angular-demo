package org.example.angulardemo.controller;

import org.example.angulardemo.entity.Product;
import org.example.angulardemo.repo.ProductRepository;
import org.example.angulardemo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost")
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
       return productService.getAllProducts();
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