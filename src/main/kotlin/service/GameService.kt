package service

import entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import service.network.ConnectionState
import java.lang.Exception
import kotlin.math.abs

/**
 * GameService class responsible for managing game-related services and functionalities.
 *
 * @param rootService The RootService providing access to various game-related functionalities.
 */
class GameService(private val rootService: RootService) : AbstractRefreshingService() {
    /**
     * Starts a new game with the specified parameters or default values.
     *
     * This function initializes a new game with the given list of players, gate-sharing setting,
     * and randomization option. If 'random' is set to true, the order of players is shuffled.
     *
     * The game includes a game board, treasure tiles, gems, tokens, and other game elements.
     * The initial positions of the treasure tiles are set on the game board. Players are then
     * assigned initial tiles and take their turns in a shuffled order, with each player receiving
     * a new tile and the turn being changed until all players have their initial tiles.
     *
     * @param players The list of players participating in the game.
     * @param notSharedGate A boolean indicating whether players share the same gate token.
     * @param random A boolean indicating whether to randomize the order of players.
     */
    fun startGame(
        players: MutableList<Player> = mutableListOf(),
        notSharedGate: Boolean = false,
        random: Boolean = false
    ) {
        if (random) {
            players.shuffle()
        }
        require(players.size in 2..4)
        val gameBoard = GameBoard()
        val allTiles = initializeTiles()
        val gems = initializeGems()
        val tokens = initializeTokens()

        val settings = GameSettings(players, 0, false)
        rootService.currentGame = Indigo(settings, gameBoard, allTiles, gems, tokens)
        rootService.currentGame!!.gameBoard.gateTokens = createGateTokens(players, notSharedGate)
        val treasureTiles = rootService.currentGame!!.treasureTiles
        val listCoordinate = listOf(
            Coordinate(-4, 0),
            Coordinate(-4, 4),
            Coordinate(0, 4),
            Coordinate(4, 0),
            Coordinate(4, -4),
            Coordinate(0, -4)
        )
        for (i in listCoordinate.indices) {
            val coordinate = listCoordinate[i]
            rootService.currentGame!!.gameBoard.gameBoardTiles[coordinate] = treasureTiles[i]
        }
        rootService.currentGame!!.routeTiles.shuffle()
        for (i in 0 until players.size) {
            players[i].handTile = rootService.currentGame!!.routeTiles.removeAt(0)
            onAllRefreshables { refreshAfterDistributeNewTile() }
        }
        val firstAppearance = rootService.currentGame
        val lastGame = rootService.currentGame?.copyTo()
        firstAppearance?.nextGameState = lastGame
        lastGame?.previousGameState = firstAppearance
        //rootService.currentGame?.nextGameState = lastGame
        rootService.currentGame = lastGame //rootService.currentGame?.nextGameState
        //rootService.currentGame?.nextGameState = null

        onAllRefreshables {
            refreshAfterChangePlayer()
            refreshAfterStartGame()
        }

        val connectionState = rootService.networkService.connectionState
        if (connectionState == ConnectionState.DISCONNECTED) {
            val currentPlayerIndex = rootService.currentGame!!.currentPlayerIndex
            val currentPlayer = players[currentPlayerIndex]
            if (currentPlayer.isAI) {
                when (currentPlayer) {
                    is CPUPlayer -> {
                        rootService.aiActionService.aiMove(currentPlayer.difficulty)
                    }
                }
            }
        }
        println(rootService.currentGame!!.routeTiles.size)
    }

    /**
     * Restarts the game with the specified parameters or default values.
     *
     * This function resets the game state by invoking the startGame function with the given list
     * of players, gate-sharing setting, and randomization option. If no parameters are provided,
     * the game is restarted with an empty list of players, notSharedGate set to false, and random set to false.
     *
     * @param players The list of players participating in the restarted game.
     * @param notSharedGate A boolean indicating whether players share the same gate token.
     * @param random A boolean indicating whether to randomize the order of players.
     */
    fun restartGame(
        players: MutableList<Player> = mutableListOf(),
        notSharedGate: Boolean = false,
        random: Boolean = false
    ) {
        onAllRefreshables { refreshAfterRestartGame() }
        return this.startGame(players, notSharedGate, random)
    }

