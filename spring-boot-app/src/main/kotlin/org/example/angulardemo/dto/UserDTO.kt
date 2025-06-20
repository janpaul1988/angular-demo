package org.example.angulardemo.dto

import jakarta.validation.constraints.Email

data class UserDTO(
    val id: Long? = null,
    @field: Email(message = "Email must be valid.")
    var email: String? = null,
)


