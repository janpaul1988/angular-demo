package org.example.jobjournaler.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.jobjournaler.dto.JournalDTO
import org.example.jobjournaler.entity.Journal
import org.example.jobjournaler.exception.JournalNotFoundException
import org.example.jobjournaler.mapper.JournalMapper
import org.example.jobjournaler.repository.JournalCrudRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class JournalService(
    val journalRepository: JournalCrudRepository,
    val journalMapper: JournalMapper,
    private val jobService: JobService,
) {

    suspend fun getJournalByYearWeekAndJobId(year: Int, week: Int, jobId: String): JournalDTO {
        return journalRepository.findByYearAndWeekAndJobId(year, week, jobId)
            ?.let {
                journalMapper.toDto(it)
            } ?: throw JournalNotFoundException(
            year, week, jobId
        ).also {
            logger.error { it.message }
        }
    }


    suspend fun getJournalsByJobId(jobId: String): Flow<JournalDTO> {
        return journalRepository.findByJobId(jobId)
            .map { journalMapper.toDto(it) }
    }

    suspend fun saveJournal(journalDTO: JournalDTO): JournalDTO {
        var journal = journalMapper.toEntity(journalDTO);
        jobService.doesJobExist(journal.jobId)

        return journalMapper.toDto(journalRepository.save(journal))
    }

    suspend fun updateJournal(journalDTO: JournalDTO): JournalDTO {
        return findJournal(journalDTO.id!!)
            .let {
                var journalToUpdateTo = journalMapper.toEntity(journalDTO);
                it.content = journalToUpdateTo.content
                it.templateId = journalToUpdateTo.templateId
                journalRepository.save(it)
            }.let {
                journalMapper.toDto(it)
            }
    }

    suspend fun deleteJournal(journalId: String) {
        findJournal(journalId)
        journalRepository.deleteById(journalId)
    }

    suspend fun findJournal(journalId: String): Journal {
        return journalRepository.findById(journalId) ?: throw JournalNotFoundException(
            journalId
        ).also {
            logger.error { it.message }
        }
    }

}
