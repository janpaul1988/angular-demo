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
import org.example.jobjournaler.dto.JournalTemplateDTO
import org.example.jobjournaler.entity.JournalTemplate
import org.example.jobjournaler.entity.User
import org.example.jobjournaler.exception.JournalTemplateNotFoundException
import org.example.jobjournaler.mapper.JournalTemplateMapper
import org.example.jobjournaler.repository.JournalTemplateCrudRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.transaction.reactive.TransactionalOperator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class JournalTemplateServiceUnitTest(
    @RelaxedMockK
    private val journalTemplateRepositoryMockk: JournalTemplateCrudRepository,
    @RelaxedMockK
    private val journalTemplateMapperMockk: JournalTemplateMapper,
    @RelaxedMockK
    private val transactionalOperatorMockk: TransactionalOperator,
    @RelaxedMockK
    private val userServiceMockk: UserService,
    @InjectMockKs
    private val journalTemplateService: JournalTemplateService,
) {
    private lateinit var objectMapper: ObjectMapper

    @BeforeTest()
    fun setup() {
        val logger = LoggerFactory.getLogger("org.example.jobjournaler") as Logger
        logger.level = Level.DEBUG

        objectMapper = ObjectMapper()
    }

    @Test
    fun `should find journal template by id`() = runTest {
        // Given
        val templateId = "template-1"
        val template = JournalTemplate(
            id = templateId,
            userId = 1L,
            name = "Template 1",
            version = 1,
            content = "{\"questions\": []}"
        )
        val templateDTO = JournalTemplateDTO(
            id = templateId,
            userId = 1L,
            name = "Template 1",
            version = 1,
            content = objectMapper.readTree("{\"questions\": []}")
        )

        coEvery { journalTemplateRepositoryMockk.findById(templateId) } returns template
        every { journalTemplateMapperMockk.toDto(template) } returns templateDTO

        // When
        val result = journalTemplateService.findJournalTemplateDtoById(templateId)

        // Then
        coVerify(exactly = 1) { journalTemplateRepositoryMockk.findById(templateId) }
        verify(exactly = 1) { journalTemplateMapperMockk.toDto(template) }
        assertEquals(templateDTO, result)
    }

    @Test
    fun `should throw when journal template not found`() = runTest {
        // Given
        val templateId = "non-existent-template"
        coEvery { journalTemplateRepositoryMockk.findById(templateId) } returns null

        // When & Then
        assertFailsWith<JournalTemplateNotFoundException> {
            journalTemplateService.findJournalTemplateDtoById(templateId)
        }

        coVerify(exactly = 1) { journalTemplateRepositoryMockk.findById(templateId) }
        verify(exactly = 0) { journalTemplateMapperMockk.toDto(any()) }
    }

    @Test
    fun `should find all journal templates by user id`() = runTest {
        // Given
        val userId = 1L
        val templates = listOf(
            JournalTemplate(
                id = "template-1",
                userId = userId,
                name = "Template 1",
                version = 1,
                content = "{\"questions\": []}"
            ),
            JournalTemplate(
                id = "template-2",
                userId = userId,
                name = "Template 2",
                version = 1,
                content = "{\"questions\": []}"
            )
        )
        val templatesDTO = listOf(
            JournalTemplateDTO(
                id = "template-1",
                userId = userId,
                name = "Template 1",
                version = 1,
                content = objectMapper.readTree("{\"questions\": []}")
            ),
            JournalTemplateDTO(
                id = "template-2",
                userId = userId,
                name = "Template 2",
                version = 1,
                content = objectMapper.readTree("{\"questions\": []}")
            )
        )

        coEvery { journalTemplateRepositoryMockk.findAllByUserId(userId) } returns flowOf(*templates.toTypedArray())

        templates.forEachIndexed { index, template ->
            every { journalTemplateMapperMockk.toDto(template) } returns templatesDTO[index]
        }

        // When
        val result = journalTemplateService.findJournalTemplatesByUserId(userId).toList()

        // Then
        coVerify(exactly = 1) { journalTemplateRepositoryMockk.findAllByUserId(userId) }
        templates.forEach { template ->
            verify(atLeast = 1) { journalTemplateMapperMockk.toDto(template) }
        }
        assertEquals(templatesDTO, result)
    }

    @Test
    fun `should create new journal template`() = runTest {
        // Given
        val userId = 1L
        val templateName = "New Template"
        val templateDTO = JournalTemplateDTO(
            id = null,
            userId = userId,
            name = templateName,
            version = 1,
            content = objectMapper.readTree("{\"questions\": []}")
        )

        val template = JournalTemplate(
            id = null,
            userId = userId,
            name = templateName,
            version = 1,
            content = "{\"questions\": []}"
        )

        val templateWithIncVersion = template.copy(version = 1) // First version
        val savedTemplate = templateWithIncVersion.copy(id = "template-3")
        val savedTemplateDTO = templateDTO.copy(id = "template-3", version = 1)

        every { journalTemplateMapperMockk.toEntity(templateDTO) } returns template
        coEvery { userServiceMockk.doesUserExist(userId) } returns mockk<User>()
        coEvery { journalTemplateRepositoryMockk.findMaxVersionByUserIdAndName(userId, templateName) } returns null
        coEvery { journalTemplateRepositoryMockk.save(any()) } returns savedTemplate
        every { journalTemplateMapperMockk.toDto(savedTemplate) } returns savedTemplateDTO

        // When
        val result = journalTemplateService.createOrUpdateTemplate(templateDTO)

        // Then
        assertEquals(savedTemplateDTO, result)
        verify(exactly = 1) { journalTemplateMapperMockk.toEntity(templateDTO) }
        coVerify(exactly = 1) { userServiceMockk.doesUserExist(userId) }
        coVerify(exactly = 1) { journalTemplateRepositoryMockk.findMaxVersionByUserIdAndName(userId, templateName) }
        coVerify(exactly = 1) { journalTemplateRepositoryMockk.save(any()) }
        verify(exactly = 1) { journalTemplateMapperMockk.toDto(savedTemplate) }
    }

    @Test
    fun `should update existing journal template with incremented version`() = runTest {
        // Given
        val userId = 1L
        val templateName = "Existing Template"
        val templateDTO = JournalTemplateDTO(
            id = null,
            userId = userId,
            name = templateName,
            version = 1,
            content = objectMapper.readTree("{\"questions\": []}")
        )

        val template = JournalTemplate(
            id = null,
            userId = userId,
            name = templateName,
            version = 1,
            content = "{\"questions\": []}"
        )

        val templateWithIncVersion = template.copy(version = 2) // Incremented version
        val savedTemplate = templateWithIncVersion.copy(id = "template-4")
        val savedTemplateDTO = templateDTO.copy(id = "template-4", version = 2)

        every { journalTemplateMapperMockk.toEntity(templateDTO) } returns template
        coEvery { userServiceMockk.doesUserExist(userId) } returns mockk<User>()
        coEvery { journalTemplateRepositoryMockk.findMaxVersionByUserIdAndName(userId, templateName) } returns 1
        coEvery { journalTemplateRepositoryMockk.save(any()) } returns savedTemplate
        every { journalTemplateMapperMockk.toDto(savedTemplate) } returns savedTemplateDTO

        // When
        val result = journalTemplateService.createOrUpdateTemplate(templateDTO)

        // Then
        assertEquals(savedTemplateDTO, result)
        verify(exactly = 1) { journalTemplateMapperMockk.toEntity(templateDTO) }
        coVerify(exactly = 1) { userServiceMockk.doesUserExist(userId) }
        coVerify(exactly = 1) { journalTemplateRepositoryMockk.findMaxVersionByUserIdAndName(userId, templateName) }
        coVerify(exactly = 1) { journalTemplateRepositoryMockk.save(any()) }
        verify(exactly = 1) { journalTemplateMapperMockk.toDto(savedTemplate) }
    }

    @Test
    fun `should delete journal template`() = runTest {
        // Given
        val templateId = "template-to-delete"
        val template = JournalTemplate(
            id = templateId,
            userId = 1L,
            name = "Template to Delete",
            version = 1,
            content = "{\"questions\": []}"
        )

        coEvery { journalTemplateRepositoryMockk.findById(templateId) } returns template
        coEvery { journalTemplateRepositoryMockk.deleteById(templateId) } returns Unit

        // When
        journalTemplateService.deleteJournalTemplate(templateId)

        // Then
        coVerify(exactly = 1) { journalTemplateRepositoryMockk.findById(templateId) }
        coVerify(exactly = 1) { journalTemplateRepositoryMockk.deleteById(templateId) }
    }

    @Test
    fun `should throw when deleting non-existent journal template`() = runTest {
        // Given
        val templateId = "non-existent-template"
        coEvery { journalTemplateRepositoryMockk.findById(templateId) } returns null

        // When & Then
        assertFailsWith<JournalTemplateNotFoundException> {
            journalTemplateService.deleteJournalTemplate(templateId)
        }

        coVerify(exactly = 1) { journalTemplateRepositoryMockk.findById(templateId) }
        coVerify(exactly = 0) { journalTemplateRepositoryMockk.deleteById(any()) }
    }
}
