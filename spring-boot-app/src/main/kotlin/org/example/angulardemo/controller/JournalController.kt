package org.example.angulardemo.controller

import jakarta.validation.Valid
import org.example.angulardemo.dto.JournalDTO
import org.example.angulardemo.service.JournalService
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping("/journals")
class JournalController(
    val journalService: JournalService,
) {

    @GetMapping()
    suspend fun getJournalByWeekYearAndJobId(
        @RequestParam year: Int,
        @RequestParam week: Int,
        @RequestParam jobId: String,
    ): JournalDTO =
        journalService.getJournalByYearWeekAndJobId(year, week, jobId)

    @GetMapping("/job/{jobId}")
    suspend fun getJournalsJobId(@PathVariable jobId: String): JournalDTO =
        journalService.getJournalsByJobId(jobId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun saveJournal(@RequestBody @Valid journalDTO: JournalDTO): JournalDTO =
        journalService.saveJournal(journalDTO);

    @PutMapping
    suspend fun updateJournal(@RequestBody @Valid journalDTO: JournalDTO): JournalDTO =
        journalService.updateJournal(journalDTO);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteJournal(
        @PathVariable("id") journalId: String,
    ) = journalService.deleteJournal(journalId)
}
