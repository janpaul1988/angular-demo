package org.example.jobjournaler.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull

data class UserDTO(
    val id: Long?,
    @field: Email(message = "Email must be valid.")
    @field:NotNull(message = "Email must not be null.")
    var email: String?,
)


