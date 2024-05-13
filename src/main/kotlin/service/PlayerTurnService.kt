package service

import entity.*
import service.network.ConnectionState
import java.lang.Exception

/**
 * Service class for managing player turns and actions.
 * @param rootService The root service providing access to the current game state.
 */
class PlayerTurnService(private val rootService: RootService) : AbstractRefreshingService() {
    /**
     * Places a route tile at the specified coordinate.
     * @param space The coordinate where the tile is to be placed.
     * @param tile The tile to be placed.
     * @throws Exception if the placement is invalid.
     */

    fun placeRouteTile(space: Coordinate, tile: Tile, isAiCalled: Boolean =false) {
        val currentGame = rootService.currentGame
        // Check if the game has started
        checkNotNull(currentGame) { "The game has not started yet" }

        // meryem code
        // Check if game ended
        /*if (rootService.gameService.endGame()) {
            onAllRefreshables { refreshAfterEndGame() }
        }
        */
        val firstAppearance = currentGame
        // Check if the tile placement is valid
        if (rootService.gameService.checkPlacement(space, tile,true)) {

            rootService.gameService.checkPlacement(space, tile)
            // Move gems, check collisions, distribute new tiles, and change the player
            val neighbors = rootService.gameService.getNeighboringCoordinates(space)
            for (i in neighbors.indices) {
                rootService.gameService.moveGems(space, neighbors[i], i)
            }
            // change rows with moveGems?
            if (!rootService.gameService.endGame()){
                rootService.gameService.distributeNewTile()
                rootService.gameService.changePlayer()
            }

            val lastGame = rootService.currentGame?.copyTo()
            firstAppearance.nextGameState = lastGame
            lastGame?.previousGameState = firstAppearance
            //rootService.currentGame?.nextGameState = lastGame
            rootService.currentGame = lastGame //rootService.currentGame?.nextGameState
            //rootService.currentGame?.nextGameState = null

            //if is your turn in the NetworkMode
            val connectionState = rootService.networkService.connectionState
            if (connectionState == ConnectionState.PLAYING_MY_TURN) {
                rootService.networkService.sendPlacedTile(tile, space)
            }
            //spiellogik team code
            if (rootService.gameService.endGame()) {
                onAllRefreshables { refreshAfterEndGame() }
            }
        } else {
            if (!isAiCalled) {
                throw IllegalStateException("Invalid space, choose another space please")
            } else{
                rotateTileLeft(tile)
                placeRouteTile(space,tile,true)

            }
        }
    }


    /**
     * Undoes one game move and returns to the previous game state,
     * it is possible to undo moves until the beginning of the game
     */
    fun undo() {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        if (currentGame.previousGameState != null) {
            //currentGame.nextGameState = currentGame
            rootService.currentGame = currentGame.previousGameState
            onAllRefreshables { refreshAfterUndo() }
            println("UNDO")


        } else {
            println("Previous game state doesn't exist, cannot undo the move")
        }
    }


    /**
     * Redoes one game move and returns to the next game state,
     * it is possible to redo moves from the beginning until the last made move
     */
    fun redo() {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        if (currentGame.nextGameState != null) {
            rootService.currentGame = currentGame.nextGameState
            onAllRefreshables { refreshAfterRedo() }
            println("REDO")
        } else {
            println("Next game state doesn't exist, cannot redo the move")
        }

    }

    /**
     * Rotates the edges of a tile to the left.
     *
     * @param tile The tile to be rotated.
     * @param isAiCalled Indicates whether the rotation is called by an AI component.
     */
    fun rotateTileLeft(tile: Tile, isAiCalled: Boolean = false) {    // Add the first edge to the end of the list
        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        tile.edges.addAll(tile.edges.subList(0, 1))
        // Remove the original first edge
        tile.edges.removeAll(tile.edges.subList(0, 1))
        if (!isAiCalled) onAllRefreshables { refreshAfterLeftRotation(game.currentPlayerIndex) }
    }

    /**
     * Rotates the tile to the right.
     * @param tile The tile to be rotated.
     * @param isAiCalled (optional) [Boolean] to prevent refreshes when simulating an AI move, defaults to false
     * @throws IllegalStateException if no game is running
     */
    fun rotateTileRight(tile: Tile, isAiCalled: Boolean = false) {    // Add the last edge to the beginning of the list
        val game = rootService.currentGame
        checkNotNull(game) { "No game found." }

        tile.edges.addAll(0, tile.edges.subList(tile.edges.size - 1, tile.edges.size))
        // Remove the original last edge
        tile.edges.subList(tile.edges.size - 1, tile.edges.size).clear()
        if (!isAiCalled) onAllRefreshables { refreshAfterRightRotation(game.currentPlayerIndex) }
    }

}

