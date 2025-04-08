package org.example.angulardemo.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.exception.ProductNotFoundException
import org.example.angulardemo.service.ProductService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier

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
        val body = ProductDTO(name = "testname", description = "testdescription")

        coEvery { productService.addProduct(any()) } returns body

        val result = webTestClient.post()
            .uri("/products")
            .bodyValue(body)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ProductDTO::class.java)
            .returnResult()
            .responseBody

        coVerify(exactly = 1) { productService.addProduct(body) }

        Assertions.assertEquals(body, result)
    }

    @Test
    fun `should not add invalid product`() {
        val body = ProductDTO(name = "", description = "testdescription")

        val result = webTestClient.post()
            .uri("/products")
            .bodyValue(body)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(String::class.java)
            .returnResult()
            .responseBody

        coVerify { productService wasNot Called }
        val expectedMessage = "Product name cannot be blank."
        Assertions.assertEquals(expectedMessage, result)
    }

    @Test
    fun `should update product`() {

        // Given
        val id = 1L
        val requestBody = ProductDTO(name = "testName", description = "testdescription")
        val resultBody = requestBody.copy(id = id)

        coEvery { productService.updateProduct(id, requestBody) } returns resultBody

        // When
        val response = webTestClient.put()
            .uri("/products/$id")
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isOk
            .returnResult(ProductDTO::class.java)
            .responseBody

        // Then
        coVerify(exactly = 1) { productService.updateProduct(id, requestBody) }

        StepVerifier.create(response)
            .expectNext(resultBody)
            .verifyComplete()


    }

    @Test
    fun `should not update invalid product`() {
        // Given
        val id = 1L
        val requestBody = ProductDTO(name = "", description = "testdescription")

        // When
        val response = webTestClient.put()
            .uri("/products/$id")
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isBadRequest
            .returnResult(String::class.java)
            .responseBody

        //Then
        coVerify { productService wasNot Called }

        StepVerifier.create(response)
            .expectNext("Product name cannot be blank.")
            .verifyComplete()
    }

    @Test
    fun `should not update non-existing product`() {
        // Given
        val id = 1L
        val body = ProductDTO(name = "testName", description = "testdescription")

        coEvery { productService.updateProduct(id, body) } throws ProductNotFoundException(id)

        // When
        val response = webTestClient.put()
            .uri("/products/$id")
            .bodyValue(body)
            .exchange()
            .expectStatus().isNotFound
            .returnResult(String::class.java)
            .responseBody

        // Then
        coVerify(exactly = 1) { productService.updateProduct(id, body) }

        StepVerifier.create(response)
            .expectNext("Product with id: $id not found")
            .verifyComplete()
    }

    @Test
    fun `should retrieve all products successfully`() {
        // Given
        val expectedProducts = listOf(
            ProductDTO(1, "testname1", "testdescription1"),
            ProductDTO(2, "testname2", "testdescription2")
        )

        coEvery { productService.getAllProducts() } returns flowOf(*expectedProducts.toTypedArray())

        // When we test the endpoint with a GET request.
        val response = webTestClient.get()
            .uri("/products")
            .exchange()
            .expectStatus().isOk
            .returnResult(ProductDTO::class.java)
            .responseBody

        // Then
        coVerify(exactly = 1) { productService.getAllProducts() }

        StepVerifier.create(response)
            .expectNextSequence(expectedProducts)
            .verifyComplete()
    }

    @Test
    fun `should delete product`() {
        val id = 1L
        coEvery { productService.deleteProduct(id) } just runs

        webTestClient.delete()
            .uri("/products/$id")
            .exchange()
            .expectStatus().isNoContent

        coVerify(exactly = 1) { productService.deleteProduct(id) }
    }

    @Test
    fun `should not delete non-existing product`() {
        val id = 1L
        coEvery { productService.deleteProduct(id) } throws ProductNotFoundException(id)

        val result = webTestClient.delete()
            .uri("/products/$id")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(String::class.java)
            .returnResult()
            .responseBody

        coVerify(exactly = 1) { productService.deleteProduct(id) }
        val expectedMessage = "Product with id: $id not found"
        Assertions.assertEquals(expectedMessage, result)
    }

    @Test
    fun `should provide correct message on unexpected error`() {
        val id = 1L
        coEvery { productService.deleteProduct(id) } throws Exception("Potentially sensitive system information")

        val result = webTestClient.delete()
            .uri("/products/$id")
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody(String::class.java)
            .returnResult()
            .responseBody

        coVerify(exactly = 1) { productService.deleteProduct(id) }

        val expectedMessage = "An unexpected internal server error occurred. Please contact the system administrator."
        Assertions.assertEquals(expectedMessage, result)
    }
}
