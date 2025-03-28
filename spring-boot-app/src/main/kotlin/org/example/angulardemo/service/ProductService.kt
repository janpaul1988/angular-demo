package org.example.angulardemo.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.exception.ProductNotFoundException
import org.example.angulardemo.repository.ProductCrudRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    val productRepository: ProductCrudRepository,
) {

    suspend fun addCourse(productDTO: ProductDTO): ProductDTO {
        return productRepository.save(productDTO.toProduct()).toProductDTO()
    }

    suspend fun getAllProducts(): Flow<ProductDTO> = productRepository.findAll()
        .map {
            it.toProductDTO()
        }

    suspend fun updateProduct(productId: Long, productDTO: ProductDTO): ProductDTO {
        val product = productRepository
            .findById(productId) ?: throw ProductNotFoundException(productId)

        return product
            .also {
                it.name = productDTO.name
                it.description = productDTO.description
            }.let {
                productRepository.save(it).toProductDTO()
            }
    }

    suspend fun deleteProduct(productId: Long) {
        val product = productRepository.findById(productId) ?: throw ProductNotFoundException(productId)
        product.also {
            productRepository.deleteById(productId)
        }
    }
}
