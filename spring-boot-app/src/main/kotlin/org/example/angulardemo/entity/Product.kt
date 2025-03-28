package org.example.angulardemo.entity

import org.example.angulardemo.dto.ProductDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table


@Table(name = "product")
data class Product(
    @Id
    val id: Long? = null,
    var name: String,
    var description: String? = null,
) {

    fun toProductDTO(): ProductDTO {
        return ProductDTO(
            id = this.id,
            name = this.name,
            description = this.description
        )
    }
}
