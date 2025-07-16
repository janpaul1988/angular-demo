package org.example.jobjournaler.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.jobjournaler.dto.JournalTemplateDTO
import org.example.jobjournaler.entity.JournalTemplate
import org.springframework.stereotype.Component

@Component
class JournalTemplateMapper(private val objectMapper: ObjectMapper) {

    fun toEntity(journalTemplateDTO: JournalTemplateDTO): JournalTemplate {
        return JournalTemplate(
            id = journalTemplateDTO.id,
            userId = journalTemplateDTO.userId!!,
            name = journalTemplateDTO.name!!,
            version = journalTemplateDTO.version!!,
            content = objectMapper.writeValueAsString(journalTemplateDTO.content)
        )
    }

    fun toDto(journalTemplate: JournalTemplate): JournalTemplateDTO {
        return JournalTemplateDTO(
            id = journalTemplate.id!!,
            userId = journalTemplate.userId,
            name = journalTemplate.name,
            version = journalTemplate.version,
            content = objectMapper.readTree(journalTemplate.content),
        )
    }

}
