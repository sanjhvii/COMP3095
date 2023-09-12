package ca.gbc.microservicesparent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MicroservicesParentApplication

fun main(args: Array<String>) {
    runApplication<MicroservicesParentApplication>(*args)
}
