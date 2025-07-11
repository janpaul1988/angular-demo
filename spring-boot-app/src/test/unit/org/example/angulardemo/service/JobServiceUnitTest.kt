package org.example.angulardemo.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.example.angulardemo.configuration.JobConfiguration
import org.example.angulardemo.dto.JobDTO
import org.example.angulardemo.entity.Job
import org.example.angulardemo.entity.User
import org.example.angulardemo.exception.JobNotFoundException
import org.example.angulardemo.exception.UserNotFoundException
import org.example.angulardemo.mapper.JobMapper
import org.example.angulardemo.repository.JobCrudRepository
import org.example.angulardemo.repository.UserCrudRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class JobServiceUnitTest(
    @RelaxedMockK
    private val jobCrudRepositoryMockk: JobCrudRepository,
    @RelaxedMockK
    private val userCrudRepositoryMockk: UserCrudRepository,
    @RelaxedMockK
    private val jobMapperMockk: JobMapper,
    @RelaxedMockK
    private val jobConfigurationMockk: JobConfiguration,
    @InjectMockKs
    private val jobService: JobService,
) {

    @BeforeTest()
    fun setUpLogging() {
        val logger = LoggerFactory.getLogger("org.example.angulardemo") as Logger
        logger.level = Level.DEBUG
    }

    @Test
    fun `should add new job`() = runTest {
        // Given
        val jobDTO = mockk<JobDTO>()
        val jobEntity = mockk<Job>(relaxed = true)
        val savedJobEntity = mockk<Job>()
        val savedJobDTO = mockk<JobDTO>()
        val userId = 1L

        // Mock behavior
        every { jobConfigurationMockk.maxInserts } returns 10
        coEvery { userCrudRepositoryMockk.findById(userId) } returns mockk<User>()
        every { jobMapperMockk.toEntity(jobDTO) } returns jobEntity
        coEvery { jobCrudRepositoryMockk.findMaxExternalIdByUserId(userId) } returns 1L
        every { jobConfigurationMockk.externalIdStartingValue } returns 0L
        every { jobConfigurationMockk.externalIdIncrementValue } returns 1L
        coEvery { jobCrudRepositoryMockk.save(jobEntity) } returns savedJobEntity
        every { jobMapperMockk.toDto(savedJobEntity) } returns savedJobDTO

        // Capture the externalId that is set.
        val externalIdSlot = slot<Long>()
        every { jobEntity.externalId = capture(externalIdSlot) } just Runs

        // When
        val result = jobService.addJob(userId, jobDTO)

        // Then
        coVerify(exactly = 1) { userCrudRepositoryMockk.findById(userId) }
        verify(exactly = 1) { jobMapperMockk.toEntity(jobDTO) }

        coVerify(exactly = 1) { jobCrudRepositoryMockk.findMaxExternalIdByUserId(userId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.save(jobEntity) }
        verify(exactly = 1) { jobMapperMockk.toDto(savedJobEntity) }
        assertEquals(savedJobDTO, result)
        assertEquals(2L, externalIdSlot.captured) // <-- This checks the incremented value
    }

    @Test
    fun `add new job for user that does not exist should throw`() = runTest {
        // Given
        val jobDTO = mockk<JobDTO>()
        val userId = 1L

        every { jobConfigurationMockk.maxInserts } returns 10
        coEvery { userCrudRepositoryMockk.findById(userId) } returns null
        assertFailsWith<UserNotFoundException> { jobService.addJob(userId, jobDTO) }

        coVerify(exactly = 1) { userCrudRepositoryMockk.findById(userId) }
        verify { jobMapperMockk wasNot called }
        coVerify { jobCrudRepositoryMockk wasNot called }
    }

    @Test
    fun `add new job should reattempt insertion the correct number of times and when the amount-of-retries reaches zero should throw`() =
        runTest {
            // Given
            val jobDTO = mockk<JobDTO>()
            val jobEntity = mockk<Job>(relaxed = true)
            val userId = 1L

            every { jobConfigurationMockk.maxInserts } returns 10
            coEvery { userCrudRepositoryMockk.findById(userId) } returns mockk<User>()
            every { jobMapperMockk.toEntity(jobDTO) } returns jobEntity
            coEvery { jobCrudRepositoryMockk.findMaxExternalIdByUserId(userId) } returns 0L
            every { jobConfigurationMockk.externalIdStartingValue } returns 0L
            every { jobConfigurationMockk.externalIdIncrementValue } returns 1L
            coEvery { jobCrudRepositoryMockk.save(jobEntity) } throws DuplicateKeyException("Error, key exists")


            // When
            assertFailsWith<IllegalStateException> { jobService.addJob(userId, jobDTO) }

            // Then
            coVerify(exactly = 10) { userCrudRepositoryMockk.findById(userId) }
            verify(exactly = 10) { jobMapperMockk.toEntity(jobDTO) }
            coVerify(exactly = 10) { jobCrudRepositoryMockk.findMaxExternalIdByUserId(userId) }
            coVerify(exactly = 10) { jobCrudRepositoryMockk.save(jobEntity) }
            verify(exactly = 0) { jobMapperMockk.toDto(any()) }
        }

    @Test
    fun `should get all jobs for a certain user`() = runTest {
        // Given
        val jobFlow = flowOf(mockk<Job>(), mockk<Job>())
        val jobList = jobFlow.toList()
        val jobDTOLists = listOf(mockk<JobDTO>(), mockk<JobDTO>())
        val userId = 1L
        coEvery { jobCrudRepositoryMockk.findAllByUserId(userId) } returns jobFlow

        // Pair jobs with their corresponding DTOs and mock the mapper behavior
        jobList.zip(jobDTOLists).forEach { (job, jobDTO) ->
            every { jobMapperMockk.toDto(job) } returns jobDTO
        }

        // When
        val result = jobService.getAllJobsForUser(userId).toList()

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findAllByUserId(userId) }
        verify(exactly = jobList.size) { jobMapperMockk.toDto(any()) }

        assertEquals(jobDTOLists, result)
    }


    @Test
    fun `should update job`() = runTest {
        val jobId = 1L
        val userId = 1L
        val jobDTO = JobDTO(jobId, userId, title = "testUpdate", description = "testUpdate")
        val jobEntity = Job(null, userId, jobId, title = "test", description = "update")
        val jobEntityToSave = jobEntity.copy(
            title = jobDTO.title,
            description = jobDTO.description
        )
        val savedJobEntity = mockk<Job>()
        val savedJobDTO = mockk<JobDTO>()

        coEvery { jobCrudRepositoryMockk.findByUserIdAndExternalId(userId, jobId) } returns jobEntity
        coEvery {
            jobCrudRepositoryMockk.save(
                jobEntityToSave
            )
        } returns savedJobEntity

        every { jobMapperMockk.toDto(savedJobEntity) } returns savedJobDTO

        // When
        val result = jobService.updateJob(userId, jobId, jobDTO)

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findByUserIdAndExternalId(userId, jobId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.save(jobEntityToSave) }
        verify(exactly = 1) { jobMapperMockk.toDto(savedJobEntity) }

        assertEquals(savedJobDTO, result)
    }

    @Test
    fun `update job that does not exist should throw`() = runTest {
        // Given
        val userId = 1L
        val jobId = 1L
        coEvery {
            jobCrudRepositoryMockk.findByUserIdAndExternalId(
                userId,
                jobId
            )
        } returns null

        // When
        assertFailsWith<JobNotFoundException> {
            jobService.updateJob(userId, jobId, mockk<JobDTO>())
        }

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findByUserIdAndExternalId(userId, jobId) }
        coVerify(exactly = 0) { jobCrudRepositoryMockk.save(any()) }
        verify { jobMapperMockk wasNot called }
    }

    @Test
    fun `should delete job`() = runTest {
        // Given
        val userId = 1L
        val jobId = 1L
        coEvery { jobCrudRepositoryMockk.findByUserIdAndExternalId(userId, jobId) } returns mockk<Job>()

        // When
        jobService.deleteJob(userId, jobId)

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findByUserIdAndExternalId(userId, jobId) }
        coVerify(exactly = 1) { jobCrudRepositoryMockk.deleteByUserIdAndExternalId(userId, jobId) }
    }

    @Test
    fun `delete job that does not exist should throw`() = runTest {
        // Given
        val userId = 1L
        val jobId = 1L
        coEvery { jobCrudRepositoryMockk.findByUserIdAndExternalId(userId, jobId) } returns null

        // When
        assertFailsWith<JobNotFoundException> {
            jobService.deleteJob(userId, jobId)
        }

        // Then
        coVerify(exactly = 1) { jobCrudRepositoryMockk.findByUserIdAndExternalId(userId, jobId) }
        coVerify(exactly = 0) { jobCrudRepositoryMockk.deleteByUserIdAndExternalId(userId, jobId) }
    }
}
