package org.example.angulardemo.repository

import kotlinx.coroutines.flow.Flow
import org.example.angulardemo.entity.Product
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProductCrudRepository : CoroutineCrudRepository<Product, String> {
    suspend fun findAllByUserId(userId: Long): Flow<Product>
    suspend fun findByUserIdAndExternalId(userId: Long, externalId: Long): Product?
    suspend fun deleteByUserIdAndExternalId(userId: Long, externalId: Long)

    @Query("SELECT MAX(external_id) FROM product WHERE user_id = :userId")
    suspend fun findMaxExternalIdByUserId(userId: Long): Long?

}
