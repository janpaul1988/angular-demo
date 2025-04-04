package org.example.angulardemo.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.exception.ProductNotFoundException
import org.example.angulardemo.mapper.ProductMapper
import org.example.angulardemo.repository.ProductCrudRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    val productRepository: ProductCrudRepository,
    val productMapper: ProductMapper,
) {

    suspend fun addProduct(product: ProductDTO): ProductDTO {
        return productRepository.save(productMapper.toEntity(product))
            .let {
                productMapper.toDto(it)
            }
    }

    suspend fun getAllProducts(): Flow<ProductDTO> = productRepository.findAll()
        .map {
            productMapper.toDto(it)
        }

    suspend fun updateProduct(productId: Long, product: ProductDTO): ProductDTO {
        val productToUpdate = productRepository
            .findById(productId) ?: throw ProductNotFoundException(productId)

        return productToUpdate
            .also {
                it.name = product.name
                it.description = product.description
            }.let {
                productRepository.save(it)
            }.let {
                productMapper.toDto(it)
            }
    }

    suspend fun deleteProduct(productId: Long) {
        val product = productRepository.findById(productId) ?: throw ProductNotFoundException(productId)
        product.also {
            productRepository.deleteById(productId)
        }
    }
}
