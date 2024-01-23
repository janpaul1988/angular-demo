package org.example.angulardemo.repo;

import org.example.angulardemo.entity.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {
}