    /**
     * Checks whether the current game has ended.
     *
     * This function determines if the game has reached its conclusion based on two conditions:
     * 1. All gems have been collected.
     * 2. The current player has no hand tile remaining.
     *
     * @return `true` if the game has ended, `false` otherwise.
     */
    fun endGame(): Boolean {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        val gems = currentGame.gems
        val currentPlayerIndex = currentGame.currentPlayerIndex
        val currentPlayerTile = currentGame.players[currentPlayerIndex].handTile
        return gems.isEmpty() || currentPlayerTile == null
    }

    /**
     * Checks if placing a tile at the specified coordinate is valid.
     * @param space The coordinate where the tile is to be placed.
     * @param tile The tile to be placed.
     * @param isAiCalled (optional) [Boolean] to prevent refreshes when simulating moves for the AI, defaults false
     * @return True if placement is valid, false otherwise.
     * @throws Exception if the space is already occupied or the tile blocks an exit.
     */

    fun checkPlacement(space: Coordinate, tile: Tile, isAiCalled: Boolean = false): Boolean {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        if (space == Coordinate(0, 0)) {
            if (!isAiCalled) onAllRefreshables { refreshAfterCheckPlacement() }
            return false
        }
        // Check if the space is occupied
        if (currentGame.gameBoard.gameBoardTiles[space] != null) {

            if (!isAiCalled) {
                onAllRefreshables { refreshAfterCheckPlacement() }
                throw IllegalStateException("this place is occupied")
            } else return false

        }
        // Check if the space has an exit
        return if (!coordinateHasExit(space)) {
            if (!isAiCalled) placeTile(space, tile)
            true

        } else {
            // Check if the tile blocks an exit
            return if (!tileBlocksExit(space, tile)) {
                if (!isAiCalled) placeTile(space, tile)
                true
            } else {
                if (!isAiCalled) {
                    onAllRefreshables { refreshAfterCheckPlacement() }
                    throw IllegalStateException("tile blocks exit, please rotate Tile")
                } else return false
            }
        }
    }

    /**
     * Checks if placing a tile at the specified coordinate is valid for AI.
     *
     * @param space The coordinate where the tile is intended to be placed.
     * @param tile The tile to be placed.
     * @return True if the placement is valid, false otherwise.
     */
    //for AI returning false instead of throwing exception
    fun checkPlacementAI(space: Coordinate, tile: Tile): Boolean {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        if (space == Coordinate(0, 0) || currentGame.gameBoard.gameBoardTiles[space] != null) {
            // Return false when the space is occupied or is (0, 0)
            return false
        }

        // Check if the space has an exit
        if (!coordinateHasExit(space)) {
            placeTile(space, tile)
            return true
        }

        // Check if the tile blocks an exit
        if (!tileBlocksExit(space, tile)) {
            placeTile(space, tile)
            return true
        }

        // Return false when the tile blocks an exit
        return false
    }

    /**
     * Checks if the given coordinate has an exit.
     * @param space The coordinate to check.
     * @return True if the coordinate has an exit, false otherwise.
     */
    private fun coordinateHasExit(space: Coordinate): Boolean {
        // List of  gates with exits
        val exits = listOf(
            Coordinate(1, -4),
            Coordinate(2, -4),
            Coordinate(3, -4),
            Coordinate(4, -3),
            Coordinate(4, -2),
            Coordinate(4, -1),
            Coordinate(3, 1),
            Coordinate(2, 2),
            Coordinate(1, 3),
            Coordinate(-1, 4),
            Coordinate(-2, 4),
            Coordinate(-3, 4),
            Coordinate(-4, 3),
            Coordinate(-4, 2),
            Coordinate(-4, 1),
            Coordinate(-3, -1),
            Coordinate(-2, -2),
            Coordinate(-1, -3)
        )
        return exits.contains(space)
    }

    /**
     * Places a tile at the specified coordinate.
     * @param space The coordinate where the tile is to be placed.
     * @param tile The tile to be placed.
     * @param isAiCalled (optional) [Boolean] to prevent refreshes when simulating moves for the AI, defaults false
     */
    private fun placeTile(space: Coordinate, tile: Tile, isAiCalled: Boolean = false) {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        currentGame.gameBoard.gameBoardTiles[space] = tile
        if (!isAiCalled) onAllRefreshables {
            refreshAfterPlaceTile(space, tile)
        }
    }

