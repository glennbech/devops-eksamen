package no.eksamen.devops

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.eksamen.devops.db.UserRepository
import no.eksamen.devops.db.UserService
import no.eksamen.devops.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*

@Api(value = "/api/user-collections", description = "Operations on card collections owned by users")
@RequestMapping(
        path = ["/api/user-collections"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)]
)
@RestController
class RestAPI(

        private val userService: UserService,

        private  var userRepository: UserRepository


) {
    @Autowired
    private lateinit var meterRegistry: MeterRegistry


    private val logger = LoggerFactory.getLogger(this::class.java)


    @ApiOperation("Retrieve card collection information for a specific user")
    @GetMapping(path = ["/{userId}"])
    @Timed("my.timer Get User information")
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
    fun createUser(
            @PathVariable("userId") userId: String
    ): ResponseEntity<Void> {

        val ok = userService.registerNewUser(userId)

        return if (!ok) {

            logger.warn("error while created a new user")
            return ResponseEntity.status(400).build()
        } else {
            if (userId == "dummy") {
                // create a dummy base with simulation on metric counter
                logger.info("created dummyData")
                userService.createDummyUsers()
            }
            logger.info("Successfully created regular user: $userId")
            ResponseEntity.status(201).build()
        }
    }


    @Timed("my.timer longTask create user", longTask = true)
    @Scheduled(fixedDelay = 60000L) // every 60 seconds
    fun deleteEntireUserDataBase() {

        //THIS IS NOT HOW TO DO IT !!!
        val userId = "this_UserId_Is_To_Check_If_The_Data_Base_Is_Deleted_Or_Not_42_42_42"
        userService.registerNewUser(userId)

         userRepository.deleteAll()

        // there are other ways and better to check of the userRepository is Deleted or not
        // if the database was not deleted  the user will exist and cast a warning
        return if (userRepository.existsById(userId)) {

            logger.warn("error while created Scheduled_User")
        } else {
            logger.info("Successfully Deleted the database")
        }
    }






    @ApiOperation("Create a new custom user, with specific values")
    @PostMapping(path = ["/{userId}/{role}/{coins}/{cardpack}"])
    @Timed("my.timer create custom User")
    fun customCustomUser(
            @PathVariable("userId") userId: String,
            @PathVariable("role") role: String,
            @PathVariable("coins") coins: Double,
            @PathVariable("cardpack") cardPack: Int
    ): ResponseEntity<Void> {

        val ok = userService.registerNewCustomUser(userId, role, coins, cardPack)

        return if (!ok) {

            //"faking error""
            logger.error("error while created a new custom user")
            return ResponseEntity.status(400).build()
        } else {

            if (role == "user" && coins > 500) {
                logger.warn(" this user starts with to much coins!! $coins")
            } else if (role == "admin") {
                logger.info(" Admin has been created $userId")
            }
            val counter: Counter = meterRegistry.counter("my.counter")

            counter.increment(coins)

            logger.info("Successfully creating user: $userId,role: $role, $coins Coins, $cardPack card packs")
            ResponseEntity.status(201).build()
        }
    }
}