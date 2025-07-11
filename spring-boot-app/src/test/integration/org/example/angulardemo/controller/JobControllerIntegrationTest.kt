package org.example.angulardemo.controller

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.example.angulardemo.dto.JobDTO
import org.example.angulardemo.entity.Job
import org.example.angulardemo.entity.User
import org.example.angulardemo.repository.JobCrudRepository
import org.example.angulardemo.repository.UserCrudRepository
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.util.Collections.synchronizedSet
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class JobControllerIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val jobRepository: JobCrudRepository,
    @Autowired private val userCrudRepository: UserCrudRepository,
) {
    var userId: Long = 0L;

    @BeforeTest
    fun setup() = runBlocking {
        // Given an empty jobs table in the database.
        jobRepository.deleteAll()
        userCrudRepository.deleteAll()
        userId = userCrudRepository.save(User(null, "testuser")).id!!
    }

    @Test
    fun `should retrieve all jobs successfully for a user`() = runTest {
        // Given: Save the jobs reactively and prepare the expected results.
        flowOf(
            Job(null, userId, 1, "testname1", "testdescription1"),
            Job(null, userId, 2, "testname2", "testdescription2")
        ).let {
            jobRepository.saveAll(it)
        }.map {
            JobDTO(it.externalId, it.userId, it.title, it.description)
        }.toList()
            .toTypedArray()
            .also {
                // When: Send a GET request to retrieve all jobs.
                webTestClient.get()
                    .uri("/jobs/${userId}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<JobDTO>()
                    // Then: Verify the response matches the saved jobs.
                    .hasSize(2)
                    .contains(*it)
            }
    }

    @Test
    fun `should save job successfully`() = runTest {
        // Given: Prepare the job to save.
        JobDTO(null, userId, "testname1", "testdescription1")
            .also {
                // When: Send a POST request to save the job.
                webTestClient.post()
                    .uri("/jobs/${userId}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody(JobDTO::class.java)
                    .consumeWith { responseEntity ->
                        // Then: Verify the response contains the saved job with a generated ID.
                        val response = responseEntity.responseBody
                        Assertions.assertEquals(response, it.copy(id = response!!.id))
                    }
            }
    }

    @Test
    fun `concurrent job creation should assign unique externalIds`() = runTest {
        val jobNumbers = synchronizedSet(mutableSetOf<Long>())
        val threadPoolSize = 1000;
        List(threadPoolSize) {
            async {
                JobDTO(null, userId, "Test", "Test").let {
                    webTestClient.post()
                        .uri("/jobs/${userId}")
                        .bodyValue(it)
                        .exchange()
                        .expectStatus().isCreated
                        .expectBody(JobDTO::class.java)
                        .consumeWith { responseEntity ->
                            // Then: Verify the response contains the saved job with a generated ID.
                            val response = responseEntity.responseBody
                            Assertions.assertEquals(response, it.copy(id = response!!.id))
                            jobNumbers.add(response.id)
                        }
                }
            }
        }.awaitAll()

        assertEquals(threadPoolSize, jobNumbers.size) // all unique
    }

    @Test
    fun `should update job successfully`() = runTest {
        // Given: Save the job to the database and prepare the job for update.
        Job(null, userId, 1L, "testname", "testdescription")
            .let {
                jobRepository.save(it)
            }
            .let {
                assertThat(it.id).isNotNull() // Check that the job is saved.
                JobDTO(
                    it.externalId,
                    userId,
                    "updatedTestName",
                    "updatedTestDescription"
                )// Prepare the job for update.
            }.also {
                // When: Send a PUT request to update the job.
                webTestClient.put()
                    .uri("/jobs/${it.userId}/${it.id}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(JobDTO::class.java)
                    // Then: Verify the updated job matches the input.
                    .isEqualTo(it)
            }
    }

    @Test
    fun `should delete job successfully`() = runTest {
        // Given: Save the job to the database.
        Job(null, userId, 1L, "updatedTestName", "updatedTestDescription")
            .let {
                jobRepository.save(it)
            }.also {// Verify the job was saved and has an ID.
                assertThat(it.id).isNotNull()
            }.also {
                // When: Send a DELETE request to remove the job.
                webTestClient.delete()
                    .uri("/jobs/${it.userId}/${it.externalId}")
                    .exchange()
                    .expectStatus().isNoContent
            }.also {
                // Then: Verify the job is no longer in the database.
                Assertions.assertNull(jobRepository.findById(it.id!!))
            }
    }
}


