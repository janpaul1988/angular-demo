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
    fun `should add product`() {

        ProductDTO(name = "testname", description = "testdescription")                         // Given
            .also {
                coEvery { productService.addProduct(any()) } returns it
            }.also {
                webTestClient.post()                                                           // When
                    .uri("/products")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isCreated                                                  // Then
                    .expectBody(ProductDTO::class.java)
                    .isEqualTo(it)
            }.also {
                coVerify(exactly = 1) { productService.addProduct(it) }
            }
    }

    @Test
    fun `should not add invalid product`() {
        ProductDTO(name = "", description = "testdescription")                         // Given
            .also {
                webTestClient.post()                                                   // When
                    .uri("/products")
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

        ProductDTO(1L, name = "testName", description = "testdescription")                          // Given
            .let {
                // Pair the request to its expected result.
                it to it.copy(id = it.id, name = "updatedName", description = "updatedDescription")
            }.also {
                coEvery { productService.updateProduct(it.first.id!!, it.first) } returns it.second
            }.also {                                                      // When
                webTestClient.put()
                    .uri("/products/${it.first.id}")
                    .bodyValue(it.first)
                    .exchange()
                    .expectStatus().isOk                                                              // Then
                    .expectBody(ProductDTO::class.java)
                    .isEqualTo(it.second)
            }.also {// Then
                coVerify(exactly = 1) { productService.updateProduct(it.first.id!!, it.first) }
            }
    }

    @Test
    fun `should not update invalid product`() {

        ProductDTO(1L, name = "", description = "testdescription")          // Given
            .also {
                webTestClient.put()                                             // When
                    .uri("/products/${it.id}")
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

        ProductDTO(1L, name = "testName", description = "testdescription")            // Given
            .also {
                coEvery { productService.updateProduct(it.id!!, it) } throws ProductNotFoundException(it.id!!)
            }
            .also {
                webTestClient.put()                                                      // When
                    .uri("/products/${it.id}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isNotFound                                          // Then
                    .expectBody(String::class.java)
                    .isEqualTo("Product with id: ${it.id} not found")
            }
            .also {
                coVerify(exactly = 1) { productService.updateProduct(it.id!!, it) }
            }
    }

    @Test
    fun `should retrieve all products successfully`() {
        // Given
        arrayOf(
            ProductDTO(1, "testname1", "testdescription1"),
            ProductDTO(2, "testname2", "testdescription2")
        )
            .also {
                coEvery { productService.getAllProducts() } returns flowOf(*it)
            }
            .also {
                webTestClient.get()
                    .uri("/products")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(ProductDTO::class.java)
                    .contains(*it)
            }
            .also {
                coVerify(exactly = 1) { productService.getAllProducts() }
            }
    }

    @Test
    fun `should delete product`() {
        val id = 1L                                                 // Given

        webTestClient.delete()                                      // When
            .uri("/products/$id")
            .exchange()
            .expectStatus().isNoContent                             // Then

        coVerify(exactly = 1) { productService.deleteProduct(id) }
    }

    @Test
    fun `should not delete non-existing product`() {
        val id = 1L                                                                                        // Given

        coEvery { productService.deleteProduct(id) } throws ProductNotFoundException(id)

        webTestClient.delete()                                                                             // When
            .uri("/products/$id")
            .exchange()
            .expectStatus().isNotFound                                                                     // Then
            .expectBody(String::class.java)
            .isEqualTo("Product with id: $id not found")

        coVerify(exactly = 1) { productService.deleteProduct(id) }

    }

    @Test
    fun `should provide correct message on unexpected error`() {
        val id = 1L                                                                                             // Given

        coEvery { productService.deleteProduct(id) } throws Exception("Potentially sensitive system information")

        webTestClient.delete()                                                                                   // When
            .uri("/products/$id")
            .exchange()
            .expectStatus().is5xxServerError                                                                     // Then
            .expectBody(String::class.java)
            .isEqualTo("An unexpected internal server error occurred. Please contact the system administrator.")

        coVerify(exactly = 1) { productService.deleteProduct(id) }
    }
}
