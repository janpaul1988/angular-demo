package org.example.angulardemo.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import org.example.angulardemo.dto.JobDTO
import org.example.angulardemo.exception.JobNotFoundException
import org.example.angulardemo.service.JobService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [JobController::class])
@AutoConfigureWebTestClient
class jobControllerUnitTest(
    @Autowired
    val webTestClient: WebTestClient,
) {
    @MockkBean(relaxed = true)
    lateinit var jobService: JobService


    @Test
    fun `should add job for user`() {
        val userId = 1L
        val jobToAdd = JobDTO(null, userId, "testname", "testdescription")
        val jobAdded = jobToAdd.copy(id = 1L)
        // Given

        coEvery { jobService.addJob(userId, jobToAdd) } returns jobAdded

        webTestClient.post()                                                           // When
            .uri("/jobs/$userId")
            .bodyValue(jobToAdd)
            .exchange()
            .expectStatus().isCreated                                                  // Then
            .expectBody(JobDTO::class.java)
            .isEqualTo(jobAdded)

        coVerify(exactly = 1) { jobService.addJob(userId, jobToAdd) }

    }

    @Test
    fun `should not add job for user without name`() {
        JobDTO(null, 1L, title = "", description = "testdescription")                         // Given
            .also {
                webTestClient.post()                                                   // When
                    .uri("/jobs/${it.userId}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isBadRequest                                       // Then
                    .expectBody(String::class.java)
                    .isEqualTo("job name cannot be blank.")
            }.also {
                coVerify { jobService wasNot Called }
            }
    }

    @Test
    fun `should update job`() {

        JobDTO(1L, 1L, title = "testName", description = "testdescription")                          // Given
            .let {
                // Pair the request to its expected result.
                it to it.copy(id = it.id, userId = it.userId, title = "updatedName", description = "updatedDescription")
            }.also {
                coEvery { jobService.updateJob(it.first.userId, it.first.id!!, it.first) } returns it.second
            }.also {                                                      // When
                webTestClient.put()
                    .uri("/jobs/${it.first.userId}/${it.first.id}")
                    .bodyValue(it.first)
                    .exchange()
                    .expectStatus().isOk                                                              // Then
                    .expectBody(JobDTO::class.java)
                    .isEqualTo(it.second)
            }.also {// Then
                coVerify(exactly = 1) { jobService.updateJob(it.first.userId, it.first.id!!, it.first) }
            }
    }

    @Test
    fun `should not update invalid job`() {

        JobDTO(1L, 1L, title = "", description = "testdescription")          // Given
            .also {
                webTestClient.put()                                             // When
                    .uri("/jobs/${it.userId}/${it.id}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isBadRequest                                // Then
                    .expectBody(String::class.java)
                    .isEqualTo("job name cannot be blank.")
            }.also {
                coVerify { jobService wasNot Called }
            }
    }

    @Test
    fun `should not update non-existing job`() {

        JobDTO(1L, 1L, title = "testName", description = "testdescription")            // Given
            .also {
                coEvery {
                    jobService.updateJob(
                        it.userId,
                        it.id!!,
                        it
                    )
                } throws JobNotFoundException(it.userId, it.id!!)
            }
            .also {
                webTestClient.put()                                                      // When
                    .uri("/jobs/${it.userId}/${it.id}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isNotFound                                          // Then
                    .expectBody(String::class.java)
                    .isEqualTo("job with id: ${it.id} not found for user with id: ${it.userId}")
            }
            .also {
                coVerify(exactly = 1) { jobService.updateJob(it.userId, it.id!!, it) }
            }
    }

    @Test
    fun `should retrieve all jobs successfully`() {
        val userId = 1L
        // Given
        arrayOf(
            JobDTO(1, userId, "testname1", "testdescription1"),
            JobDTO(2, userId, "testname2", "testdescription2")
        )
            .also {
                coEvery { jobService.getAllJobsForUser(userId) } returns flowOf(*it)
            }
            .also {
                webTestClient.get()
                    .uri("/jobs/${userId}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(JobDTO::class.java)
                    .contains(*it)
            }
            .also {
                coVerify(exactly = 1) { jobService.getAllJobsForUser(userId) }
            }
    }

    @Test
    fun `should delete job`() {
        val id = 1L                                                 // Given
        val userId = 1L

        webTestClient.delete()                                      // When
            .uri("/jobs/$userId/$id")
            .exchange()
            .expectStatus().isNoContent                             // Then

        coVerify(exactly = 1) { jobService.deleteJob(userId, id) }
    }

    @Test
    fun `should not delete non-existing job`() {
        val id = 1L                                                                                        // Given
        val userId = 1L

        coEvery { jobService.deleteJob(userId, id) } throws JobNotFoundException(userId, id)

        webTestClient.delete()                                                                             // When
            .uri("/jobs/$userId/$id")
            .exchange()
            .expectStatus().isNotFound                                                                     // Then
            .expectBody(String::class.java)
            .isEqualTo("job with id: $id not found for user with id: $userId")

        coVerify(exactly = 1) { jobService.deleteJob(userId, id) }

    }

    @Test
    fun `should provide correct message on unexpected error`() {
        // Given
        val id = 1L
        val userId = 1L

        coEvery {
            jobService.deleteJob(
                userId,
                id
            )
        } throws Exception("Potentially sensitive system information")

        webTestClient.delete()                                                                                   // When
            .uri("/jobs/$userId/$id")
            .exchange()
            .expectStatus().is5xxServerError                                                                     // Then
            .expectBody(String::class.java)
            .isEqualTo("An unexpected internal server error occurred. Please contact the system administrator.")

        coVerify(exactly = 1) { jobService.deleteJob(userId, id) }
    }
}
