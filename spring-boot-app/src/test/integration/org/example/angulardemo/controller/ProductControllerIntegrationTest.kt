package org.example.angulardemo.controller

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.example.angulardemo.entity.User
import org.example.angulardemo.repository.ProductCrudRepository
import org.example.angulardemo.repository.UserCrudRepository
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.util.Collections.synchronizedSet
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ProductControllerIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val productRepository: ProductCrudRepository,
    @Autowired private val userCrudRepository: UserCrudRepository,
) {
    var userId: Long = 0L;

    @BeforeTest
    fun setup() = runBlocking {
        // Given an empty products table in the database.
        productRepository.deleteAll()
        userCrudRepository.deleteAll()
        userId = userCrudRepository.save(User(null, "testuser", "testemail")).id!!
    }

    @Test
    fun `should retrieve all products successfully for a user`() = runTest {
        // Given: Save the products reactively and prepare the expected results.
        flowOf(
            Product(null, userId, 1, "testname1", "testdescription1"),
            Product(null, userId, 2, "testname2", "testdescription2")
        ).let {
            productRepository.saveAll(it)
        }.map {
            ProductDTO(it.externalId, it.userId, it.name, it.description)
        }.toList()
            .toTypedArray()
            .also {
                // When: Send a GET request to retrieve all products.
                webTestClient.get()
                    .uri("/products/${userId}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<ProductDTO>()
                    // Then: Verify the response matches the saved products.
                    .hasSize(2)
                    .contains(*it)
            }
    }

    @Test
    fun `should save product successfully`() = runTest {
        // Given: Prepare the product to save.
        ProductDTO(null, userId, "testname1", "testdescription1")
            .also {
                // When: Send a POST request to save the product.
                webTestClient.post()
                    .uri("/products/${userId}")
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
    fun `concurrent product creation should assign unique externalIds`() = runTest {
        val productNumbers = synchronizedSet(mutableSetOf<Long>())
        val threadPoolSize = 1000;
        List(threadPoolSize) {
            async {
                ProductDTO(null, userId, "Test", "Test").let {
                    webTestClient.post()
                        .uri("/products/${userId}")
                        .bodyValue(it)
                        .exchange()
                        .expectStatus().isCreated
                        .expectBody(ProductDTO::class.java)
                        .consumeWith { responseEntity ->
                            // Then: Verify the response contains the saved product with a generated ID.
                            val response = responseEntity.responseBody
                            Assertions.assertEquals(response, it.copy(id = response!!.id))
                            productNumbers.add(response.id)
                        }
                }
            }
        }.awaitAll()

        assertEquals(threadPoolSize, productNumbers.size) // all unique
    }

    @Test
    fun `should update product successfully`() = runTest {
        // Given: Save the product to the database and prepare the product for update.
        Product(null, userId, 1L, "testname", "testdescription")
            .let {
                productRepository.save(it)
            }
            .let {
                assertThat(it.id).isNotNull() // Check that the product is saved.
                ProductDTO(
                    it.externalId,
                    userId,
                    "updatedTestName",
                    "updatedTestDescription"
                )// Prepare the product for update.
            }.also {
                // When: Send a PUT request to update the product.
                webTestClient.put()
                    .uri("/products/${it.userId}/${it.id}")
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
        Product(null, userId, 1L, "updatedTestName", "updatedTestDescription")
            .let {
                productRepository.save(it)
            }.also {// Verify the product was saved and has an ID.
                assertThat(it.id).isNotNull()
            }.also {
                // When: Send a DELETE request to remove the product.
                webTestClient.delete()
                    .uri("/products/${it.userId}/${it.externalId}")
                    .exchange()
                    .expectStatus().isNoContent
            }.also {
                // Then: Verify the product is no longer in the database.
                Assertions.assertNull(productRepository.findById(it.id!!))
            }
    }
}


