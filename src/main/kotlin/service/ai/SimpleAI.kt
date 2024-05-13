package service.ai

import entity.*
import service.*

/**
 * SimpleAI class implementing a strategy of maximizing value gained during a turn.
 * @param root [RootService] this SimpleAI belongs to
 */
class SimpleAI(private val root: RootService): AbstractRefreshingService() {

//these do not change during the course of a game
    /** gates owned by the player */
    private val ownGates : List<Int> = findOwnGates()
    /** gates not owned by the player */
    private val enemyGates: List<Int> = mutableListOf(0,1,2,3,4,5).filterNot { ownGates.contains(it) }.toList()

//these all change and need to be updated each move
    /** all [Coordinate]s found to contain [Gem]s on their tiles */
    private var gemTiles: List<Coordinate> = listOf()
    /** Positions of [Gem]s on their respective Tile */
    private var gemPositions: List<Pair<Int,Gem>> = listOf()
    /** all [Coordinate]s that extend the path of a [Gem] */
    private var moves: List<Coordinate> = listOf()
    /** Nearest owned gate index and distance to gate for each gem */
    private var nearestGates: List<Pair<Int,Int>> = listOf()
    /** Nearest enemy gate index and distance to gate for each gem */
    private var nearestEnemyGates: List<Pair<Int,Int>> = listOf()
    /** [List] of [Pair]s consisting of the best rotation for each move and the value for that move */
    private var moveValues: List<Pair<Int,Int>> = listOf()

    /**
     * finds the best move for the strategy and places the tile accordingly
     */
    fun makeMove() {
        val randomAI = RandomAI(root)
        val game = root.currentGame
        checkNotNull(game)
        val player = game.players[game.currentPlayerIndex]
        val playerTurnService = root.playerTurnService
        gatherInformation()
        if (gemTiles.isNotEmpty()){
            var bestDistanceDelta: Int = Int.MIN_VALUE
            var bestMoveIndex = 0
            for (i in moveValues.indices){
                if (moveValues[i].second > bestDistanceDelta){
                    bestDistanceDelta = moveValues[i].second
                    bestMoveIndex = i
                }
            }
            for (i in 0 until moveValues[bestMoveIndex].first){
                playerTurnService.rotateTileRight(player.handTile!!)
            }
            println("[SimpleAI DEBUG] gemTile: "+gemTiles[bestMoveIndex]+" move: " +
                    moves[bestMoveIndex]+"rotation:"+moveValues[bestMoveIndex].first)
            playerTurnService.placeRouteTile(moves[bestMoveIndex], player.handTile!!)
        }else{
            val randomCoordinate = randomAI.findAvailableMoves().shuffled().first().first
            playerTurnService.placeRouteTile(randomCoordinate, player.handTile!!)
        }
        onAllRefreshables { refreshAfterAITurn() }
    }
    /**
     * Simulates where a [Gem] ends up after each move in [moves] with each possible rotation
     *
     * the value of a move is calculated using twice the distance gained towards the nearest own gate and
     * the distance lost towards the nearest enemy gate, weighted based on [Gem] value and closeness to own gate
     * @return [List] of [Pair]s representing the best Orientation of the tile for each move
     * and the Distance delta the move causes
     */
    private fun tryOrientations(): List<Pair<Int,Int>>{
        val game = root.currentGame
        checkNotNull(game)
        val player = game.players[game.currentPlayerIndex]
        checkNotNull(player.handTile) {"Player does not have a Tile"}
        println("[SimpleAI DEBUG] tile: "+player.handTile.toString())
        val playerTurnService = root.playerTurnService
        val gameService = root.gameService
        var newEndPos: Int
        var ownGateDelta: Int
        var enemyGateDelta: Int
        var moveValue: Int
        var bestValue: Int
        var bestRotation = 0
        val result: MutableList<Pair<Int,Int>> = mutableListOf()
        for (i in moves.indices){
            bestValue = Int.MIN_VALUE
            for (j in 0..5){
                if (gameService.checkPlacement(moves[i],player.handTile!!,true)) {   //handTile checked above
                    newEndPos = calculateNewEndPosition(i,j)
                    ownGateDelta = calculateDistance(findNeighbor(moves[i],(newEndPos-j).mod(6)),
                        nearestGates[i].first)-nearestGates[i].second
                    enemyGateDelta = calculateDistance(findNeighbor(moves[i],(newEndPos-j).mod(6)),
                        nearestEnemyGates[i].first)-nearestEnemyGates[i].second
                    moveValue = (enemyGateDelta-(ownGateDelta * 2))*
                            (gemPositions[i].second.gemColor.ordinal + 1) * (6 - nearestGates[i].second)
                    if (moveValue > bestValue){
                        bestValue = moveValue
                        bestRotation = (j).mod(6)
                    }
                }
                playerTurnService.rotateTileRight(player.handTile!!,true)           //handTile checked above
            }
            result.add(Pair(bestRotation,bestValue))
        }
        return result.toList()
    }
    /**
     * calculates where a Gem ends up if the handTile is placed at the [Coordinate] given by [moves]
     *
     * @param index [Int] specifying which of the possible [Coordinate]s in [moves] should be regarded
     * @param rotation [Int] indicating how often the [Tile] was rotated right
     * @return [Int] representing the [Tile.gemEndPosition] after the simulated move
     */
    private fun calculateNewEndPosition(index: Int, rotation: Int): Int{
        val game = root.currentGame
        checkNotNull(game)
        val player = game.players[game.currentPlayerIndex]
        checkNotNull(player.handTile)
        val gemStartPosition = (gemPositions[index].first+3+rotation).mod(6)
        for (path in player.handTile!!.paths){
            if (path.first.ordinal == gemStartPosition){
                return path.second.ordinal
            }else if (path.second.ordinal == gemStartPosition){
                return path.first.ordinal
            }
        }
        return -1 //cannot happen, one of the paths has to include the correct edge
    }
    /**
     * function for updating information needed for the AI to choose a move
     */
    private fun gatherInformation(){
        gemTiles = findGems()
        println("[SimpleAI DEBUG] gemTiles size: "+gemTiles.size)
        for (element in gemTiles){
            println("[SimpleAI DEBUG] $element")
        }
        if (gemTiles.isNotEmpty()){
            gemPositions = findGemPositions()
            println("[SimpleAI DEBUG] gemPos size: "+gemPositions.size)
            for (element in gemPositions){
                println("[SimpleAI DEBUG] $element")
            }
            moves = findMoves()
            println("[SimpleAI DEBUG] moves size: "+moves.size)
            for (element in moves){
                println("[SimpleAI DEBUG] $element")
            }
            nearestGates = findNearestGate(ownGates)
            println("[SimpleAI DEBUG] nearestGates (GATE, DISTANCE) size: "+nearestGates.size)
            for (element in nearestGates){
                println("[SimpleAI DEBUG] $element")
            }
            nearestEnemyGates = findNearestGate(enemyGates)
            println("[SimpleAI DEBUG] nearestEnemyGates (GATE, DISTANCE) size: "+nearestEnemyGates.size)
            for (element in nearestEnemyGates){
                println("[SimpleAI DEBUG] $element")
            }
            moveValues = tryOrientations()
            println("[SimpleAI DEBUG] values (ROTATION, VALUE) size: "+moveValues.size)
            for (element in moveValues){
                println("[SimpleAI DEBUG] $element")
            }
        }
    }
    /**
     * Finds the nearest gate for each move [Coordinate] where a gem would be moved by placing a tile
     *
     * @param gates [List] of gate indices to be evaluated
     * @return [List] of [Pair]s representing the nearest Gate index and the distance to it for each move
     */
    private fun findNearestGate(gates: List<Int>) : List<Pair<Int,Int>>{
        val game = root.currentGame
        checkNotNull(game)
        val result : MutableList<Pair<Int,Int>> = mutableListOf()
        var tempDist: Int
        var minDist: Int
        var minGate: Int = -1

        for (move in moves){
            minDist = Int.MAX_VALUE
            for (gate in gates){
                tempDist = calculateDistance(move,gate)
                if (tempDist < minDist){
                    minDist = tempDist
                    minGate = gate
                }
            }
            result.add(Pair(minGate,minDist))
        }
        return result.toList()
    }

