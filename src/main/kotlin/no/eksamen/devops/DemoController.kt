package no.eksamen.devops

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class DemoController {
    @GetMapping(path = ["/ping"])
    fun ping(): String {
        return "ping"
    }

    @GetMapping(path = ["/pong"])
    fun pong(): String {
        return "pong"
    }

    @GetMapping(path = ["/hello"])
    fun hello(): String {
        return "Hello World! :D"
    }

    @GetMapping(path = ["/helloworld"])
    fun helloworld(): String {
        return "Hello World!! this is me trying to learn devOps "
    }
}