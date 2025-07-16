package org.example.jobjournaler.controller

import org.example.jobjournaler.dto.UserDTO
import org.example.jobjournaler.service.UserService
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/users")
class UserController(
    val userService: UserService,
) {

    @GetMapping()
    suspend fun getUserByEmail(
        @RequestHeader("X-Forwarded-Email") email: String,
    ): UserDTO = userService.getUserByEmail(email)

    @GetMapping("/headers")
    fun example(request: ServerHttpRequest): String {
        request.headers.forEach { name, values ->
            values.forEach { value ->
                println("Header: $name = $value")
            }
        }
        return "ok"
    }

}
