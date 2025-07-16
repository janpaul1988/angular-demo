package org.example.jobjournaler.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.example.jobjournaler.dto.JobDTO
import org.example.jobjournaler.entity.Job
import org.example.jobjournaler.entity.User
import org.example.jobjournaler.exception.JobConstraintViolationException
import org.example.jobjournaler.exception.JobNotFoundException
import org.example.jobjournaler.exception.JobStartDateViolationException
import org.example.jobjournaler.mapper.JobMapper
import org.example.jobjournaler.repository.JobCrudRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class JobServiceUnitTest(
    @RelaxedMockK
    private val jobCrudRepositoryMockk: JobCrudRepository,
    @RelaxedMockK
    private val userServiceMockk: UserService,
    @RelaxedMockK
    private val jobMapperMockk: JobMapper,
    @InjectMockKs
    private val jobService: JobService,
) {

    private val today = LocalDate.now()

    @BeforeTest()
    fun setUpLogging() {
        val logger = LoggerFactory.getLogger("org.example.jobjournaler") as Logger
        logger.level = Level.DEBUG
    }

    @Test
    fun `should add new job`() = runTest {
        // Given
        val userId = 1L
        val startDate = today
        val jobDTO = JobDTO(
            id = null,
            userId = userId,
            title = "testJob",
            description = "testDescription",
            startDate = startDate,
            endDate = null,
            currentJournalTemplateId = null
        )
        val jobEntity = Job(
            id = null,
            userId = userId,
            title = "testJob",
            description = "testDescription",
            startDate = startDate,
            endDate = null,
            currentJournalTemplateId = null
        )
        val savedJobEntity = jobEntity.copy(id = "1")
        val savedJobDTO = jobDTO.copy(id = "1")

        // Mock behavior
        coEvery { userServiceMockk.doesUserExist(userId) } returns mockk<User>()
        coEvery { jobCrudRepositoryMockk.findAllByUserIdAndEndDateIsNull(userId) } returns flowOf()
        coEvery { jobCrudRepositoryMockk.findMaxEndDateByUserId(userId) } returns null
        every { jobMapperMockk.toEntity(jobDTO) } returns jobEntity
        coEvery { jobCrudRepositoryMockk.save(jobEntity) } returns savedJobEntity
        every { jobMapperMockk.toDto(savedJobEntity) } returns savedJobDTO

        // When
        val result = jobService.addJob(jobDTO)

        // Then
        coVerify(exactly = 1) { userServiceMockk.doesUserExist(userId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findAllByUserIdAndEndDateIsNull(userId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findMaxEndDateByUserId(userId) }
        verify(exactly = 1) { jobMapperMockk.toEntity(jobDTO) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.save(jobEntity) }
        verify(exactly = 1) { jobMapperMockk.toDto(savedJobEntity) }
        assertEquals(savedJobDTO, result)
    }

    @Test
    fun `add job for user should throw when there's already a job without end date`() = runTest {
        // Given
        val userId = 1L
        val startDate = today
        val jobDTO = JobDTO(
            id = null,
            userId = userId,
            title = "testJob",
            description = "testDescription",
            startDate = startDate,
            endDate = null,
            currentJournalTemplateId = null
        )
        val existingJobEntity = Job(
            id = "existing-job",
            userId = userId,
            title = "Existing Job",
            description = "This job has no end date",
            startDate = startDate.minusDays(30),
            endDate = null,
            currentJournalTemplateId = null
        )

        // Mock behavior
        every { jobMapperMockk.toEntity(jobDTO) } returns Job(
            id = null,
            userId = userId,
            title = "testJob",
            description = "testDescription",
            startDate = startDate,
            endDate = null,
            currentJournalTemplateId = null
        )
        coEvery { userServiceMockk.doesUserExist(userId) } returns User(id = userId, email = "test@example.com")
        coEvery { jobCrudRepositoryMockk.findAllByUserIdAndEndDateIsNull(userId) } returns flowOf(existingJobEntity)


        // When/Then
        assertFailsWith<JobConstraintViolationException> {
            jobService.addJob(jobDTO)
        }

        // Verify
        coVerify(exactly = 1) { userServiceMockk.doesUserExist(userId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findAllByUserIdAndEndDateIsNull(userId) }
        coVerify(exactly = 0) { jobCrudRepositoryMockk.save(any()) }
    }

    @Test
    fun `add job should throw when start date is before max end date`() = runTest {
        // Given
        val userId = 1L
        val maxEndDate = today.minusDays(10)
        val invalidStartDate = maxEndDate.minusDays(5)  // Start date before max end date

        val jobDTO = JobDTO(
            id = null,
            userId = userId,
            title = "testJob",
            description = "testDescription",
            startDate = invalidStartDate,
            endDate = null,
            currentJournalTemplateId = null
        )

        every { jobMapperMockk.toEntity(jobDTO) } returns Job(
            id = null,
            userId = userId,
            title = "testJob",
            description = "testDescription",
            startDate = invalidStartDate,
            endDate = null,
            currentJournalTemplateId = null
        )
        coEvery { userServiceMockk.doesUserExist(userId) } returns User(id = userId, email = "test@example.com")
        coEvery { jobCrudRepositoryMockk.findAllByUserIdAndEndDateIsNull(userId) } returns flowOf()
        coEvery { jobCrudRepositoryMockk.findMaxEndDateByUserId(userId) } returns maxEndDate

        // When/Then
        assertFailsWith<JobStartDateViolationException> {
            jobService.addJob(jobDTO)
        }

        // Verify
        coVerify(exactly = 1) { userServiceMockk.doesUserExist(userId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findAllByUserIdAndEndDateIsNull(userId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findMaxEndDateByUserId(userId) }
        coVerify(exactly = 0) { jobCrudRepositoryMockk.save(any()) }
    }

    @Test
    fun `should get all jobs for a certain user`() = runTest {
        // Given
        val userId = 1L
        val job1 = Job(
            id = "1",
            userId = userId,
            title = "Job 1",
            description = "Description 1",
            startDate = today.minusDays(30),
            endDate = today.minusDays(15),
            currentJournalTemplateId = null
        )
        val job2 = Job(
            id = "2",
            userId = userId,
            title = "Job 2",
            description = "Description 2",
            startDate = today.minusDays(10),
            endDate = null,
            currentJournalTemplateId = null
        )
        val jobFlow = flowOf(job1, job2)

        val jobDTO1 = JobDTO(
            id = "1",
            userId = userId,
            title = "Job 1",
            description = "Description 1",
            startDate = today.minusDays(30),
            endDate = today.minusDays(15),
            currentJournalTemplateId = null
        )
        val jobDTO2 = JobDTO(
            id = "2",
            userId = userId,
            title = "Job 2",
            description = "Description 2",
            startDate = today.minusDays(10),
            endDate = null,
            currentJournalTemplateId = null
        )
        val jobDTOList = listOf(jobDTO1, jobDTO2)

        coEvery { jobCrudRepositoryMockk.findAllByUserId(userId) } returns jobFlow
        every { jobMapperMockk.toDto(job1) } returns jobDTO1
        every { jobMapperMockk.toDto(job2) } returns jobDTO2

        // When
        val result = jobService.getAllJobsForUser(userId).toList()

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findAllByUserId(userId) }
        verify(exactly = 1) { jobMapperMockk.toDto(job1) }
        verify(exactly = 1) { jobMapperMockk.toDto(job2) }

        assertEquals(jobDTOList, result)
    }


    @Test
    fun `should update job`() = runTest {
        // Given
        val jobId = "1"
        val userId = 1L

        val existingJob = Job(
            id = jobId,
            userId = userId,
            title = "Original Title",
            description = "Original Description",
            startDate = today.minusDays(10),
            endDate = null,
            currentJournalTemplateId = null
        )

        val jobDTO = JobDTO(
            id = jobId,
            userId = userId,
            title = "Updated Title",
            description = "Updated Description",
            startDate = today.minusDays(10),
            endDate = today,
            currentJournalTemplateId = "template-123"
        )

        val updatedJob = Job(
            id = jobId,
            userId = userId,
            title = "Updated Title",
            description = "Updated Description",
            startDate = today.minusDays(10),
            endDate = today,
            currentJournalTemplateId = "template-123"
        )

        val updatedJobDTO = jobDTO.copy()

        coEvery { jobCrudRepositoryMockk.findById(jobId) } returns existingJob
        coEvery { jobCrudRepositoryMockk.save(any()) } returns updatedJob
        every { jobMapperMockk.toEntity(jobDTO) } returns updatedJob
        every { jobMapperMockk.toDto(updatedJob) } returns updatedJobDTO

        // When
        val result = jobService.updateJob(jobDTO)

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findById(jobId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.save(any()) }
        verify(exactly = 1) { jobMapperMockk.toDto(updatedJob) }
        assertEquals(updatedJobDTO, result)
    }

    @Test
    fun `update job that does not exist should throw`() = runTest {
        // Given
        val jobId = "non-existing-id"
        val userId = 1L

        val jobDTO = JobDTO(
            id = jobId,
            userId = userId,
            title = "Updated Title",
            description = "Updated Description",
            startDate = today.minusDays(10),
            endDate = today,
            currentJournalTemplateId = null
        )

        coEvery { jobCrudRepositoryMockk.findById(jobId) } returns null

        // When/Then
        assertFailsWith<JobNotFoundException> {
            jobService.updateJob(jobDTO)
        }

        // Verify
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findById(jobId) }
        coVerify(exactly = 0) { jobCrudRepositoryMockk.save(any()) }
        verify(exactly = 0) { jobMapperMockk.toDto(any()) }
    }

    @Test
    fun `should delete job`() = runTest {
        // Given
        val jobId = "1"

        val existingJob = Job(
            id = jobId,
            userId = 1L,
            title = "Job to Delete",
            description = "This job will be deleted",
            startDate = today.minusDays(10),
            endDate = null,
            currentJournalTemplateId = null
        )

        coEvery { jobCrudRepositoryMockk.findById(jobId) } returns existingJob
        coEvery { jobCrudRepositoryMockk.deleteById(jobId) } returns Unit

        // When
        jobService.deleteJob(jobId)

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findById(jobId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.deleteById(jobId) }
    }

    @Test
    fun `delete job that does not exist should throw`() = runTest {
        // Given
        val jobId = "non-existing-id"

        coEvery { jobCrudRepositoryMockk.findById(jobId) } returns null

        // When/Then
        assertFailsWith<JobNotFoundException> {
            jobService.deleteJob(jobId)
        }

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findById(jobId) }
        coVerify(exactly = 0) { jobCrudRepositoryMockk.deleteById(any()) }
    }
}
