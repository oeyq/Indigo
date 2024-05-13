package service.ai

import entity.*
import service.RootService

/**
 * Represents a node in the Monte Carlo Tree Search algorithm for game decision-making.
 *
 * @property rootService The RootService instance containing game-related information.
 * @property parent The parent node in the search tree (null if the node is the root).
 * @property coordinate The [Coordinate] where the move that leads from the parent node to this node was made.
 */

data class Node(val rootService: RootService, val parent: Node?, val coordinate: Coordinate) {

    // List to store child nodes of this node
    val children: MutableList<Node> = mutableListOf()

    // Variables to track statistics for the MCTS algorithm
    var winCount = 0.0
    var visitCount = 0.0

    // The game state associated with this node
    var state: Indigo =
        // If there is a parent, apply the move associated with this node to derive the new game state
        if (parent != null) ServiceAi.doMove(parent.state, coordinate)
        // If this is the root node, initialize the state from the current game in the root service
        else rootService.currentGame!!.copyTo()

    // Initialize the current player index if this is the root node
    init {
        if (parent == null) state.currentPlayerIndex = rootService.currentGame!!.currentPlayerIndex
    }

    /**
     * Returns a list of possible moves in this node's state.
     **
     * @return List of [Coordinate] objects representing the possible moves in this node's state
     */
    fun getPossibleMoves(): MutableList<Coordinate> {
        val availableMoves: MutableList<Coordinate> = mutableListOf()
        if (state.players[state.currentPlayerIndex].handTile == null) return availableMoves

        // If the current player has no hand tile, return an empty list of moves
        val playerTile = state.players[state.currentPlayerIndex].handTile

        // Iterate over the game board and find available moves
        for (row in -4..4) {
            for (col in Integer.max(-4, -row - 4)..Integer.min(4, -row + 4)) {
                val coordinate = Coordinate(row, col)
                // Check if placing the tile at the coordinate is a valid move
                if (GameServiceAi(state).checkPlacement(
                        coordinate,
                        playerTile!!,
                        true
                    )
                ) { //even when it blocks an exit it returns true because we ll rotate the tile later
                    availableMoves.add(Coordinate(row, col))
                }
            }
        }
        return availableMoves
    }

}
