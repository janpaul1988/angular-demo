package org.example.angulardemo.dto

import jakarta.validation.constraints.NotBlank

data class ProductDTO(
    val id: Long? = null,
    @get: NotBlank(message = "Product name cannot be blank.")
    var name: String,
    var description: String? = null,
) {


}

