package entity

/**
 * Represents the settings for a game, including the list of players, the current player index,
 * and whether the player index should be initialized randomly.
 *
 *
 * @param players [List] of [Player] entities involved in the game
 * @param playerIndex [Int] serving as an indicator of whose turn it is, defaults to 0
 * @param isRandom [Boolean] which, when true overrides the [Indigo.currentPlayerIndex] with a random value on init,
 * defaults to false
 */
class GameSettings(
    var players: List<Player>,
    val playerIndex: Int = 0,
    val isRandom: Boolean = false
)
