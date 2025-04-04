package org.example.angulardemo.service

import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.example.angulardemo.mapper.ProductMapper
import org.example.angulardemo.repository.ProductCrudRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductServiceUnitTest {

    private val productCrudRepositoryMockk: ProductCrudRepository = mockk(relaxed = true)
    private val productMapperMockk: ProductMapper = mockk(relaxed = true)
    private val productService = ProductService(productCrudRepositoryMockk, productMapperMockk)

    @Test
    fun `should add new product`() {
        // Given
        val productDTO = mockk<ProductDTO>()
        val product = mockk<Product>()
        val savedProduct = mockk<Product>()
        val savedProductDTO = mockk<ProductDTO>()

        // Mock behavior
        every { productMapperMockk.toEntity(productDTO) } returns product
        coEvery { productCrudRepositoryMockk.save(product) } returns savedProduct
        every { productMapperMockk.toDto(savedProduct) } returns savedProductDTO

        // When
        val result = runBlocking { productService.addProduct(productDTO) }

        // Then
        coVerify(exactly = 1) { productMapperMockk.toEntity(productDTO) }
        coVerify(exactly = 1) { productCrudRepositoryMockk.save(product) }
        coVerify(exactly = 1) { productMapperMockk.toDto(savedProduct) }
        assertEquals(savedProductDTO, result)
    }

    @Test
    fun `should get all products`() = runBlocking {
        // Given
        val productList = flowOf(mockk<Product>(), mockk<Product>())
        val productDTOList = listOf(mockk<ProductDTO>(), mockk<ProductDTO>())

        every { productCrudRepositoryMockk.findAll() } returns productList

        // Pair products with their corresponding DTOs and mock the mapper behavior
        productList.toList().zip(productDTOList).forEach { (product, productDTO) ->
            every { productMapperMockk.toDto(product) } returns productDTO
        }

        // When
        val result = productService.getAllProducts().toList()

        // Then
        coVerify(exactly = 1) { productCrudRepositoryMockk.findAll() }
        verify(exactly = productList.toList().size) { productMapperMockk.toDto(any()) }

        assertEquals(productDTOList, result)
    }


}
