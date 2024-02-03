package org.example.angulardemo.controller;

import jakarta.validation.Valid;
import org.example.angulardemo.entity.Product;
import org.example.angulardemo.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<?> addProduct(@Valid @RequestBody Product newProduct, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        if (newProduct.getId() != null) {
            return new ResponseEntity<>("Id field should not be set", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(productRepository.save(newProduct), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productUpdate, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        return productRepository.findById(id)
                .map(product -> {
                    product.setExtId(productUpdate.getExtId());
                    product.setName(productUpdate.getName());
                    product.setDescription(productUpdate.getDescription());
                    return new ResponseEntity<>(productRepository.save(product), HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}