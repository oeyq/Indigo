package entity

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

/**
 * Represents a player in the Indigo game.
 *
 * @property name The name of the player.
 * @property age The age of the player, default is Date(0).
 * @property color The color assigned to the player.
 * @property isAI Indicates whether the player is controlled by AI, default is false.
 * @property score The score of the player.
 * @property collectedGems A list of gems collected by the player.
 * @property handTile The tile currently held by the player.
 */
open class Player(val name: String, val age: Date = Date(0), var color: TokenColor,
                  @JsonProperty("ai")
                  val isAI: Boolean = false) {
    var score = 0
    var collectedGems = mutableListOf<Gem>()
    var handTile: Tile? = null
}