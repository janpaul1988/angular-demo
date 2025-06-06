package org.example.angulardemo.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.angulardemo.configuration.ProductConfiguration
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.example.angulardemo.entity.User
import org.example.angulardemo.exception.ProductNotFoundException
import org.example.angulardemo.exception.UserNotFoundException
import org.example.angulardemo.mapper.ProductMapper
import org.example.angulardemo.repository.ProductCrudRepository
import org.example.angulardemo.repository.UserCrudRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ProductService(
    val productRepository: ProductCrudRepository,
    val userRepository: UserCrudRepository,
    val productMapper: ProductMapper,
    val productConfiguration: ProductConfiguration,
) {

    suspend fun addProduct(
        userId: Long,
        productDto: ProductDTO,
        retriesLeft: Int = productConfiguration.maxInserts,
    ): ProductDTO {
        if (retriesLeft == 0) {
            val illegalStateException =
                IllegalStateException("Could not generate unique product number after ${productConfiguration.maxInserts} attempts")
            logger.error { illegalStateException.message }
            throw illegalStateException
        }

        // Check if the user exists, throws if it does not.
        doesUserExist(userId)

        try {
            val product = productMapper.toEntity(productDto)
            // Generate a new externalIdentifier by incrementing the max for this user.
            product.externalId = (productRepository.findMaxExternalIdByUserId(userId)
                ?: productConfiguration.externalIdStartingValue) + productConfiguration.externalIdIncrementValue
            return productMapper.toDto(productRepository.save(product))
        } catch (e: DuplicateKeyException) {
            // Retry for race conditions.
            logger.debug {
                """
            Saving new product failed with message:
            ${e.message}
            Now reattempting saving new product,
            reattempt ${productConfiguration.maxInserts - retriesLeft}
        """.trimIndent()
            }
            return addProduct(userId, productDto, retriesLeft - 1)
        }
    }

    suspend fun getAllProductsForUser(userId: Long): Flow<ProductDTO> =
        productRepository.findAllByUserId(userId)
            .map {
                productMapper.toDto(it)
            }

    suspend fun updateProduct(userId: Long, productId: Long, product: ProductDTO): ProductDTO {
        return doesUserProductExist(userId, productId)
            .let {
                it.name = product.name
                it.description = product.description
                productRepository.save(it)
            }.let {
                productMapper.toDto(it)
            }
    }

    suspend fun deleteProduct(userId: Long, productId: Long) {
        doesUserProductExist(userId, productId)
        productRepository.deleteByUserIdAndExternalId(userId, productId)
    }

    private suspend fun doesUserExist(userId: Long): User {
        return userRepository.findById(userId) ?: UserNotFoundException(userId).let {
            logger.error { it.message }
            throw it
        }
    }

    private suspend fun doesUserProductExist(userId: Long, productId: Long): Product {
        return productRepository.findByUserIdAndExternalId(userId, productId) ?: ProductNotFoundException(
            userId,
            productId
        ).let {
            logger.error { it.message }
            throw it
        }
    }

}
