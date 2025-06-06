package org.example.angulardemo.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserDTO(
    val id: Long? = null,
    @field: NotBlank(message = "User name cannot be blank.")
    var name: String,
    @field: Email(message = "Email must be valid.")
    var email: String? = null,
)


