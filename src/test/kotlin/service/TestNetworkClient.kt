package service

import edu.udo.cs.sopra.ntf.GameInitMessage
import edu.udo.cs.sopra.ntf.TilePlacedMessage
import service.network.IndigoNetworkClient
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.GameActionResponse
import tools.aqua.bgw.net.common.response.JoinGameResponse

/**
 * Custom implementation of a network client for testing purposes, extending IndigoNetworkClient.
 *
 * @param playerName The name of the player associated with the network client.
 * @param host The host address for the network connection.
 * @param secret The secret token for secure communication.
 * @param networkService The network service responsible for handling network interactions.
 */
class TestNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    networkService: TestNetworkService
) :
    IndigoNetworkClient(playerName, host, secret, networkService) {
    // Callbacks for various network events
    var onGameActionResponse: ((GameActionResponse) -> Unit)? = null
    var onCreateGameResponse: ((CreateGameResponse) -> Unit)? = null
    private var onInitMessage: ((GameInitMessage, String) -> Unit)? = null
    private var onPlayerJoined: ((PlayerJoinedNotification) -> Unit)? = null
    private var onPlayerLeft: ((PlayerLeftNotification) -> Unit)? = null
    private var onJoinedGameResponse: ((JoinGameResponse) -> Unit)? = null
    private var onTilePlacedReceived: ((TilePlacedMessage, String) -> Unit)? = null

    // Accessor for the TestNetworkService
    /*private val testNetworkService: TestNetworkService?
        get() = networkService as? TestNetworkService*/

    /**
     * Overrides the callback for handling CreateGameResponse events.
     * @param response The CreateGameResponse containing information about the outcome of  create game attempt.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        onCreateGameResponse?.invoke(response)
        super.onCreateGameResponse(response)
    }

    /**
     * Overrides the callback for handling JoinGameResponse events.
     * @param response The JoinGameResponse containing information about the outcome of the join game attempt.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        onJoinedGameResponse?.invoke(response)
        super.onJoinGameResponse(response)
    }

    /**
     * Overrides the callback for handling PlayerJoinedNotification events.
     * @param notification The PlayerJoinedNotification containing information about the joined player.
     */

    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        onPlayerJoined?.invoke(notification)
        super.onPlayerJoined(notification)
    }

    /**
     * Overrides the callback for handling PlayerLeftNotification events.
     * @param notification The PlayerLeftNotification containing information about the departed player.
     */

    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        onPlayerLeft?.invoke(notification)
        super.onPlayerLeft(notification)
    }

    /**
     * Overrides the callback for handling GameActionResponse events.
     * @param response The GameActionResponse containing information about the outcome of the game action.
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        onGameActionResponse?.invoke(response)
        super.onGameActionResponse(response)
    }

    /**
     * Overrides the callback for handling TilePlacedMessage events.
     * @param message The TilePlacedMessage containing information about the placed tile.
     * @param sender The identifier of the sender of the message.
     */
    @GameActionReceiver
    override fun onTilePlacedReceived(message: TilePlacedMessage, sender: String) {
        onTilePlacedReceived?.invoke(message, sender)
        super.onTilePlacedReceived(message, sender)
    }

    /**
     * Overrides the callback for handling GameInitMessage events.
     * @param message The GameInitMessage containing information about the game initialization.
     * @param sender The identifier of the sender of the message.
     */

    @GameActionReceiver
    override fun onInitReceivedMessage(message: GameInitMessage, sender: String) {
        onInitMessage?.invoke(message, sender)
        super.onInitReceivedMessage(message, sender)
    }

}