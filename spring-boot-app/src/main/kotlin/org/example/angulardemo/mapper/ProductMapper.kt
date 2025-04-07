package org.example.angulardemo.mapper

import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    fun toEntity(product: ProductDTO): Product {
        return Product(
            id = product.id,
            name = product.name,
            description = product.description
        )
    }

    fun toDto(product: Product): ProductDTO {
        return ProductDTO(
            id = product.id,
            name = product.name,
            description = product.description
        )
    }

}
