package org.example.jobjournaler.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class JobDTO(
    val id: String?,
    val userId: Long,
    @field: NotBlank(message = "Job title cannot be blank.")
    var title: String?,
    var description: String?,
    @field:NotNull(message = "Job startDate cannot be null.")
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    var currentJournalTemplateId: String?,
) {
    @AssertTrue(message = "End date must be equal to or after start date")
    @JsonIgnore
    fun isEndDateValid(): Boolean {
        // If endDate is null, there's nothing to validate (it's valid)
        return endDate == null || !endDate.isBefore(startDate)
    }
}

