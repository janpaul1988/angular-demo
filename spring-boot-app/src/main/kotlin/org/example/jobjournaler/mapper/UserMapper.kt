package org.example.jobjournaler.mapper

import org.example.jobjournaler.dto.UserDTO
import org.example.jobjournaler.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toEntity(user: UserDTO): User {
        return User(
            id = user.id,
            email = user.email!!
        )
    }

    fun toDto(user: User): UserDTO {
        return UserDTO(
            id = user.id,
            email = user.email
        )
    }

}
