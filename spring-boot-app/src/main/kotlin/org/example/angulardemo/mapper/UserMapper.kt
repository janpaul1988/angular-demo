package org.example.angulardemo.mapper

import org.example.angulardemo.dto.UserDTO
import org.example.angulardemo.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toEntity(user: UserDTO): User {
        return User(
            id = user.id,
            email = user.email
        )
    }

    fun toDto(user: User): UserDTO {
        return UserDTO(
            id = user.id,
            email = user.email
        )
    }

}
