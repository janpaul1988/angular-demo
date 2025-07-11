package org.example.angulardemo.repository

import kotlinx.coroutines.flow.Flow
import org.example.angulardemo.entity.Journal
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface JournalCrudRepository : CoroutineCrudRepository<Journal, String> {
    suspend fun findByYearAndWeekAndJobId(year: Int, week: Int, jobId: String): Journal?
    suspend fun findByJobId(jobId: String): Flow<Journal>
}
