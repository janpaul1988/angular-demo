package org.example.angulardemo.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.example.angulardemo.configuration.ProductConfiguration
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.example.angulardemo.entity.User
import org.example.angulardemo.exception.ProductNotFoundException
import org.example.angulardemo.exception.UserNotFoundException
import org.example.angulardemo.mapper.ProductMapper
import org.example.angulardemo.repository.ProductCrudRepository
import org.example.angulardemo.repository.UserCrudRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class ProductServiceUnitTest(
    @RelaxedMockK
    private val productCrudRepositoryMockk: ProductCrudRepository,
    @RelaxedMockK
    private val userCrudRepositoryMockk: UserCrudRepository,
    @RelaxedMockK
    private val productMapperMockk: ProductMapper,
    @RelaxedMockK
    private val productConfigurationMockk: ProductConfiguration,
    @InjectMockKs
    private val productService: ProductService,
) {

    @BeforeTest()
    fun setUpLogging() {
        val logger = LoggerFactory.getLogger("org.example.angulardemo") as Logger
        logger.level = Level.DEBUG
    }

    @Test
    fun `should add new product`() = runTest {
        // Given
        val productDTO = mockk<ProductDTO>()
        val productEntity = mockk<Product>(relaxed = true)
        val savedProductEntity = mockk<Product>()
        val savedProductDTO = mockk<ProductDTO>()
        val userId = 1L

        // Mock behavior
        every { productConfigurationMockk.maxInserts } returns 10
        coEvery { userCrudRepositoryMockk.findById(userId) } returns mockk<User>()
        every { productMapperMockk.toEntity(productDTO) } returns productEntity
        coEvery { productCrudRepositoryMockk.findMaxExternalIdByUserId(userId) } returns 1L
        every { productConfigurationMockk.externalIdStartingValue } returns 0L
        every { productConfigurationMockk.externalIdIncrementValue } returns 1L
        coEvery { productCrudRepositoryMockk.save(productEntity) } returns savedProductEntity
        every { productMapperMockk.toDto(savedProductEntity) } returns savedProductDTO

        // Capture the externalId that is set.
        val externalIdSlot = slot<Long>()
        every { productEntity.externalId = capture(externalIdSlot) } just Runs

        // When
        val result = productService.addProduct(userId, productDTO)

        // Then
        coVerify(exactly = 1) { userCrudRepositoryMockk.findById(userId) }
        verify(exactly = 1) { productMapperMockk.toEntity(productDTO) }

        coVerify(exactly = 1) { productCrudRepositoryMockk.findMaxExternalIdByUserId(userId) }
        coVerify(exactly = 1) { productCrudRepositoryMockk.save(productEntity) }
        verify(exactly = 1) { productMapperMockk.toDto(savedProductEntity) }
        assertEquals(savedProductDTO, result)
        assertEquals(2L, externalIdSlot.captured) // <-- This checks the incremented value
    }

    @Test
    fun `add new product for user that does not exist should throw`() = runTest {
        // Given
        val productDTO = mockk<ProductDTO>()
        val userId = 1L

        every { productConfigurationMockk.maxInserts } returns 10
        coEvery { userCrudRepositoryMockk.findById(userId) } returns null
        assertFailsWith<UserNotFoundException> { productService.addProduct(userId, productDTO) }

        coVerify(exactly = 1) { userCrudRepositoryMockk.findById(userId) }
        verify { productMapperMockk wasNot called }
        coVerify { productCrudRepositoryMockk wasNot called }
    }

    @Test
    fun `add new product should reattempt insertion the correct number of times and when the amount-of-retries reaches zero should throw`() =
        runTest {
            // Given
            val productDTO = mockk<ProductDTO>()
            val productEntity = mockk<Product>(relaxed = true)
            val userId = 1L

            every { productConfigurationMockk.maxInserts } returns 10
            coEvery { userCrudRepositoryMockk.findById(userId) } returns mockk<User>()
            every { productMapperMockk.toEntity(productDTO) } returns productEntity
            coEvery { productCrudRepositoryMockk.findMaxExternalIdByUserId(userId) } returns 0L
            every { productConfigurationMockk.externalIdStartingValue } returns 0L
            every { productConfigurationMockk.externalIdIncrementValue } returns 1L
            coEvery { productCrudRepositoryMockk.save(productEntity) } throws DuplicateKeyException("Error, key exists")


            // When
            assertFailsWith<IllegalStateException> { productService.addProduct(userId, productDTO) }

            // Then
            coVerify(exactly = 10) { userCrudRepositoryMockk.findById(userId) }
            verify(exactly = 10) { productMapperMockk.toEntity(productDTO) }
            coVerify(exactly = 10) { productCrudRepositoryMockk.findMaxExternalIdByUserId(userId) }
            coVerify(exactly = 10) { productCrudRepositoryMockk.save(productEntity) }
            verify(exactly = 0) { productMapperMockk.toDto(any()) }
        }

    @Test
    fun `should get all products for a certain user`() = runTest {
        // Given
        val productFlow = flowOf(mockk<Product>(), mockk<Product>())
        val productList = productFlow.toList()
        val productDTOList = listOf(mockk<ProductDTO>(), mockk<ProductDTO>())
        val userId = 1L
        coEvery { productCrudRepositoryMockk.findAllByUserId(userId) } returns productFlow

        // Pair products with their corresponding DTOs and mock the mapper behavior
        productList.zip(productDTOList).forEach { (product, productDTO) ->
            every { productMapperMockk.toDto(product) } returns productDTO
        }

        // When
        val result = productService.getAllProductsForUser(userId).toList()

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findAllByUserId(userId) }
        verify(exactly = productList.size) { productMapperMockk.toDto(any()) }

        assertEquals(productDTOList, result)
    }


    @Test
    fun `should update product`() = runTest {
        val productId = 1L
        val userId = 1L
        val productDTO = ProductDTO(productId, userId, name = "testUpdate", description = "testUpdate")
        val productEntity = Product(null, userId, productId, name = "test", description = "update")
        val productEntityToSave = productEntity.copy(
            name = productDTO.name,
            description = productDTO.description
        )
        val savedProductEntity = mockk<Product>()
        val savedProductDTO = mockk<ProductDTO>()

        coEvery { productCrudRepositoryMockk.findByUserIdAndExternalId(userId, productId) } returns productEntity
        coEvery {
            productCrudRepositoryMockk.save(
                productEntityToSave
            )
        } returns savedProductEntity

        every { productMapperMockk.toDto(savedProductEntity) } returns savedProductDTO

        // When
        val result = productService.updateProduct(userId, productId, productDTO)

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findByUserIdAndExternalId(userId, productId) }
        coVerify(exactly = 1) { productCrudRepositoryMockk.save(productEntityToSave) }
        verify(exactly = 1) { productMapperMockk.toDto(savedProductEntity) }

        assertEquals(savedProductDTO, result)
    }

    @Test
    fun `update product that does not exist should throw`() = runTest {
        // Given
        val userId = 1L
        val productId = 1L
        coEvery {
            productCrudRepositoryMockk.findByUserIdAndExternalId(
                userId,
                productId
            )
        } returns null

        // When
        assertFailsWith<ProductNotFoundException> {
            productService.updateProduct(userId, productId, mockk<ProductDTO>())
        }

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findByUserIdAndExternalId(userId, productId) }
        coVerify(exactly = 0) { productCrudRepositoryMockk.save(any()) }
        verify { productMapperMockk wasNot called }
    }

    @Test
    fun `should delete product`() = runTest {
        // Given
        val userId = 1L
        val productId = 1L
        coEvery { productCrudRepositoryMockk.findByUserIdAndExternalId(userId, productId) } returns mockk<Product>()

        // When
        productService.deleteProduct(userId, productId)

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findByUserIdAndExternalId(userId, productId) }
        coVerify(exactly = 1) { productCrudRepositoryMockk.deleteByUserIdAndExternalId(userId, productId) }
    }

    @Test
    fun `delete product that does not exist should throw`() = runTest {
        // Given
        val userId = 1L
        val productId = 1L
        coEvery { productCrudRepositoryMockk.findByUserIdAndExternalId(userId, productId) } returns null

        // When
        assertFailsWith<ProductNotFoundException> {
            productService.deleteProduct(userId, productId)
        }

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findByUserIdAndExternalId(userId, productId) }
        coVerify(exactly = 0) { productCrudRepositoryMockk.deleteByUserIdAndExternalId(userId, productId) }
    }
}
