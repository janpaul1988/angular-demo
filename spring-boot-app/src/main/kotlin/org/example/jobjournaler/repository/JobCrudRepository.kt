package org.example.jobjournaler.repository

import kotlinx.coroutines.flow.Flow
import org.example.jobjournaler.entity.Job
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface JobCrudRepository : CoroutineCrudRepository<Job, String> {
    suspend fun findAllByUserId(userId: Long): Flow<Job>
    suspend fun deleteByUserId(userId: Long)
    suspend fun findAllByUserIdAndEndDateIsNull(userId: Long): Flow<Job>

    @Query("SELECT MAX(end_date) FROM job WHERE user_id = :userId")
    suspend fun findMaxEndDateByUserId(userId: Long): LocalDate?
}
