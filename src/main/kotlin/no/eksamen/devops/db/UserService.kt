package no.eksamen.devops.db

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import no.eksamen.devops.CardService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import javax.persistence.LockModeType
import kotlin.concurrent.thread


@Repository
interface UserRepository : CrudRepository<User, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :id")
    fun lockedFind(@Param("id") userId: String): User?

}


@Service
@Transactional
class UserService(
        private val userRepository: UserRepository,
        private val cardService: CardService
) {

    companion object {
        const val CARDS_PER_PACK = 5
    }

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    private val logger = LoggerFactory.getLogger(this::class.java)


    fun findByIdEager(userId: String): User? {

        val user = userRepository.findById(userId).orElse(null)
        if (user != null) {
            user.ownedCards.size
        }
        return user
    }


    fun registerNewUser(userId: String): Boolean {

        if (userRepository.existsById(userId)) {
            return false
        }


        val user = User()
        user.userId = userId
        user.role = "user"
        user.cardPacks = 3
        user.coins = 100.0
        userRepository.save(user)
        return true
    }

    fun registerNewCustomUser(userId: String, role: String, coins: Double, cardPack: Int): Boolean {

        if (userRepository.existsById(userId)) {
            return false
        }
        if (role == "user" && coins > 500) {

            logger.warn("Role:user:$userId was created with:$coins need to validate creation")
        }

        val user = User()
        user.userId = userId
        user.role = role
        user.coins = coins
        user.cardPacks = cardPack
        userRepository.save(user)
        return true
    }

    fun createDummyUsers(){

        for (x in 1 .. 15){

             Thread.sleep(1200)
            val user = User()
            user.userId = "devops$x"
            user.role = "user"
            user.coins = x.toDouble()*100
            user.cardPacks = 0 + x

            logger.info("Successfully created dummyData $x")

            val counter: Counter = meterRegistry.counter("my.counter")
            counter.increment(user.coins)

            userRepository.save(user)
        }
        simulateActiveUser()
    }

    // this function will take 30 seconds after createDummy's 18 seconds
    fun simulateActiveUser(){

        val user = User()
        user.userId = "testDummy"
        user.role = "user"
        user.coins = 1337.42
        user.cardPacks = 5
        logger.info("Simulate Active User")

        for (x in 1..30){

            Thread.sleep(1000)
            val randomDouble = (-1000..2000).random()

            user.coins =+ randomDouble.toDouble()
            val counter: Counter = meterRegistry.counter("my.counter")
            counter.increment(user.coins)
            logger.info("Simulate coins: ${user.coins}")
        }
    }

    private fun validateCard(cardId: String) {
        if (!cardService.isInitialized()) {
            throw IllegalStateException("Card service is not initialized")
        }

        if (!cardService.cardCollection.any { it.cardId == cardId }) {
            throw IllegalArgumentException("Invalid cardId: $cardId")
        }
    }

    private fun validateUser(userId: String) {
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("User $userId does not exist")
        }
    }

    private fun validate(userId: String, cardId: String) {
        validateUser(userId)
        validateCard(cardId)
    }

    fun buyCard(userId: String, cardId: String) {
        validate(userId, cardId)

        val price = cardService.price(cardId)
        val user = userRepository.lockedFind(userId)!!

        if (user.coins < price) {
            throw IllegalArgumentException("Not enough coins")
        }

        user.coins -= price

        addCard(user, cardId)
    }

    private fun addCard(user: User, cardId: String) {
        user.ownedCards.find { it.cardId == cardId }
                ?.apply { numberOfCopies++ }
                ?: CardCopy().apply {
                    this.cardId = cardId
                    this.user = user
                    this.numberOfCopies = 1
                }.also { user.ownedCards.add(it) }
    }

    fun millCard(userId: String, cardId: String) {
        validate(userId, cardId)

        val user = userRepository.lockedFind(userId)!!

        val copy = user.ownedCards.find { it.cardId == cardId }
        if (copy == null || copy.numberOfCopies == 0) {
            throw IllegalArgumentException("User $userId does not own a copy of $cardId")
        }

        copy.numberOfCopies--

        val millValue = cardService.millValue(cardId)
        user.coins += millValue
    }

    fun openPack(userId: String): List<String> {

        validateUser(userId)

        val user = userRepository.lockedFind(userId)!!

        if (user.cardPacks < 1) {
            throw IllegalArgumentException("No pack to open")
        }

        user.cardPacks--

        val selection = cardService.getRandomSelection(CARDS_PER_PACK)

        val ids = mutableListOf<String>()

        selection.forEach {
            addCard(user, it.cardId)
            ids.add(it.cardId)
        }

        return ids
    }
}