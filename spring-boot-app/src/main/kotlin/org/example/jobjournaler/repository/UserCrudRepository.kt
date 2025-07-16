package org.example.jobjournaler.repository

import org.example.jobjournaler.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserCrudRepository : CoroutineCrudRepository<User, Long> {
    suspend fun findByEmail(email: String): User?
}
