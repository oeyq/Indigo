package service.ai
import entity.Indigo
import entity.*
import kotlin.math.abs
/**
 * Service class responsible for managing game-related logic for AI simulation.
 *
 * @property currentGame The current game instance of type Indigo.
 */
class GameServiceAi (var currentGame: Indigo) {

    /**
     * Checks whether placing a tile at a specified coordinate is valid.
     *
     * @param space The coordinate where the tile is intended to be placed.
     * @param tile The tile to be placed.
     * @param isAiCalled Indicates whether the check is called by an AI component.
     * @return True if the placement is valid, false otherwise.
     */
    fun checkPlacement(space: Coordinate, tile: Tile, isAiCalled: Boolean = false): Boolean {

        if (space == Coordinate(0, 0)|| isHiddenCoordinate(space)|| isTreasureCoordinate(space)) {
            return false
        }
        // Check if the space is occupied
        if (currentGame.gameBoard.gameBoardTiles[space] != null) {
            return false
        }
        // Check if the space has an exit
        return if (!coordinateHasExit(space)) {
            true
        } else {
            // Check if the tile blocks an exit
            return if (!tileBlocksExit(space, tile)) {
              true
            } else {
                if (!isAiCalled) {
                    return false
                } else return true
            }
        }
    }

    /**
     * Checks if a given coordinate corresponds to a gate tile.
     *
     * @param coordinate The coordinate to be checked.
     * @return True if the coordinate represents a gate tile, false otherwise.
     */
    private fun isTreasureCoordinate(coordinate: Coordinate): Boolean {
        // List of coordinates representing gate tiles
        val gateCoordinates = listOf(
            Coordinate(0, -4),
            Coordinate(4, -4),
            Coordinate(4, 0),
            Coordinate(0, 4),
            Coordinate(-4, 4),
            Coordinate(-4, 0)
        )
        return coordinate in gateCoordinates
    }

