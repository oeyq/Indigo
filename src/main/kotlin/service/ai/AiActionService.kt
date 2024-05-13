package service.ai

import service.AbstractRefreshingService
import service.RootService
/**
 * The `AiActionService` class represents a service for handling AI actions in a game.
 *
 * @property rootService The root service that provides essential functionality for the AI actions.
 */
class AiActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Initiates an AI move based on the specified difficulty level.
     *
     * @param difficulty The difficulty level of the AI move. Supported values: "easy", "medium".
     */
    fun aiMove(difficulty: String) {
        when (difficulty) {
            "easy" -> RandomAI(rootService).makeMove()
            "advanced" -> SimpleAI(rootService).makeMove()
            "medium" -> MCTS(rootService, rootService.currentGame!!.currentPlayerIndex).makeMove() //still needs fixes
        }
    }
}
