package org.example.jobjournaler.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.jobjournaler.dto.JournalTemplateDTO
import org.example.jobjournaler.entity.JournalTemplate
import org.example.jobjournaler.exception.JournalTemplateNotFoundException
import org.example.jobjournaler.mapper.JournalTemplateMapper
import org.example.jobjournaler.repository.JournalTemplateCrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

private val logger = KotlinLogging.logger {}

@Service
class JournalTemplateService(
    private val journalTemplateRepository: JournalTemplateCrudRepository,
    private val journalTemplateMapper: JournalTemplateMapper,
    private val transactionalOperator: TransactionalOperator,
    private val userService: UserService,
) {
    suspend fun findJournalTemplateDtoById(journalTemplateId: String): JournalTemplateDTO {
        return findJournalTemplateById(journalTemplateId)
            .let { journalTemplateMapper.toDto(it) }
    }

    suspend fun findJournalTemplatesByUserId(userId: Long): Flow<JournalTemplateDTO> {
        return journalTemplateRepository.findAllByUserId(userId)
            .map {
                journalTemplateMapper.toDto(it)
            }
    }

    suspend fun createOrUpdateTemplate(journalTemplateDTO: JournalTemplateDTO): JournalTemplateDTO {
        val journalTemplate = journalTemplateMapper.toEntity(journalTemplateDTO)

        // First check if user exists
        userService.doesUserExist(journalTemplate.userId)

        // Get max version (handling null case)
        val maxVersion = journalTemplateRepository.findMaxVersionByUserIdAndName(
            journalTemplate.userId,
            journalTemplate.name
        ) ?: 0

        // Set the incremented version
        journalTemplate.version = maxVersion + 1

        // Save with the new version
        val savedTemplate = journalTemplateRepository.save(journalTemplate)

        // Convert to DTO and return
        return journalTemplateMapper.toDto(savedTemplate)
    }

    suspend fun deleteJournalTemplate(journalTemplateId: String): Any {
        findJournalTemplateById(journalTemplateId)
        return journalTemplateRepository.deleteById(journalTemplateId)
    }

    private suspend fun findJournalTemplateById(journalTemplateId: String): JournalTemplate {
        return journalTemplateRepository.findById(journalTemplateId)
            ?: throw JournalTemplateNotFoundException(
                journalTemplateId
            ).also {
                logger.error { it.message }
            }
    }

}
