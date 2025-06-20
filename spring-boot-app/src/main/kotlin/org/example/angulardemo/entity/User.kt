package org.example.angulardemo.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table


@Table(name = "user")
data class User(
    @Id
    val id: Long? = null,
    var email: String? = null,
)

