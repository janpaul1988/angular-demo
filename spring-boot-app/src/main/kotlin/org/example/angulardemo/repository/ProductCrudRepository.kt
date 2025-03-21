package org.example.angulardemo.repository

import org.example.angulardemo.entity.Product
import org.springframework.data.repository.CrudRepository

interface ProductCrudRepository : CrudRepository<Product, Long> {
}
