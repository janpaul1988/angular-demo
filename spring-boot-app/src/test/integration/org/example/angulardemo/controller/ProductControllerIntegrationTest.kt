package org.example.angulardemo.controller

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.example.angulardemo.repository.ProductCrudRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier
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
    fun `should retrieve all products successfully`() {

        // Given: Save the products reactively and prepare the expected results.
        val response = mono {
            flowOf(
                Product(null, "testname1", "testdescription1"),
                Product(null, "testname2", "testdescription2")
            ).let {
                productRepository.saveAll(it)
            }.toList().map {
                ProductDTO(it.id, it.name, it.description)
            }
        }.flatMapMany { expectedProducts ->
            // When: Send a GET request to retrieve all products.
            webTestClient.get()
                .uri("/products")
                .exchange()
                .expectStatus().isOk
                .returnResult(ProductDTO::class.java)
                .responseBody
                .collectList()
                .map { actualProducts -> Pair(expectedProducts, actualProducts) } // Pair expected and actual results.
        }

        // Then: Verify the response matches the saved products.
        StepVerifier.create(response)
            .assertNext { (expectedProducts, actualProducts) ->
                assertThat(actualProducts).containsExactlyInAnyOrderElementsOf(expectedProducts)
            }
            .verifyComplete()
    }

    @Test
    fun `should save product successfully`() {
        // Given: Prepare the product to save.
        val productToSave = ProductDTO(null, "testname1", "testdescription1")

        // When: Send a POST request to save the product.
        val response = webTestClient.post()
            .uri("/products")
            .bodyValue(productToSave)
            .exchange()
            .expectStatus().isCreated
            .returnResult(ProductDTO::class.java)
            .responseBody

        // Then: Verify the response contains the saved product with a generated ID.
        StepVerifier.create(response)
            .expectNextMatches { product ->
                product == productToSave.copy(id = product.id)
            }
            .verifyComplete()
    }

    @Test
    fun `should update product successfully`() {
        val response = mono {
            // Given: Save the product to the database and prepare the product for update.
            Product(null, "testname", "testdescription")
                .let {
                    productRepository.save(it)
                }
                .let {
                    assertThat(it.id).isNotNull() // Check that the product is saved.
                    ProductDTO(it.id, "updatedTestName", "updatedTestDescription")// Prepare the product for update.
                }
        }.flatMapMany {
            // When: Send a PUT request to update the product.
            webTestClient.put()
                .uri("/products/${it.id}")
                .bodyValue(it)
                .exchange()
                .expectStatus().isOk
                .returnResult(ProductDTO::class.java)
                .responseBody
        }

        // Then: Verify the updated product matches the input.
        StepVerifier.create(response)
            .expectNextMatches { product ->
                product.id != null
                        && product.name == "updatedTestName"
                        && product.description == "updatedTestDescription"
            }
            .verifyComplete()
    }

    @Test
    fun `should delete product successfully`() {
        // Given: Save the product to the database.
        val productToDelete = mono {
            Product(null, "updatedTestName", "updatedTestDescription").let {
                productRepository.save(it)
            }
        }
            .flatMap { product ->
                assertThat(product.id).isNotNull() // Verify the product was saved and has an ID.
                // When: Send a DELETE request to remove the product.
                webTestClient.delete()
                    .uri("/products/${product.id}")
                    .exchange()
                    .expectStatus().isNoContent
                mono { productRepository.findById(product.id!!) }// Check if the product still exists in the database.
            }

        // Then: Verify the product is no longer in the database.
        StepVerifier.create(productToDelete)
            .expectNextCount(0)
            .verifyComplete()

    }
}
