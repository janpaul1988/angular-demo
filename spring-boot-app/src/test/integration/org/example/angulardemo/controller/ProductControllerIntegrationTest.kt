package org.example.angulardemo.controller

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.example.angulardemo.repository.ProductCrudRepository
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import kotlin.test.BeforeTest
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ProductControllerIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val productRepository: ProductCrudRepository,
) {


    @BeforeTest
    fun setup() = runBlocking {
        // Given an empty products table in the database.
        productRepository.deleteAll()
    }

    @Test
    fun `should retrieve all products successfully`() = runTest {
        // Given: Save the products reactively and prepare the expected results.
        flowOf(
            Product(null, "testname1", "testdescription1"),
            Product(null, "testname2", "testdescription2")
        ).let {
            productRepository.saveAll(it)
        }.map {
            ProductDTO(it.id, it.name, it.description)
        }.toList()
            .toTypedArray()
            .also {
                // When: Send a GET request to retrieve all products.
                webTestClient.get()
                    .uri("/products")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<ProductDTO>()
                    // Then: Verify the response matches the saved products.
                    .hasSize(2)
                    .contains(*it)
            }
    }

    @Test
    fun `should save product successfully`() {
        // Given: Prepare the product to save.
        ProductDTO(null, "testname1", "testdescription1")
            .also {
                // When: Send a POST request to save the product.
                webTestClient.post()
                    .uri("/products")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody(ProductDTO::class.java)
                    .consumeWith { responseEntity ->
                        // Then: Verify the response contains the saved product with a generated ID.
                        val response = responseEntity.responseBody
                        Assertions.assertEquals(response, it.copy(id = response!!.id))
                    }
            }
    }

    @Test
    fun `should update product successfully`() = runTest {
        // Given: Save the product to the database and prepare the product for update.
        Product(null, "testname", "testdescription")
            .let {
                productRepository.save(it)
            }
            .let {
                assertThat(it.id).isNotNull() // Check that the product is saved.
                ProductDTO(it.id, "updatedTestName", "updatedTestDescription")// Prepare the product for update.
            }.also {
                // When: Send a PUT request to update the product.
                webTestClient.put()
                    .uri("/products/${it.id}")
                    .bodyValue(it)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(ProductDTO::class.java)
                    // Then: Verify the updated product matches the input.
                    .isEqualTo(it)
            }


    }

    @Test
    fun `should delete product successfully`() = runTest {
        // Given: Save the product to the database.
        Product(null, "updatedTestName", "updatedTestDescription")
            .let {
                productRepository.save(it)
            }.also {// Verify the product was saved and has an ID.
                assertThat(it.id).isNotNull()
            }.also {
                // When: Send a DELETE request to remove the product.
                webTestClient.delete()
                    .uri("/products/${it.id}")
                    .exchange()
                    .expectStatus().isNoContent
            }.also {
                // Then: Verify the product is no longer in the database.
                Assertions.assertNull(productRepository.findById(it.id!!))
            }
    }
}
