package org.example.angulardemo.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table


@Table(name = "product")
data class Product(
    @Id
    val id: Long? = null,
    var name: String,
    var description: String? = null,
)
