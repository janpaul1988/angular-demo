package org.example.jobjournaler.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "journal_template")
data class JournalTemplate(
    @Id
    val id: String?,
    val userId: Long,
    val name: String,
    var version: Int?,
    var content: String,
)
