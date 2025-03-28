package org.example.angulardemo.repository

import org.example.angulardemo.entity.Product
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProductCrudRepository : CoroutineCrudRepository<Product, Long> {
}
