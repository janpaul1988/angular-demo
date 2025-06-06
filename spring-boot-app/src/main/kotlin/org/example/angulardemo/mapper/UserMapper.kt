package org.example.angulardemo.mapper

import org.example.angulardemo.dto.UserDTO
import org.example.angulardemo.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toEntity(user: UserDTO): User {
        return User(
            id = user.id,
            name = user.name,
            email = user.email
        )
    }

    fun toDto(user: User): UserDTO {
        return UserDTO(
            id = user.id,
            name = user.name,
            email = user.email
        )
    }

}
