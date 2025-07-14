package org.example.angulardemo.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.example.angulardemo.dto.JournalDTO
import org.example.angulardemo.entity.Job
import org.example.angulardemo.entity.Journal
import org.example.angulardemo.entity.JournalTemplate
import org.example.angulardemo.entity.User
import org.example.angulardemo.mapper.JournalMapper
import org.example.angulardemo.repository.JobCrudRepository
import org.example.angulardemo.repository.JournalCrudRepository
import org.example.angulardemo.repository.JournalTemplateCrudRepository
import org.example.angulardemo.repository.UserCrudRepository
import org.example.angulardemo.util.DatabaseCleanupUtil
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.assertNull

//
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class JournalControllerIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val journalRepository: JournalCrudRepository,
    @Autowired private val jobRepository: JobCrudRepository,
    @Autowired private val templateRepository: JournalTemplateCrudRepository,
    @Autowired private val userCrudRepository: UserCrudRepository,
    @Autowired val journalMapper: JournalMapper,
    @Autowired private val databaseCleanupUtil: DatabaseCleanupUtil,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private lateinit var userId: String
    private lateinit var jobId: String
    private lateinit var templateId: String
    private val year = 2025
    private val week = 25
    private val journalContent = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Test answer\"}]}"
    private val templateContent = "{\"questions\":[{\"id\":\"q1\",\"text\":\"What did you accomplish this week?\"}]}"

    private fun createJsonNode(content: String): JsonNode {
        return objectMapper.readTree(content)
    }

    @BeforeTest
    fun setup() = runBlocking {
        // Clear existing data in the correct order to respect referential integrity
        databaseCleanupUtil.cleanDatabase()

        // Create test user
        val user = userCrudRepository.save(User(null, "test@example.com"))
        userId = user.id.toString()

        // Create test template
        val template = templateRepository.save(
            JournalTemplate(
                id = null,
                userId = user.id!!,
                name = "Test Template",
                version = 1,
                content = templateContent
            )
        )
        templateId = template.id!!

        // Create test job
        val job = jobRepository.save(
            Job(
                id = null,
                userId = user.id!!,
                title = "Test Job",
                description = "Test Job Description",
                startDate = LocalDate.now(),
                endDate = null,
                currentJournalTemplateId = templateId
            )
        )
        jobId = job.id!!
    }

    @Test
    fun `should get journal by year, week and job id`() = runTest {
        // Given: Save a journal to the database
        val journal = Journal(
            id = null,
            jobId = jobId,
            templateId = templateId,
            year = year,
            week = week,
            content = journalContent
        )

        val savedJournal = journalRepository.save(journal)
        val savedJournalDTO = journalMapper.toDto(savedJournal)

        // When/Then: Verify GET request returns the expected journal
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/journals")
                    .queryParam("year", year)
                    .queryParam("week", week)
                    .queryParam("jobId", jobId)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(JournalDTO::class.java)
            .isEqualTo(savedJournalDTO)
    }

    @Test
    fun `should return not found for non-existent journal`() = runTest {
        // Given: No journal exists for these parameters
        val nonExistentYear = 2026
        val nonExistentWeek = 1

        // When/Then: Verify GET request returns not found
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/journals")
                    .queryParam("year", nonExistentYear)
                    .queryParam("week", nonExistentWeek)
                    .queryParam("jobId", jobId)
                    .build()
            }
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should get journals by job id`() = runTest {
        // Given: Save multiple journals to the database
        val journals = listOf(
            Journal(
                id = null,
                jobId = jobId,
                templateId = templateId,
                year = year,
                week = week,
                content = journalContent
            ),
            Journal(
                id = null,
                jobId = jobId,
                templateId = templateId,
                year = year,
                week = week + 1,
                content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Week ${week + 1} answer\"}]}"
            )
        )

        val savedJournals = journalRepository.saveAll(flowOf(*journals.toTypedArray())).toList().map {
            journalMapper.toDto(it)
        }

        // When/Then: Verify GET request returns the expected journals
        webTestClient.get()
            .uri("/journals/job/$jobId")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<JournalDTO>()
            .hasSize(2)
            .contains(savedJournals[0], savedJournals[1])
    }

    @Test
    fun `should save journal successfully`() = runTest {
        // Given: Prepare journal data with explicit null ID to ensure a new entry is created
        val newJournal = JournalDTO(
            id = null,
            jobId = jobId,
            templateId = templateId,
            year = year,
            week = week + 2,
            content = objectMapper.readTree("{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"New journal entry\"}]}")
        )

        // When/Then: Verify POST request creates and returns the journal
        webTestClient.post()
            .uri("/journals")
            .bodyValue(newJournal)
            .exchange()
            .expectStatus().isCreated
            .expectBody(JournalDTO::class.java)
            .consumeWith { response ->
                val savedJournal = response.responseBody!!
                assertThat(savedJournal.id).isNotNull()
                assertThat(savedJournal.jobId).isEqualTo(newJournal.jobId)
                assertThat(savedJournal.templateId).isEqualTo(newJournal.templateId)
                assertThat(savedJournal.year).isEqualTo(newJournal.year)
                assertThat(savedJournal.week).isEqualTo(newJournal.week)
                assertThat(savedJournal.content).isEqualTo(newJournal.content)

                // Verify it exists in the database - using a separate coroutine context
                runBlocking {
                    val dbJournal = journalRepository.findById(savedJournal.id!!)
                    assertThat(dbJournal).isNotNull
                    assertThat(dbJournal?.content).isEqualTo(objectMapper.writeValueAsString(newJournal.content))
                }
            }
    }

    @Test
    fun `should update journal with new templateId`() = runTest {
        // First, create another template to switch to
        val newTemplate = templateRepository.save(
            JournalTemplate(
                id = null,
                userId = userCrudRepository.findById(userId.toLong())!!.id!!,
                name = "Another Template",
                version = 1,
                content = "{\"questions\":[{\"id\":\"q2\",\"text\":\"What are your goals for next week?\"}]}"
            )
        )
        val newTemplateId = newTemplate.id!!
        
        // Given: Create a journal to update
        val journal = Journal(
            id = null,
            jobId = jobId,
            templateId = templateId, // Original template ID
            year = year,
            week = week + 10, // Use a different week
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Original answer\"}]}"
        )

        val savedJournal = journalRepository.save(journal)
        val journalId = savedJournal.id!!

        // Verify journal was created successfully
        val initialJournal = runBlocking { journalRepository.findById(journalId) }
        assertThat(initialJournal).isNotNull
        assertThat(initialJournal?.content).contains("Original answer")
        assertThat(initialJournal?.templateId).isEqualTo(templateId)

        // Prepare update data with new template ID
        val updatedContent =
            objectMapper.readTree("{\"answers\":[{\"questionId\":\"q2\",\"answer\":\"Updated answer for new template\"}]}");
        val updatedJournal = JournalDTO(
            id = journalId,
            jobId = jobId,
            templateId = newTemplateId, // New template ID
            year = year,
            week = week + 10,
            content = updatedContent
        )

        // When/Then: Verify PUT request updates and returns the journal
        webTestClient.put()
            .uri("/journals")
            .bodyValue(updatedJournal)
            .exchange()
            .expectStatus().isOk
            .expectBody(JournalDTO::class.java)
            .consumeWith { response ->
                val result = response.responseBody!!
                assertThat(result.id).isEqualTo(updatedJournal.id)
                assertThat(result.templateId).isEqualTo(newTemplateId) // Verify template ID is updated
                assertThat(result.content).isEqualTo(updatedContent)
            }

        // Verify the database was updated
        val dbJournal = runBlocking { journalRepository.findById(journalId) }
        assertThat(dbJournal).isNotNull
        assertThat(dbJournal?.templateId).isEqualTo(newTemplateId) // Verify template ID is updated in DB
        assertThat(dbJournal?.content).isEqualTo(objectMapper.writeValueAsString(updatedContent))
    }

    @Test
    fun `should delete journal successfully`() = runTest {
        // Given: Create a journal to delete
        val journal = Journal(
            id = null,
            jobId = jobId,
            templateId = templateId,
            year = year,
            week = week + 4,
            content = journalContent
        )

        val savedJournal = journalRepository.save(journal)
        val journalId = savedJournal.id!!

        // When: Delete the journal
        webTestClient.delete()
            .uri("/journals/$journalId")
            .exchange()
            .expectStatus().isNoContent

        // Then: Verify journal no longer exists
        val deletedJournal = runBlocking { journalRepository.findById(journalId) }
        assertNull(deletedJournal)
    }
}
