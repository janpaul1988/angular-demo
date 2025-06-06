package org.example.angulardemo.configuration

import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "product")
@Validated
data class ProductConfiguration(
    @field: Min(1, message = "product property maxInserts must be defined as greater than or equal to 1")
    var maxInserts: Int = 10,
    @field: Min(0L, message = "product property externalIdStartingValue must be defined as greater than or equal to 0")
    var externalIdStartingValue: Long = 0L,
    @field: Min(1L, message = "product property externalIdIncrementValue must be defined as greater than or equal to 1")
    var externalIdIncrementValue: Long = 1L,
)
