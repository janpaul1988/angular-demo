package org.example.angulardemo.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.example.angulardemo.dto.JournalTemplateDTO
import org.example.angulardemo.entity.JournalTemplate
import org.example.angulardemo.exception.JournalTemplateNotFoundException
import org.example.angulardemo.mapper.JournalTemplateMapper
import org.example.angulardemo.repository.JournalTemplateCrudRepository
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
        var journalTemplate = journalTemplateMapper.toEntity(journalTemplateDTO)
        userService.doesUserExist(journalTemplate.userId)

        return transactionalOperator.execute { tx ->
            mono {
                val maxVersion = journalTemplateRepository.findMaxVersionByUserIdAndName(
                    journalTemplate.userId,
                    journalTemplate.name
                ) ?: 0
                journalTemplate.version = maxVersion + 1

                journalTemplateRepository.save(
                    journalTemplate
                )
            }
        }.awaitSingle().let {
            journalTemplateMapper.toDto(it)
        }
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
