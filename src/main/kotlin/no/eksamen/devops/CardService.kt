package no.eksamen.devops

import no.eksamen.devops.model.Card
import no.eksamen.devops.cards.dto.Rarity
import no.eksamen.devops.model.Collection

import kotlin.random.Random
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import org.springframework.stereotype.Service


@Service
open class CardService {

    companion object{
        private val log = LoggerFactory.getLogger(CardService::class.java)
    }

    protected var collection: Collection? = null

    val cardCollection : List<Card>
        get() = collection?.cards ?: listOf()

    private val lock = Any()

    @PostConstruct
    fun init(){

        synchronized(lock){
            if(cardCollection.isNotEmpty()){
                return
            }
            fetchData()
        }
    }

    fun isInitialized() = cardCollection.isNotEmpty()

    protected open fun fetchData(){
        //TODO
    }

    private fun verifyCollection(){

        if(collection == null){
            fetchData()

            if(collection == null){
                throw IllegalStateException("No collection info")
            }
        }
    }

    fun millValue(cardId: String) : Int {
        verifyCollection()
        val card : Card = cardCollection.find { it.cardId  == cardId} ?:
        throw IllegalArgumentException("Invalid cardId $cardId")

        return collection!!.millValues[card.rarity]!!
    }

    fun price(cardId: String) : Int {
        verifyCollection()
        val card : Card = cardCollection.find { it.cardId  == cardId} ?:
        throw IllegalArgumentException("Invalid cardId $cardId")

        return collection!!.prices[card.rarity]!!
    }

    fun getRandomSelection(n: Int) : List<Card>{

        if(n <= 0){
            throw IllegalArgumentException("Non-positive n: $n")
        }

        verifyCollection()

        val selection = mutableListOf<Card>()

        val probabilities = collection!!.rarityProbabilities
        val bronze = probabilities[Rarity.BRONZE]!!
        val silver = probabilities[Rarity.SILVER]!!
        val gold = probabilities[Rarity.GOLD]!!
        //val pink = probabilities[Rarity.PINK_DIAMOND]!!

        repeat(n) {
            val p = Math.random()
            val r = when{
                p <= bronze -> Rarity.BRONZE
                p > bronze && p <= bronze + silver -> Rarity.SILVER
                p > bronze + silver && p <= bronze + silver + gold -> Rarity.GOLD
                p > bronze + silver + gold -> Rarity.PINK_DIAMOND
                else -> throw IllegalStateException("BUG for p=$p")
            }
            val card = collection!!.cardsByRarity[r].let{ it!![Random.nextInt(it.size)] }
            selection.add(card)
        }

        return selection
    }
}