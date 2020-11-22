package no.eksamen.devops

import io.micrometer.core.annotation.Timed
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

@Api(value = "/api/user-collections", description = "Operations on card collections owned by users")
@RequestMapping(
        path = ["/api/user-collections"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)]
)
@RestController
class RestAPI(

        private val userService: UserService
) {

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ApiOperation("Retrieve card collection information for a specific user")
    @GetMapping(path = ["/{userId}"])
    fun getUserInfo(
            @PathVariable("userId") userId: String
    ): ResponseEntity<UserDto> {

        logger.info("Get $userId's information. ")

        val user = userService.findByIdEager(userId)

        if (user == null) {
            logger.warn("Error: tried to get $userId information ")
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.status(200).body(DtoConverter.transform(user))
    }

    @Timed("CreateUserTimer")
    @ApiOperation("Create a new user, with the given id")
    @PostMapping(path = ["/{userId}"])
    fun createUser(
            @PathVariable("userId") userId: String
    ): ResponseEntity<Void> {


        val ok = userService.registerNewUser(userId)

        return if (!ok) {

            logger.warn("error while created a new user")
            return ResponseEntity.status(400).build()
        } else {
            logger.info("Successfully created regular user: $userId")
            ResponseEntity.status(201).build()
        }
    }

    @ApiOperation("Create a new custom user, with specific values")
    @PostMapping(path = ["/{userId}/{role}/{coins}/{cardpack}"])
    fun customCustomUser(
            @PathVariable("userId")userId: String,
            @PathVariable( "role")role: String,
            @PathVariable("coins")coins: Int,
            @PathVariable("cardpack")cardPack: Int
    ): ResponseEntity<Void> {

        val ok = userService.registerNewCustomUser(userId, role, coins, cardPack)

        return if (!ok) {

            logger.error("error while created a new custom user")
            return ResponseEntity.status(400).build()
        } else {
            if(role =="user" && coins > 500){
                logger.warn(" this user starts with to much coins!! $coins")
            }else if(role =="admin"){
                logger.info(" Admin has been created $userId")
            }

            logger.info("Successfully creating user: $userId his role is: $role, and have $coins Coins, and start with: $cardPack card packs")
            ResponseEntity.status(201).build()
        }
    }
}