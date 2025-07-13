package org.example.angulardemo.mapper

import org.example.angulardemo.dto.JournalDTO
import org.example.angulardemo.entity.Journal
import org.springframework.stereotype.Component

@Component
class JournalMapper {

    fun toEntity(journalDTO: JournalDTO): Journal {
        return Journal(
            id = journalDTO.id,
            jobId = journalDTO.jobId!!,
            templateId = journalDTO.templateId!!,
            year = journalDTO.year!!,
            week = journalDTO.week,
            content = journalDTO.content,
        )
    }

    fun toDto(journal: Journal): JournalDTO {
        return JournalDTO(
            id = journal.id,
            jobId = journal.jobId,
            templateId = journal.templateId,
            year = journal.year,
            week = journal.week,
            content = journal.content,
        )
    }

}
