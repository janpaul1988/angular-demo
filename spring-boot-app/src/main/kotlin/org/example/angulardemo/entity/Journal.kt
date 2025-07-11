package org.example.angulardemo.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "journal")
data class Journal(
    @Id
    val id: String?,
    val jobId: String,
    val templateId: String,
    val year: Int,
    val week: Int,
    var content: String,
)
