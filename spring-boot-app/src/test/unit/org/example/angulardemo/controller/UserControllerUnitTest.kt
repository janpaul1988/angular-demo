package org.example.angulardemo.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import org.example.angulardemo.dto.UserDTO
import org.example.angulardemo.exception.UserNotFoundException
import org.example.angulardemo.service.UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [UserController::class])
@AutoConfigureWebTestClient
class UserControllerUnitTest(
    @Autowired
    val webTestClient: WebTestClient,
) {
    @MockkBean(relaxed = true)
    lateinit var userService: UserService


    @Test
    fun `should find an existing user by email`() {

        // Given
        val result = UserDTO(1L, name = "testName", email = "test@tester.com")
        coEvery { userService.getUserByEmail(result.email!!) } returns result

        // When
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/users")
                    .queryParam("email", result.email)
                    .build()
            }
            .exchange()

            // Then
            .expectStatus().isOk
            .expectBody(UserDTO::class.java)
            .isEqualTo(result)

        coVerify(exactly = 1) { userService.getUserByEmail(result.email!!) }

    }

    @Test
    fun `should throw if there is no existing user for a certain email`() {

        // Given
        val result = UserDTO(1L, name = "testName", email = "test@tester.com")
        coEvery { userService.getUserByEmail(result.email!!) } throws UserNotFoundException(result.email!!)

        // When
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/users")
                    .queryParam("email", result.email)
                    .build()
            }
            .exchange()

            // Then
            .expectStatus().isNotFound
            .expectBody(String::class.java)
            .isEqualTo("No user registered with email: ${result.email}.")

        coVerify(exactly = 1) { userService.getUserByEmail(result.email!!) }

    }

}
