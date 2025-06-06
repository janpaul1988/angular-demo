package org.example.angulardemo.exception

class ProductNotFoundException(userId: Long, productId: Long) :
    RuntimeException("Product with id: $productId not found for user with id: $userId")
