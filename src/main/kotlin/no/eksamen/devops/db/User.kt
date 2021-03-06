package no.eksamen.devops.db

import javax.persistence.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name="user_data")
class User(

    @get:Id
    @get:NotBlank
    var userId: String? = null,

    @get:NotNull
    @get:NotBlank
    var role: String? = null,

    @get:Min(0)
    var coins: Double = 0.00,

    @get:Min(0)
    var cardPacks: Int = 0,

    @get:OneToMany(mappedBy = "user", cascade = [(CascadeType.ALL)])
    var ownedCards : MutableList<CardCopy> = mutableListOf()
)