    /**
     * Checks if placing a tile at the specified coordinate blocks an exit.
     * @param space The coordinate where the tile is to be placed.
     * @param tile The tile to be placed.
     * @return True if placement blocks an exit, false otherwise.
     */
    private fun tileBlocksExit(space: Coordinate, tile: Tile): Boolean {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        // Define coordinates for each gate
        val gate1 = listOf(Coordinate(-4, 1), Coordinate(-4, 2), Coordinate(-4, 3))
        val gate2 = listOf(Coordinate(-3, 4), Coordinate(-2, 4), Coordinate(-1, 4))
        val gate3 = listOf(Coordinate(1, 3), Coordinate(2, 2), Coordinate(3, 1))
        val gate4 = listOf(Coordinate(4, -1), Coordinate(4, -2), Coordinate(4, -3))
        val gate5 = listOf(Coordinate(3, -4), Coordinate(2, -4), Coordinate(1, -4))
        val gate6 = listOf(Coordinate(-3, -1), Coordinate(-2, -2), Coordinate(-1, -3))
        val gates = listOf(gate1, gate2, gate3, gate4, gate5, gate6)

        var position1 = 0
        var position2 = 1
        // Check which gate the space belongs to
        for (i in gates.indices) {
            if (gates[i].contains(space)) {
                val edge1 = tile.edges[position1]
                val edge2 = getAnotherEdge(edge1, tile)
                if (edge2 == position2) {
                    return true
                }
            }
            position1 += 1
            position2 += 1
            position1 %= 6
            position2 %= 6
        }

        return false
    }

    /**
     * Gets the index of the second edge in the tile's edges list.
     * @param edge1 The first edge to find the index for.
     * @param tile The tile containing the edges.
     * @return The index of the second edge in the tile's edges list.
     */
    internal fun getAnotherEdge(edge1: Edge, tile: Tile): Int {

        val paths = tile.paths
        val edges = tile.edges

        var secondEdge: Edge? = null
        for (path in paths) {
            if (path.first == edge1) secondEdge = path.second
            if (path.second == edge1) secondEdge = path.first
        }
        return edges.indexOf(secondEdge)
    }

    /**
     * Checks for collisions between gems at the beginning and end of paths on the given tile.
     *
     * This function examines each path on the provided tile to determine if gems are present at both
     * the beginning and end of the path. If such a collision is detected, the gems are considered
     * to be colliding, and the collision is resolved by removing the gems from their positions.
     *
     * @param tile The tile to check for gem collisions.
     * @return `true` if a collision is detected and resolved, `false` otherwise.
     */
    fun checkCollision(tile: Tile): Boolean {
        for (path in tile.paths) {
            val gemAtBeginning = tile.gemEndPosition[path.first.ordinal]
            val gemAtEnd = tile.gemEndPosition[path.second.ordinal]
            //Checks if the beginning and the end of the path gave gems
            if (gemAtBeginning != null && gemAtEnd != null && gemAtBeginning != gemAtEnd) {
                //Two gems are colliding
                tile.gemEndPosition.remove(path.first.ordinal)
                tile.gemEndPosition.remove(path.second.ordinal)
                onAllRefreshables { refreshAfterCollision(gemAtEnd, gemAtBeginning) }
                return true
            }
        }
        return false
    }

    /**
     * Removes gems from the specified tile and updates scores based on the gate coordinates.
     * @param tile The tile containing the gems.
     * @param coordinate The coordinate of the tile.
     */

