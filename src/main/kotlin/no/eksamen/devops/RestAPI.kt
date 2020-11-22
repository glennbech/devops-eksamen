package no.eksamen.devops

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import no.eksamen.devops.dto.UserDto
import no.eksamen.devops.db.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import java.util.List;
import java.util.logging.Logger;

@Api(value = "/api/user-collections", description = "Operations on card collections owned by users")
@RequestMapping(
        path = ["/api/user-collections"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)]
)
@RestController
class RestAPI(

        private val userService: UserService
) {

//    @Autowired
//    private lateinit var registry: MeterRegistry

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ApiOperation("Retrieve card collection information for a specific user")
    @GetMapping(path = ["/{userId}"])
    fun getUserInfo(
            @PathVariable("userId") userId: String
    ) : ResponseEntity<UserDto>{

        logger.info("Get user information. path = /userId")

        val user = userService.findByIdEager(userId)

        if(user == null){
            logger.warn("user equals null")
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.status(200).body(DtoConverter.transform(user))
    }

    @ApiOperation("Create a new user, with the given id")
    @PutMapping(path = ["/{userId}"])
    fun createUser(
            @PathVariable("userId") userId: String
    ): ResponseEntity<Void>{

        logger.info("Create user. path = /userId")

        val ok = userService.registerNewUser(userId)

        return if(!ok){

            logger.warn("error while created a new user")
            return ResponseEntity.status(400).build()
        }
            else ResponseEntity.status(201).build()
    }

    @ApiOperation("Create a new user, with the given id")
    @PutMapping(path = ["/user"])

    fun createUser2(userId: String): ResponseEntity<Void>{

        logger.info("Create user. path = /userId")

        val ok = userService.registerNewUser(userId)

        return if(!ok){

            logger.warn("error while created a new user")
            return ResponseEntity.status(400).build()
        }
            else ResponseEntity.status(201).build()
    }
}