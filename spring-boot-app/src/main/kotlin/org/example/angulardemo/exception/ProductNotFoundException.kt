package org.example.angulardemo.exception

class ProductNotFoundException(productId: Long) : RuntimeException("Product with id: $productId not found") {

}