    fun removeGemsReachedGate(tile: Tile, coordinate: Coordinate) {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        val players = currentGame.players

        val gateTokens = currentGame.gameBoard.gateTokens
        val gate1 = listOf(Coordinate(-4, 1), Coordinate(-4, 2), Coordinate(-4, 3))
        val gate2 = listOf(Coordinate(-3, 4), Coordinate(-2, 4), Coordinate(-1, 4))
        val gate3 = listOf(Coordinate(1, 3), Coordinate(2, 2), Coordinate(3, 1))
        val gate4 = listOf(Coordinate(4, -1), Coordinate(4, -2), Coordinate(4, -3))
        val gate5 = listOf(Coordinate(1, -4), Coordinate(2, -4), Coordinate(3, -4))
        val gate6 = listOf(Coordinate(-1, -3), Coordinate(-2, -2), Coordinate(-3, -1))

        val gatesListe = mutableListOf(gate1, gate2, gate3, gate4, gate5, gate6)
        for (i in 0 until 6) {
            if (gatesListe[i].contains(coordinate)) {
                //check existence of two gems not of the same path of tile, but on the two edges beyond the gate.
                val gem1 = tile.gemEndPosition[(0 + i) % 6]
                val gem2 = tile.gemEndPosition[(1 + i) % 6]
                val gems = mutableListOf<Gem>()
                if (gem1 != null) {
                    gems.add(gem1)
                }
                if (gem2 != null) {
                    gems.add(gem2)
                }
                for (gem in gems) {
                    if (gateTokens[(i * 2)].color == gateTokens[(i * 2) + 1].color) {
                        for (player in players) {
                            if (player.color == gateTokens[(i * 2)].color) {
                                assignGem(gem, player)
                                if (gem == gem2)
                                    tile.gemEndPosition.remove((1 + i) % 6)
                                if (gem == gem1)
                                    tile.gemEndPosition.remove((0 + i) % 6)
                            }
                        }

                    } else {
                        for (player in players) {
                            if (player.color == gateTokens[(i * 2)].color) {
                                assignGem(gem, player)
                            }
                            if (player.color == gateTokens[(i * 2) + 1].color) {
                                assignGem(gem, player)
                            }
                            if (gem == gem2)
                                tile.gemEndPosition.remove((1 + i) % 6)
                            if (gem == gem1)
                                tile.gemEndPosition.remove((0 + i) % 6)
                        }
                    }
                    onAllRefreshables { refreshAfterRemoveGems(gem) }
                    println(gem.gemColor)
                    val removedElement = currentGame.gems.find { it.gemColor == gem.gemColor }
                    currentGame.gems.remove(removedElement)
                }
            }
        }
    }

    /**
     * saves the current [Indigo] gameState as a JSON file at a given location using the [IOService]
     *
     * @param path location to be given to [IOService] for saving
     * @throws IllegalStateException if currentGame is null
     */
    fun saveGame(path: String) {
        var game = rootService.currentGame
        checkNotNull(game)
        val gameStateList = mutableListOf<Indigo>()
        while (game?.previousGameState != null) {
            gameStateList.add(game)
            game = game.previousGameState
        }
        gameStateList.add(game!!)
        rootService.ioService.saveGameToFile(gameStateList, path)
        onAllRefreshables { refreshAfterSaveGame() }
    }

    /**
     * loads an [Indigo] gameState from a JSON file at a given location using the [IOService]
     *
     * @param path location to be given to [IOService] for reading JSON file
     * @throws IllegalStateException if currentGame is null after loading
     */
    fun loadGame(path: String) {
        val gameList = rootService.ioService.readGameFromFile(path)
        if (gameList.isNotEmpty()) {
            rootService.currentGame = gameList[0]
            rootService.currentGame?.nextGameState = null
            rootService.currentGame?.previousGameState = gameList[1]
        }
        for (i in 1 until gameList.size - 1) {
            rootService.currentGame = gameList[i]
            rootService.currentGame?.nextGameState = gameList[i - 1]
            rootService.currentGame?.previousGameState = gameList[i + 1]
        }
        rootService.currentGame = gameList[gameList.size - 1]


        checkNotNull(rootService.currentGame)
        onAllRefreshables { refreshAfterLoadGame() }
    }

    /**
     * function to assign a [Gem] to a given [Player]
     * @param gem [Gem] to be assigned
     * @param player [Player] to receive the [Gem]
     */
    private fun assignGem(gem: Gem, player: Player) {
        player.score += gem.gemColor.ordinal + 1
        player.collectedGems.add(gem)
        println("assign Gem : " + gem.gemColor.toString())
        println(player.collectedGems.toString())
    }

    /**
     * Changes the current player to the next player in the list.
     */
    fun changePlayer() {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)

        val playerSize = currentGame.players.size
        val currentPlayerIndex = currentGame.currentPlayerIndex

