package org.example.angulardemo.service

import org.example.angulardemo.dto.UserDTO
import org.example.angulardemo.exception.UserNotFoundException
import org.example.angulardemo.mapper.UserMapper
import org.example.angulardemo.repository.UserCrudRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserCrudRepository,
    val userMapper: UserMapper,
) {

    suspend fun getUserByEmail(email: String): UserDTO {
        return userRepository.findByEmail(email)?.let { userMapper.toDto(it) }
            ?: throw UserNotFoundException(email)
    }


}
