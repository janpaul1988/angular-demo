package org.example.angulardemo.controller

import org.example.angulardemo.dto.UserDTO
import org.example.angulardemo.service.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/users")
class UserController(
    val userService: UserService,
) {

    @GetMapping()
    suspend fun getUserByEmail(
        @RequestParam email: String,
    ): UserDTO = userService.getUserByEmail(email)

}
