package org.example.jobjournaler.util

import kotlinx.coroutines.delay
import org.example.jobjournaler.repository.JobCrudRepository
import org.example.jobjournaler.repository.JournalCrudRepository
import org.example.jobjournaler.repository.JournalTemplateCrudRepository
import org.example.jobjournaler.repository.UserCrudRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DatabaseCleanupUtil @Autowired constructor(
    private val journalRepository: JournalCrudRepository,
    private val jobRepository: JobCrudRepository,
    private val templateRepository: JournalTemplateCrudRepository,
    private val userRepository: UserCrudRepository,
) {

    /**
     * Cleans up the database in the correct order to respect referential integrity
     */
    suspend fun cleanDatabase() {
        try {
            // Short delay to ensure previous transactions are complete
            delay(100)

            // First journals as they depend on templates and jobs
            journalRepository.deleteAll()

            // Short delay between deletes
            delay(100)

            // Then jobs as they can reference templates
            jobRepository.deleteAll()

            // Short delay between deletes
            delay(100)

            // Then templates as they depend on users
            templateRepository.deleteAll()

            // Short delay between deletes
            delay(100)

            // Finally users
            userRepository.deleteAll()

            // Final delay to ensure all deletions are processed
            delay(200)
        } catch (e: Exception) {
            println("Error during cleanup: ${e.message}")
            e.printStackTrace()
            // Proceed even if cleanup fails
        }
    }
}
