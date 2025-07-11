package org.example.angulardemo.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import org.example.angulardemo.dto.JobDTO
import org.example.angulardemo.entity.Job
import org.example.angulardemo.exception.JobConstraintViolationException
import org.example.angulardemo.exception.JobNotFoundException
import org.example.angulardemo.exception.JobStartDateViolationException
import org.example.angulardemo.mapper.JobMapper
import org.example.angulardemo.repository.JobCrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class JobService(
    val jobCrudRepository: JobCrudRepository,
    val userService: UserService,
    val jobMapper: JobMapper,
) {

    suspend fun addJob(
        jobDto: JobDTO,
    ): JobDTO {
        val job = jobMapper.toEntity(jobDto)

        // Fail if the user does not exist.
        userService.doesUserExist(job.userId)

        // Fail if there are jobs for this user without endDate.
        doesUserHaveJobsWithoutEndDate(job.userId)

        // Fail if the job's startDate is before the endDate of the previously finished job.
        isStartDateValid(job.startDate, job.userId)

        return jobMapper.toDto((jobCrudRepository.save(job)))
    }

    suspend fun getAllJobsForUser(userId: Long): Flow<JobDTO> =
        jobCrudRepository.findAllByUserId(userId)
            .map {
                jobMapper.toDto(it)
            }

    suspend fun updateJob(job: JobDTO): JobDTO {
        return doesJobExist(job.id!!)
            .let {
                val jobToUpdateTo = jobMapper.toEntity(job);
                it.title = jobToUpdateTo.title
                it.description = jobToUpdateTo.description
                it.endDate = jobToUpdateTo.endDate
                it.currentJournalTemplateId = jobToUpdateTo.currentJournalTemplateId
                jobCrudRepository.save(it)
            }.let {
                jobMapper.toDto(it)
            }
    }

    suspend fun deleteJob(jobId: String) {
        doesJobExist(jobId)
        jobCrudRepository.deleteById(jobId)
    }


    private suspend fun isStartDateValid(startDate: LocalDate, userId: Long) {
        jobCrudRepository.findMaxEndDateByUserId(userId)?.let { maxEndDate ->
            when {
                startDate.isBefore(maxEndDate) -> throw JobStartDateViolationException(
                    userId,
                    maxEndDate
                ).also { logger.error { it.message } }

                else -> return
            }
        }
    }

    private suspend fun doesUserHaveJobsWithoutEndDate(userId: Long) {
        if (jobCrudRepository.findAllByUserIdAndEndDateIsNull(userId).count() > 0) {
            throw JobConstraintViolationException(userId).also {
                logger.error { it.message }
            }
        }
    }

    suspend fun doesJobExist(jobId: String): Job {
        return jobCrudRepository.findById(jobId) ?: throw JobNotFoundException(
            jobId
        ).also {
            logger.error { it.message }
        }
    }


}
