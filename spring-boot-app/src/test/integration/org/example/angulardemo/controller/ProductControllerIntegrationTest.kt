package org.example.angulardemo.controller

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.example.angulardemo.repository.ProductCrudRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ProductControllerIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val productRepository: ProductCrudRepository,
) {


    @BeforeEach
    fun setup() = runBlocking {
        productRepository.deleteAll()
    }

    @Test
    fun `should retrieve all products successfully`() {

        // Given the following saved products to the database.
        val expectedCourses = runBlocking {
            flowOf(
                Product(null, "testname1", "testdescription1"),
                Product(null, "testname2", "testdescription2")
            ).let {
                productRepository.saveAll(it)
            }.toList().map {
                ProductDTO(it.id, it.name, it.description)
            }
        }

        // When we test the endpoint.
        val response = webTestClient.get()
            .uri("/products")
            .exchange()
            .expectStatus().isOk
            .returnResult(ProductDTO::class.java)
            .responseBody

        // Then we  expect the result.
        StepVerifier.create(response)
            .expectNextSequence(expectedCourses)
            .verifyComplete()
    }
}
