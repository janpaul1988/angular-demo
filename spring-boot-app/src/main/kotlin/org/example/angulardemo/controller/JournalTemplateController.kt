package org.example.angulardemo.controller

import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.example.angulardemo.dto.JournalTemplateDTO
import org.example.angulardemo.service.JournalTemplateService
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping("/journaltemplates")
class JournalTemplateController(
    val journalTemplateService: JournalTemplateService,
) {

    @GetMapping("/{id}")
    suspend fun getJournalTemplateById(
        @PathVariable("id") journalTemplateId: String,
    ): JournalTemplateDTO =
        journalTemplateService.findJournalTemplateDtoById(journalTemplateId)

    @GetMapping("/user/{userId}")
    suspend fun getJournalTemplatesByUserId(
        @PathVariable("userId") userId: Long,
    ): Flow<JournalTemplateDTO> =
        journalTemplateService.findJournalTemplatesByUserId(userId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun saveJournalTemplate(@RequestBody @Valid journalTemplateDTO: JournalTemplateDTO): JournalTemplateDTO =
        journalTemplateService.createOrUpdateTemplate(journalTemplateDTO);


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteJournalTemplate(
        @PathVariable("id") journalTemplateId: String,
    ) = journalTemplateService.deleteJournalTemplate(journalTemplateId)
}
