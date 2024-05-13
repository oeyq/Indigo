package view

import entity.Coordinate
import entity.Gem
import entity.Tile
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus


/**
 * Interface for objects that can be refreshed in response to different game events.
 */
interface Refreshable {
    /**
     * Refreshes the state after starting a local game.
     */
    fun refreshAfterStartGame() {}

    /**
     * Refreshes the state after starting a network game.
     */

    fun refreshAfterStartNetGame() {}

    /**
     * Refreshes the state after restarting the game.
     */
    fun refreshAfterRestartGame() {}

    /**
     * Refreshes the state after the game has ended.
     */
    fun refreshAfterEndGame() {}

    /**
     * Refreshes the state after checking place for game tile.
     */
    fun refreshAfterCheckPlacement() {}

    /**
     * Refreshes the state after placing game tile.
     * @param coordinate The Coordinate where the tile has been placed.
     * @param tile The Tile that has been placed on the game board.
     */
    fun refreshAfterPlaceTile(coordinate: Coordinate, tile: Tile) {}

    /**
     * Refreshes the state after moving gems.
     * @param gem The Gem that has been moved.
     * @param coordinate The target Coordinate where the gem is moved to.
     * @param exit The exit index indicating the direction of the movement.
     */
    fun refreshAfterMoveGems(gem: Gem, coordinate: Coordinate, exit: Int) {}

    /**
     * Refreshes the state after the player win the gem, and then the gem will be removed from game board.
     * @param gem The Gem that has been removed.
     */
    fun refreshAfterRemoveGems(gem: Gem) {}

    /**
     * Refreshes the state after a collision in the game.
     * @param gem1 The first Gem involved in the collision.
     * @param gem2 The second Gem involved in the collision.
     */
    fun refreshAfterCollision(gem1: Gem,gem2: Gem) {}

    /**
     * Refreshes the state after left rotating tile.
     * @param currentPlayerIndex The index of the current player who initiated the rotation.
     */
    fun refreshAfterLeftRotation(currentPlayerIndex: Int) {}

    /**
     * Refreshes the state after right rotating tile.
     * @param currentPlayerIndex The index of the current player who initiated the rotation.
     */
    fun refreshAfterRightRotation(currentPlayerIndex: Int) {}

    /**
     * Refreshes the state after redoing a game action.
     */
    fun refreshAfterRedo() {}

    /**
     * Refreshes the state after undoing a game action.
     */
    fun refreshAfterUndo() {}

    /**
     * Refreshes the state after saving the game.
     */
    fun refreshAfterSaveGame() {}

    /**
     * Refreshes the state after loading a saved game.
     */
    fun refreshAfterLoadGame() {}

    /**
     * Refreshes the state after the AI takes its turn.
     */
    fun refreshAfterAITurn() {}

    /**
     * Refreshes the state after changing the active player.
     */
    fun refreshAfterChangePlayer() {}

    /**
     * Refreshes the state after distributing a new tile.
     */
    fun refreshAfterDistributeNewTile() {}

    /**
     *  The function refreshes the view layer after a new Player joined the game for
     *  the host.
     * @param newPlayerName The name of the new player who joined.
     */
    fun refreshAfterPlayerJoined(newPlayerName: String) {}

    /**
     *  The function refreshes the view layer after a Player leaved the game for
     *  the host.
     * @param playerLeftName The name of the player who left.
     */
    fun refreshAfterPlayerLeft(playerLeftName: String) {}

    /**
     *  the function updated the view layer after received the tilePlaceMessage
     */
    fun refreshAfterStartNewJoinedGame() {}

    /**
     * The function updated the view layer after joining a Game
     */
    fun refreshAfterJoinGame() {}

    /**
     * The function updated the view Layer after a host Game started
     */
    fun refreshAfterHostGame() {}

    /**
     * After receiving a Game Response update the view Layer
     * @param sessionID The session ID associated with the created game, or null if creation failed.
     */
    fun refreshAfterOnCreateGameResponse(sessionID: String?) {}

    /**
     *  update the Gui with the status
     * @param responseStatus The JoinGameResponseStatus indicating the outcome of the join game attempt.
     */
    fun refreshAfterOnJoinGameResponse(responseStatus: JoinGameResponseStatus) {}

    /**
     *  updated the gui with corrected button showing
     */
    fun refreshAfterNetworkPlayerTurn() {}

    /**
     * updated give the gui the correct handTile for variable PlaceTile
     * @param rotation The rotation value of the received tile.
     */
    fun refreshAfterReceivedTile(rotation: Int) {}
}