package org.example.angulardemo.controller

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.example.angulardemo.dto.UserDTO
import org.example.angulardemo.entity.User
import org.example.angulardemo.repository.ProductCrudRepository
import org.example.angulardemo.repository.UserCrudRepository
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
    @Autowired private val productCrudRepository: ProductCrudRepository,
) {

    @BeforeTest
    fun setup() = runBlocking {
        // Given empty tables in the database.
        productCrudRepository.deleteAll()
        userCrudRepository.deleteAll()
    }

    @Test
    fun `should retrieve user successfully by email`() = runTest {
        // Given: Save the products reactively and prepare the expected results.
        val email = "testuser@testuser"
        User(name = "testuser", email = email)
            .let {
                userCrudRepository.save(it)
            }
            .let {
                UserDTO(it.id, it.name, it.email)
            }
            .also {
                // When: Send a GET request to retrieve all products.
                webTestClient.get()
                    .uri { uriBuilder ->
                        uriBuilder.path("/users")
                            .queryParam("email", email)
                            .build()
                    }
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(UserDTO::class.java)
                    // Then: Verify the response matches the saved products.
                    .equals(it)
            }
    }

}
