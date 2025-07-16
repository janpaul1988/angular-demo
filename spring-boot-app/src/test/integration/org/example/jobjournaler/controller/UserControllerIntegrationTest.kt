package org.example.jobjournaler.controller

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.example.jobjournaler.dto.UserDTO
import org.example.jobjournaler.entity.User
import org.example.jobjournaler.repository.UserCrudRepository
import org.example.jobjournaler.util.DatabaseCleanupUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.BeforeTest
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class UserControllerIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val userCrudRepository: UserCrudRepository,
    @Autowired private val databaseCleanupUtil: DatabaseCleanupUtil,
) {

    @BeforeTest
    fun setup() = runBlocking {
        // Use the centralized cleanup utility
        databaseCleanupUtil.cleanDatabase()
    }

    @Test
    fun `should retrieve user successfully by email`() = runTest {
        // Given: Save the jobs reactively and prepare the expected results.
        val email = "testuser@testuser"
        User(email = email)
            .let {
                userCrudRepository.save(it)
            }
            .let {
                UserDTO(it.id, it.email)
            }
            .also {
                // When: Send a GET request to retrieve all jobs.
                webTestClient.get()
                    .uri("/users")
                    .header("X-Forwarded-Email", email)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(UserDTO::class.java)
                    // Then: Verify the response matches the saved jobs.
                    .equals(it)
            }
    }

    @Test
    fun `should create a user successfully if email is not present in the database`() = runTest {
        // Given: a email of a user that does not exist in the database.
        val email = "testuser@testuser"

        // When: Send a GET request to retrieve the user.
        val user =
            webTestClient.get()
                .uri("/users")
                .header("X-Forwarded-Email", email)
                .exchange()
                .expectStatus().isOk
                .expectBody(UserDTO::class.java)
                .returnResult()
                .responseBody

        // Then: Verify the user was created in the database.
        assertThat(user).isNotNull
        assertThat(user!!.email).isEqualTo(email)
        assertThat(user.id).isNotNull
    }


}
