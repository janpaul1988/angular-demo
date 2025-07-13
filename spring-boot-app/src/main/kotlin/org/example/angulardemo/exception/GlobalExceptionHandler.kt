package org.example.angulardemo.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(
        JobNotFoundException::class, UserNotFoundException::class,
        JournalNotFoundException::class, JournalTemplateNotFoundException::class
    )
    fun handleEntityNotFoundException(ex: RuntimeException): ResponseEntity<String> {
        logger.error { ex.message }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
    }

    @ExceptionHandler(JobConstraintViolationException::class, JobStartDateViolationException::class)
    fun handleDataConstraintException(ex: RuntimeException): ResponseEntity<String> {
        logger.error { ex.message }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
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
