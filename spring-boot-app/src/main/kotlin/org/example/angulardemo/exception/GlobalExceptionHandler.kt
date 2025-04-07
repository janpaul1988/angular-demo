package org.example.angulardemo.exception

import org.example.angulardemo.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFoundException(ex: ProductNotFoundException): ResponseEntity<String> {
        logger.error { ex.message }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleIllegalArgumentException(ex: WebExchangeBindException): ResponseEntity<String> {
        val errors = ex.bindingResult.allErrors
            .map { error -> error.defaultMessage!! }
            .sorted()

        logger.error(ex) { "Method argument not valid observed: $errors" }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors.joinToString(", "))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<String> {
        logger.error(ex) { "${ex.message}" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An unexpected internal server error occurred. Please contact the system administrator.")
    }
}
