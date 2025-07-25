package org.example.jobjournaler.repository

import kotlinx.coroutines.flow.Flow
import org.example.jobjournaler.entity.JournalTemplate
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface JournalTemplateCrudRepository : CoroutineCrudRepository<JournalTemplate, String> {
    @Query("SELECT COALESCE(MAX(version), 0) FROM journal_template WHERE user_id = :userId AND name = :name")
    suspend fun findMaxVersionByUserIdAndName(userId: Long, name: String): Int?

    suspend fun findAllByUserId(userId: Long): Flow<JournalTemplate>
}
