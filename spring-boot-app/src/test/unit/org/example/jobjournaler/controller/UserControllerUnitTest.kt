package org.example.jobjournaler.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import org.example.jobjournaler.dto.UserDTO
import org.example.jobjournaler.service.UserService
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
    fun `should find an existing user by email or create it if it does not exist`() {

        // Given
        val result = UserDTO(1L, email = "test@tester.com")
        coEvery { userService.getUserByEmail(result.email!!) } returns result

        // When
        webTestClient.get()
            .uri("/users")
            .header("X-Forwarded-Email", result.email)
            .exchange()

            // Then
            .expectStatus().isOk
            .expectBody(UserDTO::class.java)
            .isEqualTo(result)

        coVerify(exactly = 1) { userService.getUserByEmail(result.email!!) }

    }

}
