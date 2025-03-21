package org.example.angulardemo.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Component
@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {

        val errors = ex.bindingResult.allErrors
            .map { error -> error.defaultMessage!! }
            .sorted()

        logger.error("Method argument not valid observed: $errors", ex)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors.joinToString(", "))
    }

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleInstructorNotValidException(ex: ProductNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.message)
    }
}
