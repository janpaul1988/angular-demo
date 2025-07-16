package org.example.jobjournaler.controller

import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.example.jobjournaler.dto.JobDTO
import org.example.jobjournaler.service.JobService
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping("/jobs")
class JobController(
    val jobService: JobService,
) {

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun addJob(
        @RequestBody @Valid jobDTO: JobDTO,
    ): JobDTO =
        jobService.addJob(jobDTO)

    @GetMapping("/{userId}")
    suspend fun getAllJobs(
        @PathVariable("userId") userId: Long,
    ): Flow<JobDTO> = jobService.getAllJobsForUser(userId)

    @PutMapping()
    suspend fun updateJob(
        @RequestBody @Valid jobDTO: JobDTO,
    ): JobDTO =
        jobService.updateJob(jobDTO)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteCourse(
        @PathVariable("id") jobId: String,
    ) = jobService.deleteJob(jobId)

}
