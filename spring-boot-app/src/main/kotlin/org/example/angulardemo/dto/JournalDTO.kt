package org.example.angulardemo.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class JournalDTO(
    val id: String?,
    @field:NotBlank(message = "jobId cannot be blank.")
    val jobId: String?,
    @field:NotBlank(message = "templateId cannot be blank.")
    val templateId: String?,
    @field:NotNull(message = "year cannot be null.")
    val year: Int?,
    @field:NotNull(message = "week cannot be null.")
    @field:Min(value = 1, message = "week must be equal to or higher then 1.")
    @field:Max(value = 53, message = "week must be equal to or lower then 53.")
    val week: Int,
    @field:NotNull(message = "description cannot be blank.")
    val content: String,

    )
