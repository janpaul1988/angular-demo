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
        // Given an empty products table in the database.
        productRepository.deleteAll()
    }

    @Test
    fun `should retrieve all products successfully`() {

        // Given the following saved products to the database.
        val expectedProducts = runBlocking {
            flowOf(
                Product(null, "testname1", "testdescription1"),
                Product(null, "testname2", "testdescription2")
            ).let {
                productRepository.saveAll(it)
            }.toList().map {
                ProductDTO(it.id, it.name, it.description)
            }
        }

        // When we test the endpoint with a GET request.
        val response = webTestClient.get()
            .uri("/products")
            .exchange()
            .expectStatus().isOk
            .returnResult(ProductDTO::class.java)
            .responseBody

        // Then we  expect the result to match the saved products.
        StepVerifier.create(response)
            .expectNextSequence(expectedProducts)
            .verifyComplete()
    }

    @Test
    fun `should save product successfully`() {
        // Given a product we want to save.
        val productToSave = ProductDTO(null, "testname1", "testdescription1")

        // When we save that product with the http POST method.
        val response = webTestClient.post()
            .uri("/products")
            .bodyValue(productToSave)
            .exchange()
            .expectStatus().isCreated
            .returnResult(ProductDTO::class.java)
            .responseBody

        // Then we  expect the result to match the product we want to save.
        StepVerifier.create(response)
            .expectNextMatches { product ->
                product.id != null
                        && product.id!! > 0
                        && product == productToSave.copy(id = product.id)
            }
            .verifyComplete()
    }

    @Test
    fun `should update product successfully`() {
        // Given a single product that is saved to the database.
        val savedProduct = runBlocking {

            Product(null, "testname", "testdescription")
                .let {
                    productRepository.save(it)
                }
        }
        // And given a set of values we want to change on that product.
        val productToPut = savedProduct.let {
            ProductDTO(it.id, "updatedTestName", "updatedTestDescription")
        }

        // When we test the endpoint with a PUT request.
        val response = webTestClient.put()
            .uri("/products/${productToPut.id}")
            .bodyValue(productToPut)
            .exchange()
            .expectStatus().isOk
            .returnResult(ProductDTO::class.java)
            .responseBody

        // Then we  expect the result to match the product we put.
        StepVerifier.create(response)
            .expectNext(productToPut)
            .verifyComplete()
    }

    @Test
    fun `should delete product successfully`() {

        // Given a single product that is saved to the database.
        val savedProduct = runBlocking {

            Product(null, "testname", "testdescription")
                .let {
                    productRepository.save(it)
                }
        }

        // When we test the endpoint with a DELETE request,
        // Then we expect no content.
        webTestClient.delete()
            .uri("/products/${savedProduct.id}")
            .exchange()
            .expectStatus().isNoContent
    }
}
