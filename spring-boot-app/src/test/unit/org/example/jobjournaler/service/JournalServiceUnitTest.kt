package org.example.jobjournaler.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.example.jobjournaler.dto.JournalDTO
import org.example.jobjournaler.entity.Job
import org.example.jobjournaler.entity.Journal
import org.example.jobjournaler.exception.JournalNotFoundException
import org.example.jobjournaler.mapper.JournalMapper
import org.example.jobjournaler.repository.JournalCrudRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class JournalServiceUnitTest(
    @RelaxedMockK
    private val journalRepositoryMockk: JournalCrudRepository,
    @RelaxedMockK
    private val journalMapperMockk: JournalMapper,
    @RelaxedMockK
    private val jobServiceMockk: JobService,
    @InjectMockKs
    private val journalService: JournalService,
) {
    private lateinit var objectMapper: ObjectMapper
    
    @BeforeTest()
    fun setup() {
        val logger = LoggerFactory.getLogger("org.example.jobjournaler") as Logger
        logger.level = Level.DEBUG

        objectMapper = ObjectMapper()
    }

    @Test
    fun `should get journal by year, week and job id`() = runTest {
        // Given
        val year = 2025
        val week = 25
        val jobId = "job-123"

        val journal = Journal(
            id = "journal-1",
            jobId = jobId,
            templateId = "template-1",
            year = year,
            week = week,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Test answer\"}]}"
        )

        val journalDTO = JournalDTO(
            id = "journal-1",
            jobId = jobId,
            templateId = "template-1",
            year = year,
            week = week,
            content = objectMapper.readTree("{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Test answer\"}]}")
        )

        coEvery { journalRepositoryMockk.findByYearAndWeekAndJobId(year, week, jobId) } returns journal
        every { journalMapperMockk.toDto(journal) } returns journalDTO

        // When
        val result = journalService.getJournalByYearWeekAndJobId(year, week, jobId)

        // Then
        coVerify(exactly = 1) { journalRepositoryMockk.findByYearAndWeekAndJobId(year, week, jobId) }
        verify(exactly = 1) { journalMapperMockk.toDto(journal) }
        assertEquals(journalDTO, result)
    }

    @Test
    fun `should throw when journal not found by year, week and job id`() = runTest {
        // Given
        val year = 2025
        val week = 25
        val jobId = "job-123"

        coEvery { journalRepositoryMockk.findByYearAndWeekAndJobId(year, week, jobId) } returns null

        // When & Then
        assertFailsWith<JournalNotFoundException> {
            journalService.getJournalByYearWeekAndJobId(year, week, jobId)
        }

        coVerify(exactly = 1) { journalRepositoryMockk.findByYearAndWeekAndJobId(year, week, jobId) }
        verify(exactly = 0) { journalMapperMockk.toDto(any()) }
    }

    @Test
    fun `should get journals by job id`() = runTest {
        // Given
        val jobId = "job-123"

        val journal1 = Journal(
            id = "journal-1",
            jobId = jobId,
            templateId = "template-1",
            year = 2025,
            week = 25,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Week 25 answer\"}]}"
        )

        val journal2 = Journal(
            id = "journal-2",
            jobId = jobId,
            templateId = "template-1",
            year = 2025,
            week = 26,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Week 26 answer\"}]}"
        )

        val journalFlow = flowOf(journal1, journal2)

        val journalDTO1 = JournalDTO(
            id = "journal-1",
            jobId = jobId,
            templateId = "template-1",
            year = 2025,
            week = 25,
            content = objectMapper.readTree("{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Week 25 answer\"}]}")
        )

        val journalDTO2 = JournalDTO(
            id = "journal-2",
            jobId = jobId,
            templateId = "template-1",
            year = 2025,
            week = 26,
            content = objectMapper.readTree("{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Week 26 answer\"}]}")
        )

        coEvery { journalRepositoryMockk.findByJobId(jobId) } returns journalFlow
        every { journalMapperMockk.toDto(journal1) } returns journalDTO1
        every { journalMapperMockk.toDto(journal2) } returns journalDTO2

        // When
        val result = journalService.getJournalsByJobId(jobId).toList()

        // Then
        coVerify(exactly = 1) { journalRepositoryMockk.findByJobId(jobId) }
        verify(exactly = 1) { journalMapperMockk.toDto(journal1) }
        verify(exactly = 1) { journalMapperMockk.toDto(journal2) }
        assertEquals(listOf(journalDTO1, journalDTO2), result)
    }

    @Test
    fun `should save journal`() = runTest {
        // Given
        val journalDTO = JournalDTO(
            id = null,
            jobId = "job-123",
            templateId = "template-1",
            year = 2025,
            week = 27,
            content = objectMapper.readTree("{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"New answer\"}]}")
        )

        val journal = Journal(
            id = null,
            jobId = "job-123",
            templateId = "template-1",
            year = 2025,
            week = 27,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"New answer\"}]}"
        )

        val savedJournal = journal.copy(id = "journal-3")
        val savedJournalDTO = journalDTO.copy(id = "journal-3")

        every { journalMapperMockk.toEntity(journalDTO) } returns journal
        coEvery { jobServiceMockk.doesJobExist(journal.jobId) } returns mockk<Job>()
        coEvery { journalRepositoryMockk.save(journal) } returns savedJournal
        every { journalMapperMockk.toDto(savedJournal) } returns savedJournalDTO

        // When
        val result = journalService.saveJournal(journalDTO)

        // Then
        verify(exactly = 1) { journalMapperMockk.toEntity(journalDTO) }
        coVerify(exactly = 1) { jobServiceMockk.doesJobExist(journal.jobId) }
        coVerify(exactly = 1) { journalRepositoryMockk.save(journal) }
        verify(exactly = 1) { journalMapperMockk.toDto(savedJournal) }
        assertEquals(savedJournalDTO, result)
    }

    @Test
    fun `should update journal with new templateId`() = runTest {
        // Given
        val journalId = "journal-1"
        val oldTemplateId = "template-1"
        val newTemplateId = "template-2"

        val journalDTO = JournalDTO(
            id = journalId,
            jobId = "job-123",
            templateId = newTemplateId, // New template ID
            year = 2025,
            week = 25,
            content = objectMapper.readTree("{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Updated answer\"}]}")
        )

        val existingJournal = Journal(
            id = journalId,
            jobId = "job-123",
            templateId = oldTemplateId, // Old template ID
            year = 2025,
            week = 25,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Original answer\"}]}"
        )

        val journalToUpdate = Journal(
            id = journalId,
            jobId = "job-123",
            templateId = newTemplateId, // New template ID
            year = 2025,
            week = 25,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Updated answer\"}]}"
        )

        // Journal after update should have both new content and new template ID
        val updatedJournal = existingJournal.copy(
            content = journalToUpdate.content,
            templateId = newTemplateId
        )
        
        val updatedJournalDTO = journalDTO.copy()

        coEvery { journalRepositoryMockk.findById(journalId) } returns existingJournal
        every { journalMapperMockk.toEntity(journalDTO) } returns journalToUpdate
        coEvery { journalRepositoryMockk.save(any()) } returns updatedJournal
        every { journalMapperMockk.toDto(updatedJournal) } returns updatedJournalDTO

        // When
        val result = journalService.updateJournal(journalDTO)

        // Then
        coVerify(exactly = 1) { journalRepositoryMockk.findById(journalId) }
        verify(exactly = 1) { journalMapperMockk.toEntity(journalDTO) }
        coVerify(exactly = 1) { journalRepositoryMockk.save(any()) }
        verify(exactly = 1) { journalMapperMockk.toDto(updatedJournal) }

        // Verify that both content and templateId are updated
        assertEquals(updatedJournalDTO, result)
        assertEquals(newTemplateId, result.templateId) // Verify templateId is updated
    }

    @Test
    fun `should throw when updating non-existing journal`() = runTest {
        // Given
        val journalId = "non-existing-id"

        val journalDTO = JournalDTO(
            id = journalId,
            jobId = "job-123",
            templateId = "template-1",
            year = 2025,
            week = 25,
            content = objectMapper.readTree("{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Some answer\"}]}")
        )

        coEvery { journalRepositoryMockk.findById(journalId) } returns null

        // When & Then
        assertFailsWith<JournalNotFoundException> {
            journalService.updateJournal(journalDTO)
        }

        coVerify(exactly = 1) { journalRepositoryMockk.findById(journalId) }
        verify(exactly = 0) { journalMapperMockk.toEntity(any()) }
        coVerify(exactly = 0) { journalRepositoryMockk.save(any()) }
    }

    @Test
    fun `should delete journal`() = runTest {
        // Given
        val journalId = "journal-1"

        val journal = Journal(
            id = journalId,
            jobId = "job-123",
            templateId = "template-1",
            year = 2025,
            week = 25,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Some answer\"}]}"
        )

        coEvery { journalRepositoryMockk.findById(journalId) } returns journal
        coEvery { journalRepositoryMockk.deleteById(journalId) } returns Unit

        // When
        journalService.deleteJournal(journalId)

        // Then
        coVerify(exactly = 1) { journalRepositoryMockk.findById(journalId) }
        coVerify(exactly = 1) { journalRepositoryMockk.deleteById(journalId) }
    }

    @Test
    fun `should throw when deleting non-existing journal`() = runTest {
        // Given
        val journalId = "non-existing-id"

        coEvery { journalRepositoryMockk.findById(journalId) } returns null

        // When & Then
        assertFailsWith<JournalNotFoundException> {
            journalService.deleteJournal(journalId)
        }

        coVerify(exactly = 1) { journalRepositoryMockk.findById(journalId) }
        coVerify(exactly = 0) { journalRepositoryMockk.deleteById(any()) }
    }
}
