package org.example.angulardemo.mapper

import org.example.angulardemo.dto.JournalTemplateDTO
import org.example.angulardemo.entity.JournalTemplate
import org.springframework.stereotype.Component

@Component
class JournalTemplateMapper {

    fun toEntity(journalTemplateDTO: JournalTemplateDTO): JournalTemplate {
        return JournalTemplate(
            id = journalTemplateDTO.id,
            userId = journalTemplateDTO.userId!!,
            name = journalTemplateDTO.name!!,
            version = journalTemplateDTO.version!!,
            content = journalTemplateDTO.content!!
        )
    }

    fun toDto(journalTemplate: JournalTemplate): JournalTemplateDTO {
        return JournalTemplateDTO(
            id = journalTemplate.id!!,
            userId = journalTemplate.userId,
            name = journalTemplate.name,
            version = journalTemplate.version,
            content = journalTemplate.content,
        )
    }

}
