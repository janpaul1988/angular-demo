package org.example.angulardemo.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class JournalTemplateDTO(
    val id: String?,
    @field:NotNull(message = "userId cannot be null.")
    val userId: Long?,
    @field:NotBlank(message = "name cannot be blank.")
    val name: String?,
    val version: Int?,
    @field: NotBlank(message = "content cannot be blank.")
    val content: String?,
)