    /**
     * Checks if a given coordinate corresponds to a hidden tile.
     *
     * @param coordinate The coordinate to be checked.
     * @return True if the coordinate represents a hidden tile, false otherwise.
     */
    private fun isHiddenCoordinate(coordinate: Coordinate): Boolean {
        for (i in 0..3) {
            for (j in -4..-4 + i) {
                // Check if the coordinate matches the hidden tile pattern
                if (coordinate == Coordinate(-(i + 1), j) || coordinate == Coordinate(i + 1, -j)) {
                    return true
                }
            }
        }
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
     */
    fun placeTile(space: Coordinate, tile: Tile) {
        currentGame.gameBoard.gameBoardTiles[space] = tile
    }

    /**
     * Checks if placing a tile at the specified coordinate blocks an exit.
     * @param space The coordinate where the tile is to be placed.
     * @param tile The tile to be placed.
     * @return True if placement blocks an exit, false otherwise.
     */
    private fun tileBlocksExit(space: Coordinate, tile: Tile): Boolean {
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
    private fun getAnotherEdge(edge1: Edge, tile: Tile): Int {

        val paths = tile.paths
        val edges = tile.edges

        var secondEdge: Edge? = null

        // Debug: Print the paths and edges

        //if (tile.type==TileType.Type_5) println("Debug: Paths : $paths")
        //if (tile.type==TileType.Type_5) println("Debug: Edges : $edges")
        for (path in paths) {
            if (path.first == edge1) secondEdge = path.second
            if (path.second == edge1) secondEdge = path.first
        }

        // Debug: Print the secondEdge
        //if (tile.type==TileType.Type_5) println("Debug: Second Edge : $secondEdge")

       // val indexOfSecondEdge = edges.indexOf(secondEdge)

        // Debug: Print the result
        //if (tile.type==TileType.Type_5) println("Debug: Index of Second Edge : $indexOfSecondEdge")

        return edges.indexOf(secondEdge)
    }

    /**
     * Removes gems from the specified tile and updates scores based on the gate coordinates.
     * @param tile The tile containing the gems.
     * @param coordinate The coordinate of the tile.
     */

    private fun removeGemsReachedGate(tile: Tile, coordinate: Coordinate) {
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
                    println(gem.gemColor)
                    val removedElement = currentGame.gems.find { it.gemColor == gem.gemColor }
                    currentGame.gems.remove(removedElement)
                }
            }
        }
    }


    /**
     * function to assign a [Gem] to a given [Player]
     * @param gem [Gem] to be assigned
     * @param player [Player] to receive the [Gem]
     */
    private fun assignGem(gem: Gem, player: Player) {
        player.score += gem.gemColor.ordinal + 1
        player.collectedGems.add(gem)
    }

    /**
     * Moves gems from one tile to another based on the specified edge indices.
     * @param currentCoordinate The tile coordinate  to which gems are moved.
     * @param neighborCoordinate The tile coordinate from which gems are moved.
     * @param currentGemPosition is the Position of the current tile which is used to check for collision
     * if both are on the Edge

     */

    //***********************TO DO : to fix for the AI-simulation**************
    fun moveGems(
        currentCoordinate: Coordinate, neighborCoordinate: Coordinate, currentGemPosition: Int, ) {

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
            if(neighbourStart == 5) neighbourStart = 1
            else {
                neighbourStart++
            }
            println("Neighbour Start before change$neighbourStart")
            val currentTileGem = currentTile.gemEndPosition[currentGemPosition]
            if (middleTile.gemPosition[neighbourStart] == null) {
                neighbourStart = (neighbourStart + 1) % 6
                if (neighbourStart == 0 && amountOfGems > 1) neighbourStart = (neighbourStart - 2 + 6) % 6
                if(middleTile.gemPosition[neighbourStart] == null){
                    neighbourStart = (neighbourStart - 2 + 6) % 6
                }
            }
            if (neighbourStart == 0 && amountOfGems > 1) {
                neighbourStart = (neighbourStart -1+6) % 6
                if (middleTile.gemPosition[neighbourStart] == null)neighbourStart = (neighbourStart +2) % 6
            }
            if(middleTile.gemPosition[neighbourStart]==null && amountOfGems == 2){
                for ((key) in middleTile.gemPosition){
                    if(key!=0){
                        neighbourStart = key
                    }
                }
            }
            if (amountOfGems == 1) neighbourStart = 0
            if (currentTileGem != null) {

                val removedElement = middleTile.gemPosition[neighbourStart]?.let { gem ->
                    currentGame.gems.find { it.gemColor == gem.gemColor }
                }

                removedElement?.let { currentGame.gems.remove(it) }

                val removedGem = currentTile.gemEndPosition[currentGemPosition]?.let { gem ->
                    currentGame.gems.find { it.gemColor == gem.gemColor }
                }

                removedGem?.let { currentGame.gems.remove(it) }

                middleTile.gemPosition.remove(amountOfGems - 1)
                currentTile.gemEndPosition.remove(currentGemPosition)
                return
            }
            val middleTileGem = middleTile.gemPosition[neighbourStart]
            val lastGemPosition = getAnotherEdge(currentTile.edges[currentGemPosition], currentTile)
            middleTile.gemPosition.remove(neighbourStart)
            if (currentTile.gemEndPosition[lastGemPosition] != null) {

                val removedElement = middleTileGem?.let { gem ->
                    currentGame.gems.find { it.gemColor == gem.gemColor }
                }

                removedElement?.let { currentGame.gems.remove(it) }

                val removedGem = currentTile.gemEndPosition[lastGemPosition]?.let { gem ->
                    currentGame.gems.find { it.gemColor == gem.gemColor }
                }

                removedGem?.let {
                    currentGame.gems.remove(it)
                    currentTile.gemEndPosition.remove(lastGemPosition)
                }

            }
            println("Gem Postion$neighbourStart")
            println("middleTileGem ${middleTileGem?.gemColor}")
            middleTileGem?.let {
                currentTile.gemEndPosition[lastGemPosition] = it
            }
            println(currentCoordinate.toString())
            println(lastGemPosition)

            moveGems(neighborCoordinates[lastGemPosition], currentCoordinate, ((lastGemPosition + 3) % 6))
        }
        if (neighbourTile == null) {
            return
        }

        val tileGems = currentTile.gemEndPosition
        val neighbourGems = neighbourTile.gemEndPosition
        val neighborEdge = neighbourTile.edges[neighbourStart]
        val neighborEnd = getAnotherEdge(neighborEdge, neighbourTile)

        if (tileGems.contains(currentGemPosition)) {
            if (neighbourGems.contains(neighbourStart)) {

                val removedElement = currentGame.gems.find { it.gemColor == tileGems[currentGemPosition]!!.gemColor }
                currentGame.gems.remove(removedElement)
                val removedGem =
                    currentGame.gems.find { it.gemColor == neighbourGems[neighbourStart]!!.gemColor }
                currentGame.gems.remove(removedGem)
                tileGems.remove(currentGemPosition)
                neighbourGems.remove(neighbourStart)
                return
            }

            if (neighbourGems.contains(neighborEnd)) {

                val removedElement = currentGame.gems.find { it.gemColor == tileGems[currentGemPosition]!!.gemColor }
                currentGame.gems.remove(removedElement)
                val removedGem =
                    currentGame.gems.find { it.gemColor == neighbourGems[neighborEnd]!!.gemColor }
                currentGame.gems.remove(removedGem)
                tileGems.remove(currentGemPosition)
                neighbourGems.remove(neighborEnd)
                return
            }

        }
        if (!neighbourTile.gemEndPosition.contains(neighbourStart)) return
        val currentEdge = currentTile.edges[currentGemPosition]
        val currentEnd = getAnotherEdge(currentEdge, currentTile)
        currentTile.gemEndPosition[currentEnd] = neighbourGems[neighbourStart]!!
        neighbourGems.remove(neighbourStart)
        /*if (currentTile.gemEndPosition[currentEnd] != null) {

        }*/
        println( currentCoordinate.toString())
        println("currentend $currentEnd")
        removeGemsReachedGate(currentTile, currentCoordinate)

        if(currentEnd!=-1){
        moveGems(
            neighborCoordinates[currentEnd], currentCoordinate, abs((currentEnd + 3)) % 6
        )
        }
    }

    /**
     * Gets the neighboring coordinates for a given coordinate, which the contains a tile
     * @param coordinate The coordinate for which to find neighboring coordinates
     * @return List of neighboring coordinates
     */
    private fun getNeighboringCoordinates(coordinate: Coordinate): List<Coordinate> {
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


    /**
     * Distributes a new tile to the current player. If there are no more route tiles,
     * sets the current player's hand tile to null.
     */
    fun distributeNewTile() {
        if (currentGame.routeTiles.isEmpty()) {
            currentGame.players[currentGame.currentPlayerIndex].handTile = null
        } else {
            // Shuffle before distributing new Tile so that the AI don't cheat
            currentGame.routeTiles.shuffle()
            val newHandTile = currentGame.routeTiles.removeAt(0)
            val currentPlayerIndex = currentGame.currentPlayerIndex
            currentGame.settings.players[currentPlayerIndex].handTile = newHandTile
        }
    }

}


