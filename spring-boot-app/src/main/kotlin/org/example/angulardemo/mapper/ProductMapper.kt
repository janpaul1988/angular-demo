package org.example.angulardemo.mapper

import org.example.angulardemo.dto.ProductDTO
import org.example.angulardemo.entity.Product
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    fun toEntity(productDto: ProductDTO): Product {
        return Product(
            id = null,
            userId = productDto.userId,
            externalId = productDto.id,
            name = productDto.name,
            description = productDto.description
        )
    }

    fun toDto(product: Product): ProductDTO {
        return ProductDTO(
            id = product.externalId,
            userId = product.userId,
            name = product.name,
            description = product.description
        )
    }

}
