package org.example.angulardemo.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table


@Table(name = "product")
data class Product(
    @Id
    val id: String?,
    val userId: Long,
    var externalId: Long?,
    var name: String,
    var description: String?,
)
