package org.example.angulardemo.entity

import jakarta.persistence.*
import org.example.angulardemo.dto.ProductDTO

@Entity
@Table(name = "product")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var extId: String,
    @Column(nullable = false)
    var name: String,
    var description: String? = null,
) {

    fun toProductDTO(): ProductDTO {
        return ProductDTO(
            id = this.id,
            extId = this.extId,
            name = this.name,
            description = this.description
        )
    }
}
