package no.eksamen.devops

import no.eksamen.devops.db.CardCopy
import no.eksamen.devops.db.User
import no.eksamen.devops.dto.CardCopyDto
import no.eksamen.devops.dto.UserDto

object DtoConverter {

    fun transform(user: User) : UserDto {

        return UserDto().apply {
            userId = user.userId
            role = user.role
            coins = user.coins
            cardPacks = user.cardPacks
            ownedCards = user.ownedCards.map { transform(it) }.toMutableList()
        }
    }

    fun transform(cardCopy: CardCopy) : CardCopyDto {
        return CardCopyDto().apply {
            cardId = cardCopy.cardId
            numberOfCopies = cardCopy.numberOfCopies
        }
    }
}