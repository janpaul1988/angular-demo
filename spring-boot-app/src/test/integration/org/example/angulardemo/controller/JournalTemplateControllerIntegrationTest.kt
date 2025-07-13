package org.example.angulardemo.controller

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.example.angulardemo.dto.JournalTemplateDTO
import org.example.angulardemo.entity.JournalTemplate
import org.example.angulardemo.entity.User
import org.example.angulardemo.mapper.JournalTemplateMapper
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
import kotlin.test.BeforeTest
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class JournalTemplateControllerIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val templateRepository: JournalTemplateCrudRepository,
    @Autowired private val userCrudRepository: UserCrudRepository,
    @Autowired val templateMapper: JournalTemplateMapper,
    @Autowired private val databaseCleanupUtil: DatabaseCleanupUtil,
) {
    private var userId: Long = 0L
    private val templateContent = "{\"questions\":[{\"id\":\"q1\",\"text\":\"What did you accomplish this week?\"}]}"

    @BeforeTest
    fun setup() = runBlocking {
        // Clear existing data in the correct order to respect referential integrity
        databaseCleanupUtil.cleanDatabase()

        // Create test user
        userId = userCrudRepository.save(User(null, "template-test@example.com")).id!!
    }

    @Test
    fun `should get journal template by id`() = runTest {
        // Given: Save a template to the database
        val template = JournalTemplate(
            id = null,
            userId = userId,
            name = "Weekly Report Template",
            version = 1,
            content = templateContent
        )

        val savedTemplate = templateRepository.save(template)
        val templateId = savedTemplate.id!!
        val savedTemplateDTO = templateMapper.toDto(savedTemplate)

        // When/Then: Verify GET request returns the expected template
        webTestClient.get()
            .uri("/journaltemplates/$templateId")
            .exchange()
            .expectStatus().isOk
            .expectBody(JournalTemplateDTO::class.java)
            .isEqualTo(savedTemplateDTO)
    }

    @Test
    fun `should return not found for non-existent template`() = runTest {
        // Given: A non-existent template ID
        val nonExistentId = "non-existent-id"

        // When/Then: Verify GET request returns not found
        webTestClient.get()
            .uri("/journaltemplates/$nonExistentId")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should get journal templates by user id`() = runTest {
        // Given: Save multiple templates to the database
        val templates = listOf(
            JournalTemplate(
                id = null,
                userId = userId,
                name = "Weekly Report Template",
                version = 1,
                content = templateContent
            ),
            JournalTemplate(
                id = null,
                userId = userId,
                name = "Monthly Review Template",
                version = 1,
                content = "{\"questions\":[{\"id\":\"q1\",\"text\":\"What were your major achievements this month?\"}]}"
            )
        )

        val savedTemplates = templateRepository.saveAll(flowOf(*templates.toTypedArray())).toList().map {
            templateMapper.toDto(it)
        }

        // When/Then: Verify GET request returns the expected templates
        webTestClient.get()
            .uri("/journaltemplates/user/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<JournalTemplateDTO>()
            .hasSize(2)
            .contains(savedTemplates[0], savedTemplates[1])
    }

    @Test
    fun `should save new journal template successfully`() = runTest {
        // Given: Prepare template data
        val newTemplate = JournalTemplateDTO(
            id = null,
            userId = userId,
            name = "New Test Template ${System.currentTimeMillis()}", // Ensure unique name
            version = 1,
            content = "{\"questions\":[{\"id\":\"q1\",\"text\":\"New test question\"}]}"
        )

        // When/Then: Verify POST request creates and returns the template
        webTestClient.post()
            .uri("/journaltemplates")
            .bodyValue(newTemplate)
            .exchange()
            .expectStatus().isCreated
            .expectBody(JournalTemplateDTO::class.java)
            .consumeWith { response ->
                val savedTemplate = response.responseBody!!
                assertThat(savedTemplate.id).isNotNull()
                assertThat(savedTemplate.userId).isEqualTo(newTemplate.userId)
                assertThat(savedTemplate.name).isEqualTo(newTemplate.name)
                assertThat(savedTemplate.version).isEqualTo(1) // First version
                assertThat(savedTemplate.content).isEqualTo(newTemplate.content)

                // Verify in database
                runBlocking {
                    val dbTemplate = templateRepository.findById(savedTemplate.id!!)
                    assertThat(dbTemplate).isNotNull
                    assertThat(dbTemplate?.content).isEqualTo(newTemplate.content)
                }
            }
    }

    @Test
    fun `should update existing template with new version`() = runTest {
        // Generate a unique template name with timestamp to avoid conflicts with other tests
        val uniqueTemplateName = "Template To Update ${System.currentTimeMillis()}"

        // Given: Create a template - using runBlocking to ensure this completes before proceeding
        val savedTemplate = runBlocking {
            val template = JournalTemplate(
                id = null,
                userId = userId,
                name = uniqueTemplateName,
                version = 1,
                content = templateContent
            )
            templateRepository.save(template)
        }

        val originalTemplateId = savedTemplate.id!!

        // Ensure the template was saved correctly and transaction is complete
        runBlocking {
            val savedTemplateFromDb = templateRepository.findById(originalTemplateId)
            assertThat(savedTemplateFromDb).isNotNull
            assertThat(savedTemplateFromDb?.name).isEqualTo(uniqueTemplateName)
            assertThat(savedTemplateFromDb?.version).isEqualTo(1)

            // Explicitly find the max version to confirm it's working
            val maxVersion = templateRepository.findMaxVersionByUserIdAndName(userId, uniqueTemplateName)
            assertThat(maxVersion).isEqualTo(1)

            // Wait a bit to ensure the transaction is completed
            kotlinx.coroutines.delay(200)
        }

        // Create an updated version
        val updatedTemplate = JournalTemplateDTO(
            id = null, //
            userId = savedTemplate.userId,
            name = savedTemplate.name, // Same name
            version = savedTemplate.version, // We now send a journal with a version, the backend should recognize this and increment it.
            content = "{\"questions\":[{\"id\":\"q1\",\"text\":\"Updated question\"}]}"
        )

        // When/Then: Verify POST creates a new version
        webTestClient.post()
            .uri("/journaltemplates")
            .bodyValue(updatedTemplate)
            .exchange()
            .expectStatus().isCreated
            .expectBody(JournalTemplateDTO::class.java)
            .consumeWith { response ->
                val savedUpdatedTemplate = response.responseBody!!
                assertThat(savedUpdatedTemplate.id).isNotNull()
                assertThat(savedUpdatedTemplate.id).isNotEqualTo(originalTemplateId) // Should be a new entity
                assertThat(savedUpdatedTemplate.userId).isEqualTo(updatedTemplate.userId)
                assertThat(savedUpdatedTemplate.name).isEqualTo(updatedTemplate.name)
                assertThat(savedUpdatedTemplate.version).isEqualTo(2) // Should be incremented
                assertThat(savedUpdatedTemplate.content).isEqualTo(updatedTemplate.content)

                // Wait to ensure the transaction is complete
                runBlocking { kotlinx.coroutines.delay(200) }

                // Verify we have two versions of the template in a separate coroutine
                runBlocking {
                    val templates = templateRepository.findAllByUserId(userId).toList()
                        .filter { it.name == uniqueTemplateName }

                    assertThat(templates).hasSize(2)
                    assertThat(templates.map { it.version }).containsExactlyInAnyOrder(1, 2)
                }
            }
    }

    @Test
    fun `should delete journal template successfully`() = runTest {
        // Given: Create a template to delete
        val template = JournalTemplate(
            id = null,
            userId = userId,
            name = "Template To Delete",
            version = 1,
            content = templateContent
        )

        val savedTemplate = templateRepository.save(template)
        val templateId = savedTemplate.id!!

        // When: Delete the template
        webTestClient.delete()
            .uri("/journaltemplates/$templateId")
            .exchange()
            .expectStatus().isNoContent

        // Then: Verify template no longer exists
        val deletedTemplate = runBlocking { templateRepository.findById(templateId) }
        assertNull(deletedTemplate)
    }
}