        currentGame.currentPlayerIndex = (currentPlayerIndex + 1) % playerSize

        // It's AI's turn
        val connectionState = rootService.networkService.connectionState
        if (connectionState == ConnectionState.DISCONNECTED) {
            //Xue Code
            val currentPlayer = currentGame.players[currentGame.currentPlayerIndex]
            if (currentPlayer.isAI) {
                when (currentPlayer) {
                    is CPUPlayer -> {

                        when (currentPlayer.difficulty) {
                            "easy" -> {
                                CoroutineScope(Dispatchers.JavaFx).launch {
                                    delay((3000 / currentPlayer.simulationSpeed).toLong())
                                    rootService.aiActionService.aiMove(currentPlayer.difficulty)
                                    onAllRefreshables { refreshAfterChangePlayer() }
                                }
                            }


                            else -> {
                                CoroutineScope(Dispatchers.JavaFx).launch { //
                                    delay((2000 / currentPlayer.simulationSpeed).toLong())
                                    rootService.aiActionService.aiMove(currentPlayer.difficulty)
                                    onAllRefreshables { refreshAfterChangePlayer() }
                                }
                            }
                        }
                    }
                }
            } else onAllRefreshables { refreshAfterChangePlayer() }
        }
        // Meri em Code
        /*if (currentGame.players[currentGame.currentPlayerIndex].isAI) {
            val currentCPUPlayer = currentGame.players[currentGame.currentPlayerIndex] as? CPUPlayer
            rootService.aiActionService.AiMove(currentCPUPlayer!!.difficulty)
            }*/
        /**if (currentCPUPlayer!!.difficulty.equals("easy")) {
        AIService(rootService).makeRandomTurn()
        } else {

        val timeout = 10000L
        val timer = Timer()

        var resultCoordinate: Coordinate? = null

        val thread = thread {
        resultCoordinate = MCTS(rootService, currentPlayerIndex).findNextMove()
        timer.cancel() // Cancel the timer if the task completes within the timeout
        }

        timer.schedule(timeout) {
        thread.interrupt() // Interrupt the thread if the task exceeds the timeout
        }

        try {
        thread.join()
        } catch (e: InterruptedException) {
        // implement maybe a simple MCTS that doesn't go through all the tree
        resultCoordinate = MCTS(rootService, currentPlayerIndex).findNextMoveLimited(1000)
        }
        // Eventually placing the Tile
        PlayerTurnService(rootService).placeRouteTile(resultCoordinate!!, currentCPUPlayer.handTile!!)
        }
         */
        //}
    }

    /**
     * Moves gems from one tile to another based on the specified edge indices.
     * @param currentCoordinate The tile coordinate  to which gems are moved.
     * @param neighborCoordinate The tile coordinate from which gems are moved.
     * @param currentGemPosition is the Position of the current tile which is used to check for collision
     * @param isAiCalled (optional) [Boolean] to prevent refreshes when simulating moves for the AI, defaults false
     * if both are on the Edge

     */
    fun moveGems(
        currentCoordinate: Coordinate, neighborCoordinate: Coordinate, currentGemPosition: Int,
        isAiCalled: Boolean = false
    ) {
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        val middleTile = currentGame.middleTile
        val currentTile = currentGame.gameBoard.gameBoardTiles[currentCoordinate]
        val neighbourTile = currentGame.gameBoard.gameBoardTiles[neighborCoordinate]
        var neighbourStart = (currentGemPosition + 3) % 6
        val neighborCoordinates = getNeighboringCoordinates(currentCoordinate)
        if (currentTile == null) {
            return
        }

        // if in the middle are no more Gems
        if (neighborCoordinate.row == 0 && neighborCoordinate.column == 0) {
            val amountOfGems = middleTile.gemPosition.size
            if (amountOfGems <= 0) {
                return
            }
            if (neighbourStart == 5) neighbourStart = 1
            else {
                neighbourStart++
            }
            println("Neighbour Start before change$neighbourStart")
            val currentTileGem = currentTile.gemEndPosition[currentGemPosition]
            if (middleTile.gemPosition[neighbourStart] == null) {
                neighbourStart = (neighbourStart + 1) % 6
                if (neighbourStart == 0 && amountOfGems > 1) neighbourStart = (neighbourStart - 2 + 6) % 6
                if (middleTile.gemPosition[neighbourStart] == null) {
                    neighbourStart = (neighbourStart - 2 + 6) % 6
                }
            }
            if (neighbourStart == 0 && amountOfGems > 1) {
                neighbourStart = (neighbourStart - 1 + 6) % 6
                if (middleTile.gemPosition[neighbourStart] == null) neighbourStart = (neighbourStart + 2) % 6
            }
            if (middleTile.gemPosition[neighbourStart] == null) {
                for ((key) in middleTile.gemPosition) {
                    if (key != 0) {
                        neighbourStart = key
                    }
                }
            }
            if (amountOfGems == 1) neighbourStart = 0
            if (currentTileGem != null) {
                onAllRefreshables {
                    refreshAfterCollision(
                        middleTile.gemPosition[neighbourStart]!!,
                        currentTile.gemEndPosition[currentGemPosition]!!
                    )
                }
                val removedElement =
                    currentGame.gems.find { it.gemColor == middleTile.gemPosition[neighbourStart]!!.gemColor }
                currentGame.gems.remove(removedElement)
                val removedGem =
                    currentGame.gems.find { it.gemColor == currentTile.gemEndPosition[currentGemPosition]!!.gemColor }
                currentGame.gems.remove(removedGem)
                middleTile.gemPosition.remove(amountOfGems - 1)
                currentTile.gemEndPosition.remove(currentGemPosition)
                return
            }
            val middleTileGem = middleTile.gemPosition[neighbourStart]
            val lastGemPosition = getAnotherEdge(currentTile.edges[currentGemPosition], currentTile)
            middleTile.gemPosition.remove(neighbourStart)
            println("Gem Postion$neighbourStart")
            println("middleTileGem ${middleTileGem?.gemColor}")
            currentTile.gemEndPosition[lastGemPosition] = middleTileGem!!
            println(currentCoordinate.toString())
            println(lastGemPosition)
            if (!isAiCalled) onAllRefreshables {
                refreshAfterMoveGems(
                    currentTile.gemEndPosition[lastGemPosition]!!,
                    currentCoordinate,
                    lastGemPosition
                )
            }
            moveGems(neighborCoordinates[lastGemPosition], currentCoordinate, ((lastGemPosition + 3) % 6))
        }
        if (neighbourTile == null) {
            return
        }

        val tileGems = currentTile.gemEndPosition
        val neighbourGems = neighbourTile.gemEndPosition

        if (tileGems.contains(currentGemPosition)) {
            if (neighbourGems.contains(neighbourStart)) {
                onAllRefreshables {
                    refreshAfterCollision(
                        tileGems[currentGemPosition]!!,
                        neighbourGems[neighbourStart]!!
                    )
                }
                val removedElement = currentGame.gems.find { it.gemColor == tileGems[currentGemPosition]!!.gemColor }
                currentGame.gems.remove(removedElement)
                val removedGem =
                    currentGame.gems.find { it.gemColor == neighbourGems[neighbourStart]!!.gemColor }
                currentGame.gems.remove(removedGem)
                tileGems.remove(currentGemPosition)
                neighbourGems.remove(neighbourStart)
                return
            }
        }
        if (!neighbourTile.gemEndPosition.contains(neighbourStart)) return
        val currentEdge = currentTile.edges[currentGemPosition]
        val currentEnd = getAnotherEdge(currentEdge, currentTile)
        currentTile.gemEndPosition[currentEnd] = neighbourGems[neighbourStart]!!
        neighbourGems.remove(neighbourStart)
        if (currentTile.gemEndPosition[currentEnd] != null) {
            if (!isAiCalled) onAllRefreshables {
                refreshAfterMoveGems(
                    currentTile.gemEndPosition[currentEnd]!!,
                    currentCoordinate,
                    currentEnd
                )
            }
        }
        println(currentCoordinate.toString())
        println("currentEnd $currentEnd")
        removeGemsReachedGate(currentTile, currentCoordinate)

        moveGems(
            neighborCoordinates[currentEnd], currentCoordinate, abs((currentEnd + 3)) % 6
        )
    }


    /**
     * function to give the current [Player] a new route [Tile] at the end of their turn (first in list)
     *
     * @param isAiCalled (optional) [Boolean] to prevent refreshes when simulating moves for the AI, defaults false
     * @throws IllegalStateException if currentGame in [rootService] is null or no route [Tile]s remain
     */
    fun distributeNewTile(isAiCalled: Boolean = false) {
        val game = rootService.currentGame
        checkNotNull(game)
        if (game.routeTiles.isEmpty()) {
            game.players[game.currentPlayerIndex].handTile = null
        } else {
            val newHandTile = game.routeTiles.removeAt(0)
            val currentPlayerIndex = game.currentPlayerIndex
            game.settings.players[currentPlayerIndex].handTile = newHandTile
        }
        if (!isAiCalled) onAllRefreshables { refreshAfterDistributeNewTile() }
    }

    /**
     * Initializes and returns a MutableList of Tile objects representing the game's tiles.
     *
     * This function creates a list of tiles with specific configurations, including paths and
     * gem positions. The tiles include gems at the starting and ending positions of paths,
     * and different types of route tiles with various path configurations.
     *
     * @return A MutableList containing the initialized Tile objects.
     */
    private fun initializeTiles(): MutableList<Tile> {
        val allTiles: MutableList<Tile> = mutableListOf()
        //Position of gem is determined, path starts and ends on the adjacent sides
        for (i in 0 until 6) {
            val gemPos = (i + 3) % 6
            allTiles.add(
                Tile(
                    listOf(
                        Pair(
                            Edge.values()[(Edge.values().size + gemPos - 1 + 6) % 6],
                            Edge.values()[(Edge.values().size + gemPos + 1) % 6]
                        )
                    ), TileType.Type_5, mutableMapOf(Pair(gemPos, Gem(GemColor.AMBER)))
                )
            )
        }
        //TypeID 0 Route Tiles are added
        var path1 = Pair(Edge.ZERO, Edge.TWO)
        var path2 = Pair(Edge.ONE, Edge.FOUR)
        var path3 = Pair(Edge.THREE, Edge.FIVE)
        for (i in 0 until 14) {
            allTiles.add(Tile(listOf(path1, path2, path3), TileType.Type_0))
        }
        //TypeID 1 Route Tiles are added
        path1 = Pair(Edge.TWO, Edge.FIVE)
        path2 = Pair(Edge.ONE, Edge.FOUR)
        path3 = Pair(Edge.ZERO, Edge.THREE)
        for (i in 0 until 6) {
            allTiles.add(Tile(listOf(path1, path2, path3), TileType.Type_1))
        }
        //TypeID 2 Route Tiles are added
        path1 = Pair(Edge.ZERO, Edge.FIVE)
        path2 = Pair(Edge.ONE, Edge.FOUR)
        path3 = Pair(Edge.TWO, Edge.THREE)
        for (i in 0 until 14) {
            allTiles.add(Tile(listOf(path1, path2, path3), TileType.Type_2))
        }
        //TypeID 3 Route Tiles are added
        path1 = Pair(Edge.ZERO, Edge.FIVE)
        path2 = Pair(Edge.ONE, Edge.THREE)
        path3 = Pair(Edge.TWO, Edge.FOUR)
        for (i in 0 until 14) {
            allTiles.add(Tile(listOf(path1, path2, path3), TileType.Type_3))
        }
        //TypeID 4 Route Tiles are added
        path1 = Pair(Edge.ZERO, Edge.FIVE)
        path2 = Pair(Edge.ONE, Edge.TWO)
        path3 = Pair(Edge.THREE, Edge.FOUR)
        for (i in 0 until 6) {
            allTiles.add(Tile(listOf(path1, path2, path3), TileType.Type_4))
        }
        return allTiles
    }

    /**
     * Initializes and returns a MutableList of Gem objects representing the game's gems.
     *
     * This function creates a list of gems with specific colors and quantities. The list includes
     * six gems of Amber color, five gems of Emerald color, and one gem of Sapphire color.
     *
     * @return A MutableList containing the initialized Gem objects.
     */
    fun initializeGems(): MutableList<Gem> {
        val gems: MutableList<Gem> = mutableListOf()
        for (i in 0 until 6) gems.add(Gem(GemColor.AMBER))
        for (i in 0 until 5) gems.add(Gem(GemColor.EMERALD))
        gems.add(Gem(GemColor.SAPPHIRE))
        return gems
    }

    /**
     * Initializes and returns a MutableList of Token objects.
     *
     * This function creates a list of tokens with predefined colors and quantities.
     * The list includes six tokens of each color: WHITE, PURPLE, BLUE, and RED.
     *
     * @return A MutableList containing the initialized Token objects.
     */
    fun initializeTokens(): MutableList<Token> {
        val tokens: MutableList<Token> = mutableListOf()
        for (i in 0 until 6) tokens.add(Token(TokenColor.WHITE))
        for (i in 0 until 6) tokens.add(Token(TokenColor.PURPLE))
        for (i in 0 until 6) tokens.add(Token(TokenColor.BLUE))
        for (i in 0 until 6) tokens.add(Token(TokenColor.RED))
        return tokens
    }


    /**
     * Gets the neighboring coordinates for a given coordinate, which the contains a tile
     * @param coordinate The coordinate for which to find neighboring coordinates
     * @return List of neighboring coordinates
     */
    fun getNeighboringCoordinates(coordinate: Coordinate): List<Coordinate> {
        val neighbors = mutableListOf<Coordinate>()
        //hexagonal grid
        neighbors.add(Coordinate(coordinate.row - 1, coordinate.column))      // Above
        neighbors.add(Coordinate(coordinate.row - 1, coordinate.column + 1))  // Top-right
        neighbors.add(Coordinate(coordinate.row, coordinate.column + 1))      // Bottom-right
        neighbors.add(Coordinate(coordinate.row + 1, coordinate.column))      // Below
        neighbors.add(Coordinate(coordinate.row + 1, coordinate.column - 1))  // Bottom-left
        neighbors.add(Coordinate(coordinate.row, coordinate.column - 1)) // Top-left

        return neighbors
    }
    /*
        /**
         * Gets the neighboring tiles for a given coordinate
         * @param coordinate The coordinate for which to find neighboring tiles
         * @return List of neighboring tiles
         */
        fun getNeighboringTiles(coordinate: Coordinate): List<Tile> {
            val neighboringTiles = mutableListOf<Tile>()
            val currentGame = rootService.currentGame
            checkNotNull(currentGame)

            for (neighborCoordinate in getNeighboringCoordinates(coordinate)) {
                currentGame.gameBoard.gameBoardTiles[neighborCoordinate]?.let {
                    neighboringTiles.add(it)
                }
            }
            return neighboringTiles
        }
    */
    /**
     * The private function [createGateTokens]
     * create from the upcoming information of the network
     * to make the gate tokens
     *
     * @param players The players which are participate the game
     * @param notSharedGates  The [notSharedGates] is two decide which mode the gates are
     * initialize
     *
     * @return A mutable list of token as gateTokens
     */
    fun createGateTokens(players: List<Player>, notSharedGates: Boolean): MutableList<Token> {
        val gateTokens = mutableListOf<Token>()
        val playerSize = players.size
        if (notSharedGates) {
            for (i in 0 until 6) {
                gateTokens.add(Token(players[i % playerSize].color))
                gateTokens.add(Token(players[i % playerSize].color))
            }
        } else {
            if (playerSize == 4) {
                gateTokens.add(Token(players[0].color))
                gateTokens.add(Token(players[1].color))
                gateTokens.add(Token(players[1].color))
                gateTokens.add(Token(players[2].color))
                gateTokens.add(Token(players[0].color))
                gateTokens.add(Token(players[3].color))
                gateTokens.add(Token(players[3].color))
                gateTokens.add(Token(players[1].color))
                gateTokens.add(Token(players[2].color))
                gateTokens.add(Token(players[0].color))
                gateTokens.add(Token(players[2].color))
                gateTokens.add(Token(players[3].color))
            }
            if (playerSize == 3) {
                for (i in 0 until 3) {
                    gateTokens.add(Token(players[i * 2 % 3].color))
                    gateTokens.add(Token(players[i * 2 % 3].color))
                    gateTokens.add(Token(players[i * 2 % 3].color))
                    gateTokens.add(Token(players[(i * 2 + 1) % 3].color))
                }
            }
        }
        return gateTokens
    }
}