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
        doesProductExist(productId)
        return addProduct(product)
    }

    suspend fun deleteProduct(productId: Long) {
        doesProductExist(productId)
        productRepository.deleteById(productId)
    }

    private suspend fun doesProductExist(productId: Long) {
        productRepository.findById(productId) ?: throw ProductNotFoundException(productId)
    }
}
