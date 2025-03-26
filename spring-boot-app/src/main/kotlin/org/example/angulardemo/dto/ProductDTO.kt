package org.example.angulardemo.dto

import jakarta.validation.constraints.NotBlank
import org.example.angulardemo.entity.Product

data class ProductDTO(
    val id: Long? = null,
    @field: NotBlank(message = "Product name cannot be blank.")
    var name: String,
    var description: String? = null,
) {

    fun toProduct(): Product {
        return Product(
            id = this.id,
            name = this.name,
            description = this.description
        )
    }

}

