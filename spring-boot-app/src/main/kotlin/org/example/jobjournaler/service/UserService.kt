package org.example.jobjournaler.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.jobjournaler.dto.UserDTO
import org.example.jobjournaler.entity.User
import org.example.jobjournaler.exception.UserNotFoundException
import org.example.jobjournaler.mapper.UserMapper
import org.example.jobjournaler.repository.UserCrudRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class UserService(
    val userRepository: UserCrudRepository,
    val userMapper: UserMapper,
) {

    suspend fun getUserByEmail(email: String): UserDTO {
        return userRepository.findByEmail(email)?.let { userMapper.toDto(it) }
            ?: userMapper.toDto(addUserByEmail(email))
    }

    private suspend fun addUserByEmail(email: String): User {
        return userRepository.save(User(null, email))
    }

    suspend fun doesUserExist(userId: Long): User {
        return userRepository.findById(userId) ?: throw UserNotFoundException(userId).also {
            logger.error { it.message }
        }
    }


}
