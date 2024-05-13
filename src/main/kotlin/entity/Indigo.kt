package entity

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * class modelling the game state of an Indigo game, acts like an element in a doubly linked list
 *
 * @constructor creates an instance of Indigo with the given parameters
 *
 * @param settings [players], initial [] and isRandom [Boolean] packaged into a wrapping class
 * @param gameBoard current state of the [GameBoard]
 * @param allTiles [List] of treasure and route tiles
 * @param gems [MutableList] of [Gem]s currently still in play
 * @param tokens [MutableList] of [Token]s in the game
 *
 * @property players [List] of [Player] entities involved in the game
 * @property currentPlayerIndex used to determine which [Player] is up next
 * @property middleTile representing the [MiddleTile] in the center of the board
 * @property treasureTiles [List] of Treasure [Tile]s on the board
 * @property routeTiles [MutableList] of Route [Tile]s currently still in play
 * @property previousGameState saves the previous state of the game for undo action, initially null
 * @property nextGameState saves the next state of the game for redo action, initially null
 *
 */
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id",
    scope = Indigo::class
)
@JsonSerialize
data class Indigo(
    val settings: GameSettings,
    val gameBoard: GameBoard,
    val allTiles: List<Tile>,
    var gems: MutableList<Gem>,
    var tokens: MutableList<Token>,
    val id: Int = IDGenerator.generateID()
    //val id: UUID = UUID.randomUUID()
) {
    var players = settings.players
    var currentPlayerIndex = 0
    val middleTile = MiddleTile()
    val treasureTiles: List<Tile> = allTiles.take(6)
    var routeTiles: MutableList<Tile> = allTiles.drop(6).toMutableList()

    //@JsonIgnore
    @JsonIdentityReference(alwaysAsId = true)
    var previousGameState: Indigo? = null

    //@JsonIgnore
    @JsonIdentityReference(alwaysAsId = true)
    var nextGameState: Indigo? = null

    init {
        currentPlayerIndex = if (settings.isRandom) (0 until settings.players.size).random()
        else settings.playerIndex
    }

    /**
     *  the extension function [copyTo] is a function wich create a deep copy of Indigo
     *  with the necessary data
     *
     *  @return Returning a new [Indigo] which are independent of the current game
     */
    fun copyTo(): Indigo {
        val copiedGems = mutableListOf<Gem>()
        for (gem in gems) {
            copiedGems.add(gem)
        }
        val copiedGameBoardTiles = mutableMapOf<Coordinate, Tile>()
        for ((key, value) in this.gameBoard.gameBoardTiles) {
            val gemEndPosition = value.gemEndPosition.toMutableMap()
            val egdes = value.edges.toMutableList()
            val paths = value.paths.toMutableList()
            val copiedTile = Tile(paths, value.type, gemEndPosition).apply {
                this.edges.clear()
                this.edges.addAll(egdes)
            }
            copiedGameBoardTiles[key] = copiedTile
        }
        val copiedGateTokens = this.gameBoard.gateTokens.toList()
        val copiedGameBoard = GameBoard()
        copiedGameBoard.gameBoardTiles.clear()
        copiedGameBoard.gameBoardTiles.putAll(copiedGameBoardTiles)
        copiedGameBoard.gateTokens = copiedGateTokens
        val copiedPlayers = settings.players.map { originalPlayer ->
            when (originalPlayer) {
                is CPUPlayer -> {
                    CPUPlayer(
                        originalPlayer.name,
                        originalPlayer.age,
                        originalPlayer.color,
                        originalPlayer.difficulty,
                        originalPlayer.simulationSpeed
                    ).apply {
                        score = originalPlayer.score
                        collectedGems = originalPlayer.collectedGems.toMutableList()
                        // Copy the handTile
                        handTile = originalPlayer.handTile?.copy()
                        // Additional properties specific to CPUPlayer
                        // ...
                    }
                }

                else -> {
                    Player(
                        originalPlayer.name,
                        originalPlayer.age,
                        originalPlayer.color,
                        originalPlayer.isAI
                    ).apply {
                        score = originalPlayer.score
                        collectedGems = originalPlayer.collectedGems.toMutableList()
                        // Copy the handTile
                        handTile = originalPlayer.handTile?.copy()
                    }
                }
            }
        }.toList()
        val copiedSettings = GameSettings(copiedPlayers)
        val copiedIndigo = Indigo(
            copiedSettings,
            copiedGameBoard,
            this.allTiles,
            copiedGems,
            this.tokens,
            //id = id+1
            id = IDGenerator.generateID()
        )
        copiedIndigo.currentPlayerIndex = this.currentPlayerIndex
        copiedIndigo.nextGameState = this.nextGameState
        copiedIndigo.previousGameState = this.previousGameState
        copiedIndigo.middleTile.gemPosition.clear()
        for ((key, value) in this.middleTile.gemPosition) {
            copiedIndigo.middleTile.gemPosition[key] = value
        }
        copiedIndigo.routeTiles = this.routeTiles.toMutableList()
        return copiedIndigo
    }

    //einfacher alternative für copyTo methode
    fun copy(): Indigo {
        val mapper = jacksonObjectMapper()
        val jsonString = mapper.writeValueAsString(this)
        return mapper.readValue<Indigo>(jsonString)
    }

}

/**
 * The `IDGenerator` object provides functionality for generating unique integer IDs.
 */
object IDGenerator {
    private var idCounter = 0
    fun generateID(): Int {
        return ++idCounter
    }
}
