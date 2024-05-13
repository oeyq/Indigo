package service.network

import edu.udo.cs.sopra.ntf.GameInitMessage
import edu.udo.cs.sopra.ntf.TilePlacedMessage
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.response.*
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification


/**
 * The IndigoNetworkClient class represents a network client for the Indigo game.
 *
 * @param playerName The name of the player using the network client.
 * @param host The host address for connecting to the game server.
 * @param secret The secret for the project to establish a secure connection.
 * @param networkService The network service of the game to use the functions of the class.
 */
open class IndigoNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    var networkService: NetworkService,
) : BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {
    /**
     * @property sessionID The SessionID is to connect to the specific game
     */
    var sessionID: String? = null

    //List of all player which are participate the game
    var otherPlayers = mutableListOf<String>()


    /**
     * The function [onCreateGameResponse] is getting from the server a message
     * to create a game as a host.
     *
     * @param response The response which you are allowed to create a game
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.HOST_WAITING_FOR_CONFIRMATION)
            { "unexpected CreateGameResponse" }

            when (response.status) {
                CreateGameResponseStatus.SUCCESS -> {
                    println("Success")
                    networkService.connectionState = ConnectionState.WAITING_FOR_GUEST
                    sessionID = response.sessionID
                }

                else -> {
                    networkService.disconnect()
                }
            }
            networkService.refreshAfterOnCreateGameResponse(response.sessionID)
        }
    }

    /**
     * The function [onJoinGameResponse] is getting from the server a message
     * to create a game as a host.
     *
     * @param response The response which the guest receive a JoinGameMessage
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.GUEST_WAITING_FOR_CONFIRMATION)
            { "unexpected JoinGameResponse" }

            when (response.status) {
                JoinGameResponseStatus.SUCCESS -> {
                    println("Join Game Success")
                    otherPlayers = response.opponents.toMutableList()
                    sessionID = response.sessionID
                    networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
                }

                else -> {
                    networkService.disconnect()
                }
            }
            networkService.refreshAfterOnJoinGameResponse(response.status)
        }
    }

    /**
     *  The function [onPlayerJoined] get a notification if a new player joined the game
     *
     *  @param notification the notification of new Player
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        BoardGameApplication.runOnGUIThread {
            check(
                networkService.connectionState == ConnectionState.WAITING_FOR_GUEST
                        || networkService.connectionState == ConnectionState.WAITING_FOR_INIT
            )
            { "not awaiting any guests." }

            otherPlayers.add(notification.sender)
            networkService.refreshAfterPlayerJoined(notification.sender)
        }
    }

    /**
     *  The function [onPlayerLeft] get a notification if a player left the game
     *
     *  @param notification the notification of new Player
     */
    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        BoardGameApplication.runOnGUIThread {
            check(
                networkService.connectionState == ConnectionState.WAITING_FOR_GUEST
                        || networkService.connectionState == ConnectionState.WAITING_FOR_INIT
            )
            { "Player should not leave" }

            otherPlayers.remove(notification.sender)
            networkService.refreshAfterPlayerLeft(notification.sender)
        }
    }

    /**
     * The function [onTilePlacedReceived] gets the message of the Server
     * if a new tile was placed by another Player
     *
     * @param message The message with information of the tile placing
     * @param sender The player who sent the message
     */
    @Suppress("unused")
    @GameActionReceiver
    open fun onTilePlacedReceived(message: TilePlacedMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receivedTilePLacedMessage(message)
        }
    }

    /**
     * The function [onGameActionResponse] is used to get GameActions from another player
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        BoardGameApplication.runOnGUIThread {
            check(
                networkService.connectionState == ConnectionState.PLAYING_MY_TURN ||
                        networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENTS_TURN
            )
            { "not currently playing in a network game." }
        }
        when (response.status) {
            GameActionResponseStatus.SUCCESS -> {
                println("SUCCESS")
            }

            else -> {
                println("Fail")
            }
        }
    }

    /**
     *  The function [onInitReceivedMessage] is to receive a [GameInitMessage] from the server
     *  to set the game
     */
    @Suppress("unused")
    @GameActionReceiver
    open fun onInitReceivedMessage(message: GameInitMessage, sender: String) {

        BoardGameApplication.runOnGUIThread {
            for (player in message.players) {
                if ((!otherPlayers.contains(player.name)) && (player.name != playerName)) {
                    otherPlayers.add(player.name)
                }
            }
            networkService.startNewJoinedGame(
                message = message,
                playerName
            )
        }
    }
}