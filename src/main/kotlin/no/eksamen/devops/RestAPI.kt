package no.eksamen.devops

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import no.eksamen.devops.dto.UserDto
import no.eksamen.devops.db.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.*

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
    @Timed("Get User information", longTask = true)
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


    @ApiOperation("Create a new user, with the given id")
    @PostMapping(path = ["/{userId}"])
    @Timed("Time to create custom User")
    fun createUser(
            @PathVariable("userId") userId: String
    ): ResponseEntity<Void> {


        if(userId == "dummy"){
            logger.info("created dummyData")
            userService.createDummyUsers()
        }
        val ok = userService.registerNewUser(userId)

        return if (!ok) {

            logger.warn("error while created a new user")
            return ResponseEntity.status(400).build()
        } else {

            Thread.sleep(5000)
            logger.info("Successfully created regular user: $userId")
            ResponseEntity.status(201).build()
        }
    }

    @ApiOperation("Create a new custom user, with specific values")
    @PostMapping(path = ["/{userId}/{role}/{coins}/{cardpack}"])
    fun customCustomUser(
            @PathVariable("userId") userId: String,
            @PathVariable("role") role: String,
            @PathVariable("coins") coins: Double,
            @PathVariable("cardpack") cardPack: Int
    ): ResponseEntity<Void> {

        val ok = userService.registerNewCustomUser(userId, role, coins, cardPack)

        return if (!ok) {

//            val counter = meterRegistry.counter("user coins","Coins",coins.toString()).increment()
            logger.error("error while created a new custom user")
            return ResponseEntity.status(400).build()
        } else {

            if (role == "user" && coins > 500) {
                logger.warn(" this user starts with to much coins!! $coins")
            } else if (role == "admin") {
                logger.info(" Admin has been created $userId")
            }
            val counter: Counter = meterRegistry.counter("my.counter")
            val timer: Timer = meterRegistry.timer("my.timer")

            counter.increment(coins)
            timer.record(Duration.ofMillis(500))

            logger.info("Successfully creating user: $userId,role: $role, $coins Coins, $cardPack card packs")
            ResponseEntity.status(201).build()
        }
    }
}