package org.example.angulardemo.controller

import jakarta.validation.Valid
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
    fun addProduct(@RequestBody @Valid productDTO: ProductDTO): ProductDTO = productService.addCourse(productDTO)

    @GetMapping
    fun getAllProducts(): List<ProductDTO> = productService.getAllProducts()

    @PutMapping("/{id}")
    fun updateProduct(@PathVariable("id") productId: Long, @RequestBody productDTO: ProductDTO): ProductDTO =
        productService.updateProduct(productId, productDTO)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCourse(@PathVariable("id") productId: Long) = productService.deleteProduct(productId)

}
