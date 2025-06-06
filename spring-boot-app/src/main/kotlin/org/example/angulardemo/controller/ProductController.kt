package org.example.angulardemo.controller

import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping("/products")
class ProductController(
    val productService: ProductService,
) {

    @PostMapping("{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun addProduct(
        @PathVariable("userId") userId: Long,
        @RequestBody @Valid productDTO: ProductDTO,
    ): ProductDTO =
        productService.addProduct(userId, productDTO)

    @GetMapping("/{userId}")
    suspend fun getAllProducts(
        @PathVariable("userId") userId: Long,
    ): Flow<ProductDTO> = productService.getAllProductsForUser(userId)

    @PutMapping("/{userId}/{id}")
    suspend fun updateProduct(
        @PathVariable("userId") userId: Long,
        @PathVariable("id") productId: Long,
        @RequestBody @Valid productDTO: ProductDTO,
    ): ProductDTO =
        productService.updateProduct(userId, productId, productDTO)

    @DeleteMapping("/{userId}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteCourse(
        @PathVariable("userId") userId: Long,
        @PathVariable("id") productId: Long,
    ) = productService.deleteProduct(userId, productId)

}
