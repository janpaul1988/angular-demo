package org.example.angulardemo.mapper

import org.example.angulardemo.dto.JobDTO
import org.example.angulardemo.entity.Job
import org.springframework.stereotype.Component

@Component
class JobMapper {

    fun toEntity(jobDto: JobDTO): Job {
        return Job(
            id = jobDto.id,
            userId = jobDto.userId,
            title = jobDto.title!!,
            description = jobDto.description,
            startDate = jobDto.startDate!!,
            endDate = jobDto.endDate,
            currentJournalTemplateId = jobDto.currentJournalTemplateId
        )
    }

    fun toDto(job: Job): JobDTO {
        return JobDTO(
            id = job.id,
            userId = job.userId,
            title = job.title,
            description = job.description,
            startDate = job.startDate,
            endDate = job.endDate,
            currentJournalTemplateId = job.currentJournalTemplateId
        )
    }

}
