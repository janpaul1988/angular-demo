package org.example.jobjournaler.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import org.example.jobjournaler.dto.JournalTemplateDTO
import org.example.jobjournaler.exception.JournalTemplateNotFoundException
import org.example.jobjournaler.service.JournalTemplateService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [JournalTemplateController::class])
@AutoConfigureWebTestClient
class JournalTemplateControllerUnitTest(
    @Autowired
    val webTestClient: WebTestClient,
    @Autowired
    val objectMapper: ObjectMapper,
) {
    @MockkBean(relaxed = true)
    lateinit var journalTemplateService: JournalTemplateService

    private fun createJsonNode(content: String): JsonNode {
        return objectMapper.readTree(content)
    }

    @Test
    fun `should get journal template by id`() {
        // Given
        val templateId = "template-123"
        val template = JournalTemplateDTO(
            id = templateId,
            userId = 1L,
            name = "Weekly Report Template",
            version = 1,
            content = createJsonNode("{\"questions\":[{\"id\":\"q1\",\"text\":\"What did you accomplish this week?\"}]}")
        )

        coEvery { journalTemplateService.findJournalTemplateDtoById(templateId) } returns template

        // When
        webTestClient.get()
            .uri("/journaltemplates/$templateId")
            .exchange()
            .expectStatus().isOk
            .expectBody(JournalTemplateDTO::class.java)
            .isEqualTo(template)

        // Then
        coVerify(exactly = 1) { journalTemplateService.findJournalTemplateDtoById(templateId) }
    }

    @Test
    fun `should return not found when journal template does not exist`() {
        // Given
        val nonExistingId = "non-existing-id"

        coEvery { journalTemplateService.findJournalTemplateDtoById(nonExistingId) } throws
                JournalTemplateNotFoundException(nonExistingId)

        // When
        webTestClient.get()
            .uri("/journaltemplates/$nonExistingId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(String::class.java)
            .isEqualTo("Journal template with id: $nonExistingId not found")

        // Then
        coVerify(exactly = 1) { journalTemplateService.findJournalTemplateDtoById(nonExistingId) }
    }

    @Test
    fun `should get journal templates by user id`() {
        // Given
        val userId = 1L
        val templates = arrayOf(
            JournalTemplateDTO(
                id = "template-1",
                userId = userId,
                name = "Weekly Report Template",
                version = 1,
                content = createJsonNode("{\"questions\":[{\"id\":\"q1\",\"text\":\"What did you accomplish this week?\"}]}")
            ),
            JournalTemplateDTO(
                id = "template-2",
                userId = userId,
                name = "Monthly Review Template",
                version = 1,
                content = createJsonNode("{\"questions\":[{\"id\":\"q1\",\"text\":\"What were your major achievements this month?\"}]}")
            )
        )

        coEvery { journalTemplateService.findJournalTemplatesByUserId(userId) } returns flowOf(*templates)

        // When
        webTestClient.get()
            .uri("/journaltemplates/user/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(JournalTemplateDTO::class.java)
            .contains(*templates)

        // Then
        coVerify(exactly = 1) { journalTemplateService.findJournalTemplatesByUserId(userId) }
    }

    @Test
    fun `should save journal template`() {
        // Given
        val templateToSave = JournalTemplateDTO(
            id = null,
            userId = 1L,
            name = "New Template",
            version = 1,
            content = createJsonNode("{\"questions\":[{\"id\":\"q1\",\"text\":\"New question\"}]}")
        )
        val savedTemplate = templateToSave.copy(id = "template-3")

        coEvery { journalTemplateService.createOrUpdateTemplate(any()) } returns savedTemplate

        // When
        webTestClient.post()
            .uri("/journaltemplates")
            .bodyValue(templateToSave)
            .exchange()
            .expectStatus().isCreated
            .expectBody(JournalTemplateDTO::class.java)
            .isEqualTo(savedTemplate)

        // Then
        coVerify(exactly = 1) { journalTemplateService.createOrUpdateTemplate(any()) }
    }

    @Test
    fun `should not save journal template with invalid data`() {
        // Given
        val invalidTemplate = JournalTemplateDTO(
            id = null,
            userId = 1L,
            name = "",  // Invalid: blank name
            version = 1,
            content = createJsonNode("{\"questions\":[{\"id\":\"q1\",\"text\":\"New question\"}]}")
        )

        // When
        webTestClient.post()
            .uri("/journaltemplates")
            .bodyValue(invalidTemplate)
            .exchange()
            .expectStatus().isBadRequest

        // Then
        coVerify(exactly = 0) { journalTemplateService.createOrUpdateTemplate(any()) }
    }

    @Test
    fun `should delete journal template`() {
        // Given
        val templateId = "template-1"

        coEvery { journalTemplateService.deleteJournalTemplate(templateId) } returns Unit

        // When
        webTestClient.delete()
            .uri("/journaltemplates/$templateId")
            .exchange()
            .expectStatus().isNoContent

        // Then
        coVerify(exactly = 1) { journalTemplateService.deleteJournalTemplate(templateId) }
    }

    @Test
    fun `should not delete journal template that does not exist`() {
        // Given
        val nonExistingId = "non-existing-id"

        coEvery { journalTemplateService.deleteJournalTemplate(nonExistingId) } throws
                JournalTemplateNotFoundException(nonExistingId)

        // When
        webTestClient.delete()
            .uri("/journaltemplates/$nonExistingId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(String::class.java)
            .isEqualTo("Journal template with id: $nonExistingId not found")

        // Then
        coVerify(exactly = 1) { journalTemplateService.deleteJournalTemplate(nonExistingId) }
    }
}
