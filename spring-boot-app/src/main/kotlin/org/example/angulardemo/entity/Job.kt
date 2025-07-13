package org.example.angulardemo.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate


@Table(name = "job")
data class Job(
    @Id
    val id: String?,
    val userId: Long,
    var title: String,
    var description: String?,
    var startDate: LocalDate,
    var endDate: LocalDate?,
    @Column("current_journal_template_id")
    var currentJournalTemplateId: String?,
)
