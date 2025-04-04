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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun addProduct(@RequestBody @Valid productDTO: ProductDTO): ProductDTO =
        productService.addProduct(productDTO)

    @GetMapping
    suspend fun getAllProducts(): Flow<ProductDTO> = productService.getAllProducts()

    @PutMapping("/{id}")
    suspend fun updateProduct(@PathVariable("id") productId: Long, @RequestBody productDTO: ProductDTO): ProductDTO =
        productService.updateProduct(productId, productDTO)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteCourse(@PathVariable("id") productId: Long) = productService.deleteProduct(productId)

}
