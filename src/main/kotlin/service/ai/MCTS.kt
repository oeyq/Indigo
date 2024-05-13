package service.ai

import entity.Coordinate
import service.PlayerTurnService
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Class representing the Monte Carlo Tree Search (MCTS) algorithm for decision-making in a game.
 *
 * @param rootService The RootService providing access to the game state and functionalities.
 * @param aiIndex The index of the AI player using the MCTS algorithm.
 */
class MCTS(private val rootService: service.RootService, private val aiIndex: Int) {

    /**
     *
     * This method initiates the MCTS algorithm to find the next best move for the AI player.
     * It starts with a root node and iteratively performs selection, expansion, simulation,
     * and backpropagation
     * until a stopping condition is met: "game is over" or "
     * If there are no more possible moves to explore from the current node"
     *
     * @return A [Coordinate] object representing the best move for the AI player.
     */
    fun findNextMove(): Coordinate {
        val defaultMove = Coordinate(-1, -1)
        val root = Node(rootService, null, defaultMove)
        var terminateCondition = false

        while (true) {
            val node = selectPromisingNode(root)

            if (ServiceAi.isGameOver(node.state) || terminateCondition) {
                backpropagation(node, true)
                return node.coordinate
            }

            terminateCondition = expandNode(node)
            val nodeToExplore = selectPromisingNode(node)
            val aiWon = simulateRandomPlayout(nodeToExplore, 1000)
            backpropagation(nodeToExplore, aiWon)

        }
    }


    /**
     * A simplified version of findNextMove that doesn't go through all the tree.
     * It performs a limited number of iterations to make a quick decision.
     *
     * @param maxIterations The maximum number of iterations to perform.
     * @return A [Coordinate] object representing the best move for the AI player.
     */


    private fun findNextMoveLimited(maxIterations: Int): Coordinate? {
        val defaultMove = Coordinate(-1, -1)
        val root = Node(rootService, null, defaultMove)

        repeat(maxIterations) {
            val node = selectPromisingNode(root)
            if (ServiceAi.isGameOver(node.state)) {
                return node.coordinate
            }
            expandNode(node)
            val nodeToExplore = selectPromisingNode(node)
            simulateRandomPlayout(nodeToExplore, 200)
            backpropagation(nodeToExplore, true)
        }

        val bestMove = root.children.maxByOrNull { it.winCount }?.coordinate
        return bestMove.takeIf { it != null && it != Coordinate(-1, -1) }
    }


    /**
     * selectPromisingNode - a function that selects the most promising node to be expanded
     * It traverses the tree to find the most promising node based on the UCT
     * (Upper Confidence Bound applied to Trees) formula
     *
     * @param node: the current node
     * @return Node: the most promising node
     */
    private fun selectPromisingNode(node: Node): Node {
        var current = node

        while (current.children.isNotEmpty()) {

            // Calculate UCT values for children
            val uctValues = current.children.map { child ->
                if (child.visitCount != 0.0)
                    child.winCount / child.visitCount + sqrt(2.0 * ln(current.visitCount) / child.visitCount)
                else
                    Double.POSITIVE_INFINITY
            }

            // Find the index of the child with the maximum UCT value
            // movement next node  but not the game state
            val selectedChildIndex = uctValues.indexOf(uctValues.maxOrNull())

            // Move to the child with the maximum UCT value
            current = current.children[selectedChildIndex]
        }

        println("Selected promising node: $current")
        return current
    }

    /**
     * expandNode - a function that expands the given node by adding its possible moves as children
     *
     * @param node: the node to be expanded
     * @return Boolean: returns true if there are no possible moves
     * (i.e., the node is a terminal state)
     * false otherwise
     */

    private fun expandNode(node: Node): Boolean {
        node.getPossibleMoves().forEach {
            val child = Node(rootService, node, it)
            node.children.add(child)
        }
        node.children.shuffle()
        return node.children.isEmpty()
    }

    /**
     * simulateRandomPlayout - a function that simulates a random playout from the given node
     * @param node: the starting node for the simulation
     * @return Boolean: returns true if the AI won the simulation, false otherwise
     */
    private fun simulateRandomPlayout(node: Node, maxSimulations: Int): Boolean {
        val aiScores = mutableListOf<Int>() // Array to store AI player scores
        var tempNode = node.copy()
        var stop = false
        var simulations = 0

        while (!ServiceAi.isGameOver(tempNode.state) && !stop && simulations < maxSimulations) {
            stop = expandNode(tempNode)
            tempNode = selectPromisingNode(tempNode)
            simulations++
            println("simulations $simulations")

            // Record the AI player's score after each simulation
            val aiPlayerScore = tempNode.state.players[aiIndex].score
            aiScores.add(aiPlayerScore)
        }

        // Determine if the AI won based on the maximum score achieved
        val maxAiScore = aiScores.maxOrNull() ?: 0

        // When we add the simulation of the opponent it should be later be
        // return tempNode.state.players[aiIndex].score >= tempNode.state.players.maxOf { it.score }

        return tempNode.state.players[aiIndex].score >= maxAiScore
    }

    /**
     * backpropagation the result of the simulation up the tree to update the statistics
     * of each node.
     *
     * @param last Node at the end of the simulation
     * @param aiWon Boolean indicating if the AI won the simulation
     */
    private fun backpropagation(last: Node, aiWon: Boolean) {
        // It starts from the last node (end of the simulation) and moves up the tree until the root node is reached.
        var node: Node? = last
        while (node != null) {
            node.visitCount += 1
            if (aiWon) node.winCount += 1
            node = node.parent
        }
    }

    /**
     * Initiates the AI player's move by finding the next move.
     * and then placing the selected route tile on the game board.
     */

    fun makeMove() {
        // Choose how many Iterations
        val resultCoordinate = findNextMoveLimited(500)
        println("resultatCoordinate $resultCoordinate")
        PlayerTurnService(rootService).placeRouteTile(
            resultCoordinate!!,
            rootService.currentGame!!.players[aiIndex].handTile!!,
            true
        )
        println("TilePlaced ")

    }
}
