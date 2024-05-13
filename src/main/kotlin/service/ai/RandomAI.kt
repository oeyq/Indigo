package service.ai

import entity.Coordinate
import entity.Tile
import service.RootService
import service.*

/**
 * The class [RandomAI] provides functionality for the AI player to play a legal move at random
 *
 * @property rootService The [RootService] this instance belongs to
 */
class RandomAI(val rootService: RootService): AbstractRefreshingService() {

    /**
     * Makes a random legal move for the AI player.
     */
    fun makeMove() {

        // Get the current game state
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        // Get the available moves for the current player
        val availableMoves = findAvailableMoves()

        // If there are available moves, make a random move
        if (availableMoves.isNotEmpty()) {
            val randomMove = availableMoves.random()
            val coordinate = randomMove.first
            val tile = randomMove.second
            val rotation =randomMove.third
            println("[RandomAI DEBUG] coordinate: $coordinate tile: $tile rotation: $rotation")
            //rotate if necessary
            for (i in 0 until rotation){
                rootService.playerTurnService.rotateTileRight(tile)
            }
            // Place the tile at the random coordinate
            rootService.playerTurnService.placeRouteTile(coordinate, tile)
        }
    }

    /**
     * Finds available moves on the game board.
     *
     * @return [List] of [Triple]s containing the [Coordinate], Player [Tile] and rotation [Int]
     * for all available legal moves
     */
    fun findAvailableMoves(): List<Triple<Coordinate,Tile,Int>> {
        val availableMoves = mutableListOf<Triple<Coordinate,Tile,Int>>()
        val game = rootService.currentGame
        checkNotNull(game)
        var rotationFoundFlag : Boolean
        // Iterate over the game board and find available moves
        for (q in -4..4) {
            for (r in Integer.max(-4, -q - 4)..Integer.min(4, -q + 4)) {
                val coordinate = Coordinate(q, r)
                val playerTile = game.players[game.currentPlayerIndex].handTile ?: continue

                // Check if placing the tile at the coordinate is a valid move
                if (rootService.gameService.checkPlacement(coordinate, playerTile,true)) {
                    availableMoves.add(Triple(coordinate, playerTile,0))
                // if move is not valid but space on board is empty, find valid rotation for space
                }else if (game.gameBoard.gameBoardTiles[coordinate] == null){
                    rotationFoundFlag = false
                    for (i in 1..6){
                        rootService.playerTurnService.rotateTileRight(playerTile,true)
                        if (rootService.gameService.checkPlacement(coordinate,playerTile,true) &&
                            !rotationFoundFlag){
                            rotationFoundFlag = true
                            availableMoves.add(Triple(coordinate,playerTile,i))
                        }
                    }
                }
            }
        }
        return availableMoves
    }
}
