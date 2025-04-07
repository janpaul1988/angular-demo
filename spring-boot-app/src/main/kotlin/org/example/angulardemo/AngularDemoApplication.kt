package org.example.angulardemo

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

val logger = KotlinLogging.logger {}

@SpringBootApplication
class AngularDemoApplication

fun main(args: Array<String>) {
    runApplication<AngularDemoApplication>(*args)
}
