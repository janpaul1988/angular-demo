package org.example.jobjournaler.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import org.example.jobjournaler.dto.JobDTO
import org.example.jobjournaler.exception.JobNotFoundException
import org.example.jobjournaler.service.JobService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate

@WebFluxTest(controllers = [JobController::class])
@AutoConfigureWebTestClient
class JobControllerUnitTest(
    @Autowired
    val webTestClient: WebTestClient,
) {
    @MockkBean(relaxed = true)
    lateinit var jobService: JobService

    private val startDate = LocalDate.now()
    private val endDate = startDate.plusMonths(1)

    @Test
    fun `should add job for user`() {
        val userId = 1L
        val jobToAdd = JobDTO(
            id = null,
            userId = userId,
            title = "testname",
            description = "testdescription",
            startDate = startDate,
            endDate = endDate,
            currentJournalTemplateId = null
        )
        val jobAdded = jobToAdd.copy(id = "1")
        // Given

        coEvery { jobService.addJob(jobToAdd) } returns jobAdded

        webTestClient.post()                                                           // When
            .uri("/jobs")
            .bodyValue(jobToAdd)
            .exchange()
            .expectStatus().isCreated                                                  // Then
            .expectBody(JobDTO::class.java)
            .isEqualTo(jobAdded)

        coVerify(exactly = 1) { jobService.addJob(jobToAdd) }
    }

    @Test
    fun `should not add job for user without title`() {
        val jobWithoutTitle = JobDTO(
            id = null,
            userId = 1L,
            title = "",
            description = "testdescription",
            startDate = startDate,
            endDate = endDate,
            currentJournalTemplateId = null
        )

        webTestClient.post()                                                   // When
            .uri("/jobs")
            .bodyValue(jobWithoutTitle)
            .exchange()
            .expectStatus().isBadRequest                                       // Then

        coVerify { jobService wasNot Called }
    }

    @Test
    fun `should update job`() {
        val jobId = "1"
        val userId = 1L
        val jobToUpdate = JobDTO(
            id = jobId,
            userId = userId,
            title = "testName",
            description = "testdescription",
            startDate = startDate,
            endDate = endDate,
            currentJournalTemplateId = null
        )

        val updatedJob = jobToUpdate.copy(
            title = "updatedName",
            description = "updatedDescription"
        )

        coEvery { jobService.updateJob(jobToUpdate) } returns updatedJob

        webTestClient.put()
            .uri("/jobs")
            .bodyValue(jobToUpdate)
            .exchange()
            .expectStatus().isOk                                                              // Then
            .expectBody(JobDTO::class.java)
            .isEqualTo(updatedJob)

        coVerify(exactly = 1) { jobService.updateJob(jobToUpdate) }
    }

    @Test
    fun `should not update invalid job`() {
        val jobId = "1"
        val userId = 1L
        val invalidJob = JobDTO(
            id = jobId,
            userId = userId,
            title = "",
            description = "testdescription",
            startDate = startDate,
            endDate = endDate,
            currentJournalTemplateId = null
        )

        webTestClient.put()                                             // When
            .uri("/jobs")
            .bodyValue(invalidJob)
            .exchange()
            .expectStatus().isBadRequest                                // Then

        coVerify { jobService wasNot Called }
    }

    @Test
    fun `should not update non-existing job`() {
        val jobId = "non-existing-id"
        val userId = 1L
        val job = JobDTO(
            id = jobId,
            userId = userId,
            title = "testName",
            description = "testdescription",
            startDate = startDate,
            endDate = endDate,
            currentJournalTemplateId = null
        )

        coEvery { jobService.updateJob(job) } throws JobNotFoundException(jobId)

        webTestClient.put()                                                      // When
            .uri("/jobs")
            .bodyValue(job)
            .exchange()
            .expectStatus().isNotFound                                          // Then

        coVerify(exactly = 1) { jobService.updateJob(job) }
    }

    @Test
    fun `should retrieve all jobs successfully`() {
        val userId = 1L
        val jobs = arrayOf(
            JobDTO(
                id = "1",
                userId = userId,
                title = "testname1",
                description = "testdescription1",
                startDate = startDate,
                endDate = endDate,
                currentJournalTemplateId = null
            ),
            JobDTO(
                id = "2",
                userId = userId,
                title = "testname2",
                description = "testdescription2",
                startDate = startDate,
                endDate = endDate,
                currentJournalTemplateId = null
            )
        )

        coEvery { jobService.getAllJobsForUser(userId) } returns flowOf(*jobs)

        webTestClient.get()
            .uri("/jobs/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(JobDTO::class.java)
            .contains(*jobs)

        coVerify(exactly = 1) { jobService.getAllJobsForUser(userId) }
    }

    @Test
    fun `should delete job`() {
        val jobId = "1"
        
        webTestClient.delete()                                      // When
            .uri("/jobs/$jobId")
            .exchange()
            .expectStatus().isNoContent                             // Then

        coVerify(exactly = 1) { jobService.deleteJob(jobId) }
    }

    @Test
    fun `should not delete non-existing job`() {
        val jobId = "non-existing-id"                                                                     // Given

        coEvery { jobService.deleteJob(jobId) } throws JobNotFoundException(jobId)

        webTestClient.delete()                                                                             // When
            .uri("/jobs/$jobId")
            .exchange()
            .expectStatus().isNotFound                                                                     // Then
            .expectBody(String::class.java)
            .isEqualTo("Job with id: $jobId not found")

        coVerify(exactly = 1) { jobService.deleteJob(jobId) }
    }

    @Test
    fun `should provide correct message on unexpected error`() {
        // Given
        val jobId = "1"

        coEvery { jobService.deleteJob(jobId) } throws Exception("Potentially sensitive system information")

        webTestClient.delete()                                                                                   // When
            .uri("/jobs/$jobId")
            .exchange()
            .expectStatus().is5xxServerError                                                                     // Then
            .expectBody(String::class.java)
            .isEqualTo("An unexpected internal server error occurred. Please contact the system administrator.")

        coVerify(exactly = 1) { jobService.deleteJob(jobId) }
    }
}
