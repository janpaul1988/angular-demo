package org.example.angulardemo.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.exception.ProductNotFoundException
import org.example.angulardemo.service.ProductService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [ProductController::class])
@AutoConfigureWebTestClient
class ProductControllerUnitTest(
    @Autowired
    val webTestClient: WebTestClient,
) {
    @MockkBean(relaxed = true)
    lateinit var productService: ProductService


    @Test
    fun `should add product for user`() {
        val userId = 1L
        val productToAdd = ProductDTO(null, userId, "testname", "testdescription")
        val productAdded = productToAdd.copy(id = 1L)
        // Given

        coEvery { productService.addProduct(userId, productToAdd) } returns productAdded

        webTestClient.post()                                                           // When
            .uri("/products/$userId")
            .bodyValue(productToAdd)
            .exchange()
            .expectStatus().isCreated                                                  // Then
            .expectBody(ProductDTO::class.java)
            .isEqualTo(productAdded)

        coVerify(exactly = 1) { productService.addProduct(userId, productToAdd) }

    }

    @Test
    fun `should not add product for user without name`() {
        ProductDTO(null, 1L, name = "", description = "testdescription")                         // Given
            .also {
                webTestClient.post()                                                   // When
                    .uri("/products/${it.userId}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isBadRequest                                       // Then
                    .expectBody(String::class.java)
                    .isEqualTo("Product name cannot be blank.")
            }.also {
                coVerify { productService wasNot Called }
            }
    }

    @Test
    fun `should update product`() {

        ProductDTO(1L, 1L, name = "testName", description = "testdescription")                          // Given
            .let {
                // Pair the request to its expected result.
                it to it.copy(id = it.id, userId = it.userId, name = "updatedName", description = "updatedDescription")
            }.also {
                coEvery { productService.updateProduct(it.first.userId, it.first.id!!, it.first) } returns it.second
            }.also {                                                      // When
                webTestClient.put()
                    .uri("/products/${it.first.userId}/${it.first.id}")
                    .bodyValue(it.first)
                    .exchange()
                    .expectStatus().isOk                                                              // Then
                    .expectBody(ProductDTO::class.java)
                    .isEqualTo(it.second)
            }.also {// Then
                coVerify(exactly = 1) { productService.updateProduct(it.first.userId, it.first.id!!, it.first) }
            }
    }

    @Test
    fun `should not update invalid product`() {

        ProductDTO(1L, 1L, name = "", description = "testdescription")          // Given
            .also {
                webTestClient.put()                                             // When
                    .uri("/products/${it.userId}/${it.id}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isBadRequest                                // Then
                    .expectBody(String::class.java)
                    .isEqualTo("Product name cannot be blank.")
            }.also {
                coVerify { productService wasNot Called }
            }
    }

    @Test
    fun `should not update non-existing product`() {

        ProductDTO(1L, 1L, name = "testName", description = "testdescription")            // Given
            .also {
                coEvery {
                    productService.updateProduct(
                        it.userId,
                        it.id!!,
                        it
                    )
                } throws ProductNotFoundException(it.userId, it.id!!)
            }
            .also {
                webTestClient.put()                                                      // When
                    .uri("/products/${it.userId}/${it.id}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isNotFound                                          // Then
                    .expectBody(String::class.java)
                    .isEqualTo("Product with id: ${it.id} not found for user with id: ${it.userId}")
            }
            .also {
                coVerify(exactly = 1) { productService.updateProduct(it.userId, it.id!!, it) }
            }
    }

    @Test
    fun `should retrieve all products successfully`() {
        val userId = 1L
        // Given
        arrayOf(
            ProductDTO(1, userId, "testname1", "testdescription1"),
            ProductDTO(2, userId, "testname2", "testdescription2")
        )
            .also {
                coEvery { productService.getAllProductsForUser(userId) } returns flowOf(*it)
            }
            .also {
                webTestClient.get()
                    .uri("/products/${userId}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(ProductDTO::class.java)
                    .contains(*it)
            }
            .also {
                coVerify(exactly = 1) { productService.getAllProductsForUser(userId) }
            }
    }

    @Test
    fun `should delete product`() {
        val id = 1L                                                 // Given
        val userId = 1L

        webTestClient.delete()                                      // When
            .uri("/products/$userId/$id")
            .exchange()
            .expectStatus().isNoContent                             // Then

        coVerify(exactly = 1) { productService.deleteProduct(userId, id) }
    }

    @Test
    fun `should not delete non-existing product`() {
        val id = 1L                                                                                        // Given
        val userId = 1L

        coEvery { productService.deleteProduct(userId, id) } throws ProductNotFoundException(userId, id)

        webTestClient.delete()                                                                             // When
            .uri("/products/$userId/$id")
            .exchange()
            .expectStatus().isNotFound                                                                     // Then
            .expectBody(String::class.java)
            .isEqualTo("Product with id: $id not found for user with id: $userId")

        coVerify(exactly = 1) { productService.deleteProduct(userId, id) }

    }

    @Test
    fun `should provide correct message on unexpected error`() {
        // Given
        val id = 1L
        val userId = 1L

        coEvery {
            productService.deleteProduct(
                userId,
                id
            )
        } throws Exception("Potentially sensitive system information")

        webTestClient.delete()                                                                                   // When
            .uri("/products/$userId/$id")
            .exchange()
            .expectStatus().is5xxServerError                                                                     // Then
            .expectBody(String::class.java)
            .isEqualTo("An unexpected internal server error occurred. Please contact the system administrator.")

        coVerify(exactly = 1) { productService.deleteProduct(userId, id) }
    }
}
