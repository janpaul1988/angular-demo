package org.example.jobjournaler.dto

import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class JournalTemplateDTO(
    val id: String?,
    @field:NotNull(message = "userId cannot be null.")
    val userId: Long?,
    @field:NotBlank(message = "name cannot be blank.")
    val name: String?,
    val version: Int?,
    @field: NotNull(message = "content cannot be null.")
    val content: JsonNode,
)