    /**
     * calculates the Distance between a given Point and a given gate using the threefold symmetry of the [GameBoard]
     *
     * @param position [Coordinate] from which to calculate the Distance
     * @param gate [Int] representing the Gate to which the Distance is calculated
     * @return Distance between the [position] [Coordinate] and the [gate]
     */
    private fun calculateDistance(position : Coordinate, gate : Int) : Int {
        val distance = when (gate){
            0 -> 5+position.row
            1 -> 5-position.column
            2 -> 5-(position.column+position.row)
            3 -> 5-position.row
            4 -> 5+position.column
            5 -> 5+(position.column+position.row)
            else -> Int.MAX_VALUE
        }
        return distance
    }
    /**
     * takes the [List] of [Coordinate]s where [Gem]s were found and calculates neighbor next to [Gem]s
     *
     * @return [List] of [Coordinate]s where a path can move an adjacent [Gem]
     */
    private fun findMoves() : List<Coordinate>{
        val game = root.currentGame
        checkNotNull(game)
        val result : MutableList<Coordinate> = mutableListOf()
        for (gemTile in gemTiles.distinct()){
            if (gemTile == Coordinate(0,0)){
                if (game.gameBoard.gameBoardTiles[Coordinate(-1,0)] == null) result.add(Coordinate(-1,0))
                if (game.gameBoard.gameBoardTiles[Coordinate(-1,1)] == null) result.add(Coordinate(-1,1))
                if (game.gameBoard.gameBoardTiles[Coordinate(0,1)] == null) result.add(Coordinate(0,1))
                if (game.gameBoard.gameBoardTiles[Coordinate(1,0)] == null) result.add(Coordinate(1,0))
                if (game.gameBoard.gameBoardTiles[Coordinate(1,-1)] == null) result.add(Coordinate(1,-1))
                if (game.gameBoard.gameBoardTiles[Coordinate(0,-1)] == null) result.add(Coordinate(0,-1))
            }else{
                for (gemPos in game.gameBoard.gameBoardTiles[gemTile]!!.gemEndPosition.keys){
                    println("[SimpleAI DEBUG] tile: $gemTile gems: " +
                            "${game.gameBoard.gameBoardTiles[gemTile]!!.gemEndPosition.size}")
                    result.add(findNeighbor(gemTile,gemPos))
                }
            }
        }
        return result.toList()
    }

