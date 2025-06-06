package org.example.angulardemo.repository

import org.example.angulardemo.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserCrudRepository : CoroutineCrudRepository<User, Long> {
    suspend fun findByEmail(email: String): User?
}
