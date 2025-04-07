package org.example.angulardemo.service

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.example.angulardemo.exception.ProductNotFoundException
import org.example.angulardemo.mapper.ProductMapper
import org.example.angulardemo.repository.ProductCrudRepository
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class ProductServiceUnitTest(
    @RelaxedMockK
    private val productCrudRepositoryMockk: ProductCrudRepository,
    @RelaxedMockK
    private val productMapperMockk: ProductMapper,
    @InjectMockKs
    private val productService: ProductService,
) {

    @Test
    fun `should add new product`() {
        // Given
        val productDTO = mockk<ProductDTO>()
        val productEntity = mockk<Product>()
        val savedProductEntity = mockk<Product>()
        val savedProductDTO = mockk<ProductDTO>()

        // Mock behavior
        every { productMapperMockk.toEntity(productDTO) } returns productEntity
        coEvery { productCrudRepositoryMockk.save(productEntity) } returns savedProductEntity
        every { productMapperMockk.toDto(savedProductEntity) } returns savedProductDTO

        // When
        val result = runBlocking { productService.addProduct(productDTO) }

        // Then
        coVerify(exactly = 1) { productMapperMockk.toEntity(productDTO) }
        coVerify(exactly = 1) { productCrudRepositoryMockk.save(productEntity) }
        coVerify(exactly = 1) { productMapperMockk.toDto(savedProductEntity) }
        assertEquals(savedProductDTO, result)
    }

    @Test
    fun `should get all products`() {
        // Given
        val productFlow = flowOf(mockk<Product>(), mockk<Product>())
        val productList = runBlocking { productFlow.toList() }
        val productDTOList = listOf(mockk<ProductDTO>(), mockk<ProductDTO>())
        every { productCrudRepositoryMockk.findAll() } returns productFlow

        // Pair products with their corresponding DTOs and mock the mapper behavior
        productList.zip(productDTOList).forEach { (product, productDTO) ->
            every { productMapperMockk.toDto(product) } returns productDTO
        }

        // When
        val result = runBlocking { productService.getAllProducts().toList() }

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findAll() }
        verify(exactly = productList.size) { productMapperMockk.toDto(any()) }

        assertEquals(productDTOList, result)
    }

    @Test
    fun `should update product`() {
        val productId = 1L
        val productDTO = mockk<ProductDTO>()
        val productEntity = mockk<Product>()
        val savedProductEntity = mockk<Product>()
        val savedProductDTO = mockk<ProductDTO>()

        coEvery { productCrudRepositoryMockk.findById(productId) } returns productEntity
        every { productMapperMockk.toEntity(productDTO) } returns productEntity
        coEvery { productCrudRepositoryMockk.save(productEntity) } returns savedProductEntity
        every { productMapperMockk.toDto(savedProductEntity) } returns savedProductDTO

        // When
        val result = runBlocking { productService.updateProduct(1L, productDTO) }

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findById(productId) }
        verify(exactly = 1) { productMapperMockk.toEntity(productDTO) }
        coVerify(exactly = 1) { productCrudRepositoryMockk.save(productEntity) }
        verify(exactly = 1) { productMapperMockk.toDto(savedProductEntity) }

        assertEquals(savedProductDTO, result)
    }

    @Test
    fun `update product that does not exist should throw`() {
        // Given
        val productId = 1L
        coEvery { productCrudRepositoryMockk.findById(productId) } returns null

        // When
        assertFailsWith<ProductNotFoundException> {
            runBlocking { productService.updateProduct(productId, mockk<ProductDTO>()) }
        }

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findById(productId) }
        coVerify(exactly = 0) { productCrudRepositoryMockk.save(any()) }
        verify { productMapperMockk wasNot called }
    }

    @Test
    fun `should delete product`() {
        // Given
        val productId = 1L
        coEvery { productCrudRepositoryMockk.findById(productId) } returns mockk<Product>()
        coEvery { productCrudRepositoryMockk.deleteById(productId) } just runs

        // When
        runBlocking { productService.deleteProduct(1L) }

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findById(productId) }
        coVerify(exactly = 1) { productCrudRepositoryMockk.deleteById(productId) }
    }

    @Test
    fun `delete product that does not exist should throw`() {
        // Given
        val productId = 1L
        coEvery { productCrudRepositoryMockk.findById(productId) } returns null

        // When
        assertFailsWith<ProductNotFoundException> {
            runBlocking { productService.deleteProduct(1L) }
        }

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findById(productId) }
        coVerify(exactly = 0) { productCrudRepositoryMockk.deleteById(productId) }
    }
}
