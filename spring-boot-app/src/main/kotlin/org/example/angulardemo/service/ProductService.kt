package org.example.angulardemo.service

import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.exception.ProductNotFoundException
import org.example.angulardemo.repository.ProductCrudRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    val productRepository: ProductCrudRepository,
) {

    fun addCourse(productDTO: ProductDTO): ProductDTO {
        return productRepository.save(productDTO.toProduct()).toProductDTO()
    }

    fun getAllProducts(): List<ProductDTO> = productRepository.findAll().map {
        it.toProductDTO()
    }

    fun updateProduct(productId: Long, productDTO: ProductDTO): ProductDTO {
        return productRepository
            .findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }
            .let {
                it.name = productDTO.name
                it.extId = productDTO.name
                it.description = productDTO.description
                productRepository.save(it)
            }.toProductDTO()
    }

    fun deleteProduct(productId: Long) {
        productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }
            .also {
                productRepository.deleteById(productId)
            }
    }
}