    /**
     * function to find the [Coordinate] directly adjacent to the Gem
     *
     * @param gemTile [Coordinate] of [Tile] on which the [Gem] is located
     * @param gemPos Position of [Gem] on the [Tile] at [gemTile]
     * @return [Coordinate] of neighbor adjacent to the [Gem] at [gemTile]
     */
    private fun findNeighbor(gemTile: Coordinate, gemPos: Int): Coordinate{
        require(gemPos in 0..5)
        return when(gemPos){
            0 -> Coordinate(gemTile.row-1,gemTile.column)
            1 -> Coordinate(gemTile.row-1,gemTile.column+1)
            2 -> Coordinate(gemTile.row,gemTile.column+1)
            3 -> Coordinate(gemTile.row+1,gemTile.column)
            4 -> Coordinate(gemTile.row+1,gemTile.column-1)
            5 -> Coordinate(gemTile.row,gemTile.column-1)
            else -> gemTile     //cannot happen due to require() above
        }
    }

    /**
     * finds the [Tile.gemEndPosition] for each gem on the [gemTiles]
     *
     * @return [List] of [Int] representing the [Tile.gemEndPosition]
     */
    private fun findGemPositions(): List<Pair<Int,Gem>>{
        val game = root.currentGame
        checkNotNull(game)
        check(gemTiles.isNotEmpty())
        val result: MutableList<Pair<Int,Gem>> = mutableListOf()
        var middleGem = Gem(GemColor.EMERALD)
        for (i in gemTiles.distinct().indices) {
            if (gemTiles.distinct()[i] == Coordinate(0, 0)) {
                if (game.middleTile.gemPosition.size == 1) middleGem = Gem(GemColor.SAPPHIRE)
                if (game.gameBoard.gameBoardTiles[Coordinate(-1,0)] == null) result.add(Pair(0,middleGem))
                if (game.gameBoard.gameBoardTiles[Coordinate(-1,1)] == null) result.add(Pair(1,middleGem))
                if (game.gameBoard.gameBoardTiles[Coordinate(0,1)] == null) result.add(Pair(2,middleGem))
                if (game.gameBoard.gameBoardTiles[Coordinate(1,0)] == null) result.add(Pair(3,middleGem))
                if (game.gameBoard.gameBoardTiles[Coordinate(1,-1)] == null) result.add(Pair(4,middleGem))
                if (game.gameBoard.gameBoardTiles[Coordinate(0,-1)] == null) result.add(Pair(5,middleGem))
            }else{
                for (gem in game.gameBoard.gameBoardTiles[gemTiles.distinct()[i]]!!.gemEndPosition) { //Tile must exist
                    result.add(Pair(gem.key,gem.value))
                }
            }
        }
        return result.toList()
    }
    /**
     * searches the [GameBoard] for [Coordinate]s containing [Tile]s with [Gem]s
     *
     * @return [List] of [Coordinate]s that have [Tile]s containing [Gem]s
     */
    private fun findGems() : List<Coordinate>{
        val game = root.currentGame
        checkNotNull(game)
        val result : MutableList<Coordinate> = mutableListOf()
        if (root.currentGame!!.middleTile.gemPosition.isNotEmpty()){
            for (i in 0 until root.currentGame!!.middleTile.gemPosition.size){
                result.add(Coordinate(0,0))
            }
        }
        for (coordinate in game.gameBoard.gameBoardTiles.keys){
            if (game.gameBoard.gameBoardTiles[coordinate]?.gemEndPosition?.isNotEmpty() == true){
                for (i in 0 until game.gameBoard.gameBoardTiles[coordinate]!!.gemEndPosition.size){
                    result.add(coordinate)
                }
            }
        }
        return result.toList()
    }
    /**
     * finds the indices of gates owned partially or in full by the [CPUPlayer] associated with the [SimpleAI]
     *
     * @return [List] of [Int] corresponding to the indices of gates around the board
     */
    private fun findOwnGates() : List<Int>{
        val game = root.currentGame
        checkNotNull(game)
        val player = game.players[game.currentPlayerIndex]
        val result : MutableList<Int> = mutableListOf()
        for (i in 0..5){
            if (game.gameBoard.gateTokens[2*i].color == player.color ||
                game.gameBoard.gateTokens[2*i+1].color == player.color){
                result.add(i)
            }
        }
        return result.toList()
    }
}