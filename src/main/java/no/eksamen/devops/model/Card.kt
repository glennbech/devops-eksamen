package no.eksamen.devops.model

import no.eksamen.devops.cards.dto.CardDto
import no.eksamen.devops.cards.dto.Rarity


data class Card(
        val cardId : String,
        val rarity: Rarity
){

    constructor(dto: CardDto): this(
            dto.cardId ?: throw IllegalArgumentException("Null cardId"),
            dto.rarity ?: throw IllegalArgumentException("Null rarity"))
}