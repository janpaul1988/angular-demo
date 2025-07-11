package org.example.angulardemo.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import org.example.angulardemo.dto.JournalDTO
import org.example.angulardemo.exception.JournalNotFoundException
import org.example.angulardemo.service.JournalService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [JournalController::class])
@AutoConfigureWebTestClient
class JournalControllerUnitTest(
    @Autowired
    val webTestClient: WebTestClient,
) {
    @MockkBean(relaxed = true)
    lateinit var journalService: JournalService

    @Test
    fun `should get journal by year, week and job id`() {
        // Given
        val year = 2025
        val week = 25
        val jobId = "job-123"
        val journal = JournalDTO(
            id = "journal-1",
            jobId = jobId,
            templateId = "template-1",
            year = year,
            week = week,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Test answer\"}]}"
        )

        coEvery { journalService.getJournalByYearWeekAndJobId(year, week, jobId) } returns journal

        // When
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
            .isEqualTo(journal)

        // Then
        coVerify(exactly = 1) { journalService.getJournalByYearWeekAndJobId(year, week, jobId) }
    }

    @Test
    fun `should return not found when journal does not exist`() {
        // Given
        val year = 2025
        val week = 25
        val jobId = "job-123"

        coEvery { journalService.getJournalByYearWeekAndJobId(year, week, jobId) } throws
                JournalNotFoundException(year, week, jobId)

        // When
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/journals")
                    .queryParam("year", year)
                    .queryParam("week", week)
                    .queryParam("jobId", jobId)
                    .build()
            }
            .exchange()
            .expectStatus().isNotFound
            .expectBody(String::class.java)
            .isEqualTo("No journal found for year $year and week $week and jobId $jobId.")

        // Then
        coVerify(exactly = 1) { journalService.getJournalByYearWeekAndJobId(year, week, jobId) }
    }

    @Test
    fun `should get journals by job id`() {
        // Given
        val jobId = "job-123"
        val journals = arrayOf(
            JournalDTO(
                id = "journal-1",
                jobId = jobId,
                templateId = "template-1",
                year = 2025,
                week = 25,
                content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Week 25 answer\"}]}"
            ),
            JournalDTO(
                id = "journal-2",
                jobId = jobId,
                templateId = "template-1",
                year = 2025,
                week = 26,
                content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Week 26 answer\"}]}"
            )
        )

        coEvery { journalService.getJournalsByJobId(jobId) } returns flowOf(*journals)

        // When
        webTestClient.get()
            .uri("/journals/job/$jobId")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(JournalDTO::class.java)
            .contains(*journals)

        // Then
        coVerify(exactly = 1) { journalService.getJournalsByJobId(jobId) }
    }

    @Test
    fun `should save journal`() {
        // Given
        val journalToSave = JournalDTO(
            id = null,
            jobId = "job-123",
            templateId = "template-1",
            year = 2025,
            week = 27,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"New answer\"}]}"
        )
        val savedJournal = journalToSave.copy(id = "journal-3")

        coEvery { journalService.saveJournal(journalToSave) } returns savedJournal

        // When
        webTestClient.post()
            .uri("/journals")
            .bodyValue(journalToSave)
            .exchange()
            .expectStatus().isCreated
            .expectBody(JournalDTO::class.java)
            .isEqualTo(savedJournal)

        // Then
        coVerify(exactly = 1) { journalService.saveJournal(journalToSave) }
    }

    @Test
    fun `should not save journal with invalid data`() {
        // Given
        val invalidJournal = JournalDTO(
            id = null,
            jobId = "",  // Invalid: blank jobId
            templateId = "template-1",
            year = 2025,
            week = 27,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"New answer\"}]}"
        )

        // When
        webTestClient.post()
            .uri("/journals")
            .bodyValue(invalidJournal)
            .exchange()
            .expectStatus().isBadRequest

        // Then
        coVerify { journalService wasNot Called }
    }

    @Test
    fun `should update journal`() {
        // Given
        val journalToUpdate = JournalDTO(
            id = "journal-1",
            jobId = "job-123",
            templateId = "template-1",
            year = 2025,
            week = 25,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Updated answer\"}]}"
        )
        val updatedJournal = journalToUpdate.copy()

        coEvery { journalService.updateJournal(journalToUpdate) } returns updatedJournal

        // When
        webTestClient.put()
            .uri("/journals")
            .bodyValue(journalToUpdate)
            .exchange()
            .expectStatus().isOk
            .expectBody(JournalDTO::class.java)
            .isEqualTo(updatedJournal)

        // Then
        coVerify(exactly = 1) { journalService.updateJournal(journalToUpdate) }
    }

    @Test
    fun `should not update journal that does not exist`() {
        // Given
        val nonExistingJournal = JournalDTO(
            id = "non-existing-id",
            jobId = "job-123",
            templateId = "template-1",
            year = 2025,
            week = 25,
            content = "{\"answers\":[{\"questionId\":\"q1\",\"answer\":\"Some answer\"}]}"
        )

        coEvery { journalService.updateJournal(nonExistingJournal) } throws JournalNotFoundException(nonExistingJournal.id!!)

        // When
        webTestClient.put()
            .uri("/journals")
            .bodyValue(nonExistingJournal)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(String::class.java)
            .isEqualTo("Journal with id: ${nonExistingJournal.id} not found")

        // Then
        coVerify(exactly = 1) { journalService.updateJournal(nonExistingJournal) }
    }

    @Test
    fun `should delete journal`() {
        // Given
        val journalId = "journal-1"

        coEvery { journalService.deleteJournal(journalId) } returns Unit

        // When
        webTestClient.delete()
            .uri("/journals/$journalId")
            .exchange()
            .expectStatus().isNoContent

        // Then
        coVerify(exactly = 1) { journalService.deleteJournal(journalId) }
    }

    @Test
    fun `should not delete journal that does not exist`() {
        // Given
        val nonExistingId = "non-existing-id"

        coEvery { journalService.deleteJournal(nonExistingId) } throws JournalNotFoundException(nonExistingId)

        // When
        webTestClient.delete()
            .uri("/journals/$nonExistingId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(String::class.java)
            .isEqualTo("Journal with id: $nonExistingId not found")

        // Then
        coVerify(exactly = 1) { journalService.deleteJournal(nonExistingId) }
    }
}
