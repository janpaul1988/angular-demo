package org.example.angulardemo.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.angulardemo.dto.JournalDTO
import org.example.angulardemo.entity.Journal
import org.springframework.stereotype.Component

@Component
class JournalMapper(private val objectMapper: ObjectMapper) {

    fun toEntity(journalDTO: JournalDTO): Journal {
        return Journal(
            id = journalDTO.id,
            jobId = journalDTO.jobId!!,
            templateId = journalDTO.templateId!!,
            year = journalDTO.year!!,
            week = journalDTO.week,
            content = objectMapper.writeValueAsString(journalDTO.content),
        )
    }

    fun toDto(journal: Journal): JournalDTO {
        return JournalDTO(
            id = journal.id,
            jobId = journal.jobId,
            templateId = journal.templateId,
            year = journal.year,
            week = journal.week,
            content = objectMapper.readTree(journal.content),
        )
    }

}
