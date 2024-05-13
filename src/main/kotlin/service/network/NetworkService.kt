package service.network

import edu.udo.cs.sopra.ntf.GameInitMessage
import edu.udo.cs.sopra.ntf.TilePlacedMessage
import entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import service.AbstractRefreshingService
import service.RootService
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus

/**
 *  The class [NetworkService] is to have all function  with the network for online gaming.
 *
 *  @property rootService the rootService to have the information of the current game
 */
open class NetworkService(private val rootService: RootService) : AbstractRefreshingService() {
    companion object {
        /** URL of the BGW Net to play at SoPra23d */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Indigo"
    }

    /**
     *  The connection begins with disconnected
     */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    /**
     *  The client if we are the client
     */
    var client: IndigoNetworkClient? = null


    /**
     *  The function [disconnect] is to disconnect the server
     *  if we leave an online Game
     */
    fun disconnect() {
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null

        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     *  The function[hostGame] is to start a Game as a Host
     *
     *  @param secret The secret to make a secure connection
     *  @param name Name of the host
     *  @param  sessionID Write a sessionID if you want else  you get one from the server
     */
    fun hostGame(secret: String = "game23d", name: String, sessionID: String? = null) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }
        if (sessionID.isNullOrBlank()) {
            client?.createGame(GAME_ID, "Welcome!")
        } else {
            client?.createGame(GAME_ID, sessionID, "Welcome!")
        }
        updateConnectionState(ConnectionState.HOST_WAITING_FOR_CONFIRMATION)
        onAllRefreshables { refreshAfterHostGame() }
    }

    /**
     *  The function[joinGame] join a Game as a client
     *
     *  @param secret The secret to make a secure connection
     *  @param name Name of the host
     *  @param  sessionID The sessionID of the Game you want to join
     */
    fun joinGame(secret: String = "game23d", name: String, sessionID: String) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }
        client?.joinGame(sessionID, "Hello!")
        updateConnectionState(ConnectionState.GUEST_WAITING_FOR_CONFIRMATION)
        onAllRefreshables { refreshAfterJoinGame() }
    }

    /**
     * The function [startNewHostedGame] start a new HostGame
     * and configure all others player
     *
     */
    fun startNewHostedGame(
        players: MutableList<Player>,
        notSharedGates: Boolean = false,
        random: Boolean = false
    ) {
        val client = client
        checkNotNull(client)
        check(connectionState == ConnectionState.WAITING_FOR_GUEST)
        { "currently not prepared to start a new hosted game." }
        rootService.gameService.startGame(players, notSharedGates, random)
        sendGameInitMessage()
        onAllRefreshables {
            refreshAfterChangePlayer()
            refreshAfterNetworkPlayerTurn() }
        val currentPlayerIndex = rootService.currentGame!!.currentPlayerIndex
        when (val currentPlayer = players[currentPlayerIndex]) {
            is CPUPlayer -> {
                runBlocking {
                    CoroutineScope(Dispatchers.JavaFx).launch {
                        try {
                            withTimeout(8000) {
                                delay((4000/currentPlayer.simulationSpeed ).toLong())
                                rootService.aiActionService.aiMove(currentPlayer.difficulty)
                            }
                        } catch (e: TimeoutCancellationException) {
                            println(e.message)
                            rootService.aiActionService.aiMove("easy")
                        }
                    }
                }
            }
        }
    }


    /**
     * The function [startNewJoinedGame] start a new game by joining it.
     *
     * @param message The message you are getting from the host
     * to initialize the game from your game
     */
    fun startNewJoinedGame(message: GameInitMessage, playerName: String) {
        check(connectionState == ConnectionState.WAITING_FOR_INIT) { "not waiting for game init message. " }
        val routeTiles = rootService.networkMappingService.toRouteTiles(message.tileList)
        val players = rootService.networkMappingService.toEntityPlayer(message.players)
        if (players[0].name == playerName) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)
        }
        val setting = GameSettings(players)

        val allTiles = mutableListOf(
            Tile(listOf(Pair(Edge.TWO, Edge.FOUR)), TileType.Type_5, mutableMapOf(Pair(3, Gem(GemColor.AMBER)))),
            Tile(listOf(Pair(Edge.THREE, Edge.FIVE)), TileType.Type_5, mutableMapOf(Pair(4, Gem(GemColor.AMBER)))),
            Tile(listOf(Pair(Edge.ZERO, Edge.FOUR)), TileType.Type_5, mutableMapOf(Pair(5, Gem(GemColor.AMBER)))),
            Tile(listOf(Pair(Edge.ONE, Edge.FIVE)), TileType.Type_5, mutableMapOf(Pair(0, Gem(GemColor.AMBER)))),
            Tile(listOf(Pair(Edge.ZERO, Edge.TWO)), TileType.Type_5, mutableMapOf(Pair(1, Gem(GemColor.AMBER)))),
            Tile(listOf(Pair(Edge.ONE, Edge.THREE)), TileType.Type_5, mutableMapOf(Pair(2, Gem(GemColor.AMBER)))),
        )
        allTiles.addAll(routeTiles)

        val gateTokens = rootService.networkMappingService.toGateTokens(players, message.gameMode)
        val gameService = rootService.gameService
        rootService.currentGame = Indigo(
            setting,
            GameBoard(),
            allTiles.toList(),
            gameService.initializeGems(),
            gameService.initializeTokens()
        )
        rootService.currentGame?.gameBoard?.gateTokens = gateTokens
        val listCoordinate = listOf(
            Coordinate(-4, 0),
            Coordinate(-4, 4),
            Coordinate(0, 4),
            Coordinate(4, 0),
            Coordinate(4, -4),
            Coordinate(0, -4),
        )
        for (i in listCoordinate.indices) {
            val coordinate = listCoordinate[i]
            rootService.currentGame!!.gameBoard.gameBoardTiles[coordinate] = allTiles[i]
        }
        repeat(players.size) {
            gameService.distributeNewTile()
            gameService.changePlayer()
        }
        onAllRefreshables {
            refreshAfterStartNewJoinedGame()
            refreshAfterNetworkPlayerTurn()
        }

    }

    /**
     *  The class [sendGameInitMessage] the class send an initMessage
     *  from the currentGame after initialized a new game.
     *
     */
    fun sendGameInitMessage() {
        val networkPlayers = rootService.networkMappingService.toNetworkPlayer()
        val gameMode = rootService.networkMappingService.toGameMode()
        val tileList = rootService.networkMappingService.toTileTypeList()
        println(networkPlayers.toString())
        println(gameMode)
        println(tileList.size.toString())

        val message = GameInitMessage(
            networkPlayers, gameMode, tileList
        )
        client?.sendGameActionMessage(message)
        updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        for (otherPlayer in client?.otherPlayers!!) {
            if (networkPlayers[0].name == otherPlayer) {
                updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)
            }
        }
    }


    /**
     * The function [sendPlacedTile] make the placedTile to a message to send
     * to other online Player
     *
     * @param placedTile PlacedTile is the tile you choose to placed
     * @param coordinate Coordinate is where the tile placed
     */
    fun sendPlacedTile(placedTile: Tile, coordinate: Coordinate) {
        check(connectionState == ConnectionState.PLAYING_MY_TURN) { "not my turn" }
        val rotation = placedTile.edges.indexOf(Edge.ZERO)
        val qCoordinate = coordinate.column
        val rCoordinate = coordinate.row
        val message = TilePlacedMessage(
            rotation, qCoordinate, rCoordinate
        )
        client?.sendGameActionMessage(message)
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        val currentPlayerIndex = currentGame.currentPlayerIndex
        updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        for (otherPlayer in client?.otherPlayers!!) {
            if (currentGame.players[currentPlayerIndex].name == otherPlayer) {
                updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)
            }
        }
        onAllRefreshables { refreshAfterChangePlayer() }
        onAllRefreshables { refreshAfterNetworkPlayerTurn() }
    }

    /**
     * The function [receivedTilePLacedMessage] make the received message
     * to an Action in the Indigo game
     *
     * @param message The message is from the other player in the network mode
     * which have the information for the tile coordinate und rotation
     */
    fun receivedTilePLacedMessage(message: TilePlacedMessage) {
        check(
            connectionState == ConnectionState.WAITING_FOR_OPPONENTS_TURN
        ) { "currently not expecting an opponent's turn." }
        val currentGame = rootService.currentGame
        checkNotNull(currentGame)
        val players = currentGame.players
        var currentPlayerIndex = currentGame.currentPlayerIndex
        val handTile = players[currentPlayerIndex].handTile
        checkNotNull(handTile)
        val rotation = message.rotation
        repeat(rotation) {
            rootService.playerTurnService.rotateTileRight(handTile)
        }
        val space = Coordinate(message.rcoordinate, message.qcoordinate)
        onAllRefreshables { refreshAfterReceivedTile(rotation) }
        rootService.playerTurnService.placeRouteTile(space, handTile)
        currentPlayerIndex = rootService.currentGame!!.currentPlayerIndex
        updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        if (client?.otherPlayers!!.contains(currentGame.players[currentPlayerIndex].name)) {
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENTS_TURN)
        }
        onAllRefreshables {
            refreshAfterChangePlayer()
            refreshAfterNetworkPlayerTurn()
        }
        val currentPlayer = currentGame.players[currentGame.currentPlayerIndex]
        if (currentPlayer.isAI) {
            when (currentPlayer) {
                is CPUPlayer -> {
                    runBlocking {
                        CoroutineScope(Dispatchers.JavaFx).launch {
                            try {
                                withTimeout(8000) {
                                    delay((4000/currentPlayer.simulationSpeed).toLong())
                                    rootService.aiActionService.aiMove(currentPlayer.difficulty)
                                }
                            } catch (e: TimeoutCancellationException) {
                                println(e.message)
                                rootService.aiActionService.aiMove("easy")
                            }
                        }
                    }
                }
            }
        }
        println(connectionState.toString())
    }

    /**
     *  The private fun [connect] is to make a connection to the server
     *
     *  @param secret The secret to make a secure connection  to the Server
     *  @param name The name of the player
     */
    open fun connect(secret: String = "game23d", name: String): Boolean {
        require(connectionState == ConnectionState.DISCONNECTED && client == null) { "already connected to another game" }
        require(name.isNotBlank()) { "player name must be given" }
        val newClient = IndigoNetworkClient(
            playerName = name, host = SERVER_ADDRESS, secret = secret, networkService = this
        )
        return if (newClient.connect()) {
            this.client = newClient
            updateConnectionState(ConnectionState.CONNECTED)
            true
        } else {
            false
        }
    }

    /**
     * The function [updateConnectionState] updated the State machine
     *
     * @param newState The state which the function update
     */
    fun updateConnectionState(newState: ConnectionState) {
        this.connectionState = newState
    }

    /**
     * The function trigger the refresh function for refreshAfterPlayerJoined
     */
    fun refreshAfterPlayerJoined(playerJoinedName: String) {
        onAllRefreshables { refreshAfterPlayerJoined(playerJoinedName) }
    }

    /**
     * The function trigger the refresh function for refreshAfterPlayerJoined
     */
    fun refreshAfterPlayerLeft(playerLeftName: String) {
        onAllRefreshables { refreshAfterPlayerLeft(playerLeftName) }
    }

    /**
     *  The function trigger the [refreshAfterOnCreateGameResponse] function
     */
    fun refreshAfterOnCreateGameResponse(sessionID: String?) {
        onAllRefreshables { refreshAfterOnCreateGameResponse(sessionID) }
    }

    /**
     *  The function trigger the [refreshAfterOnCreateGameResponse] function
     */
    fun refreshAfterOnJoinGameResponse(responseStatus: JoinGameResponseStatus) {
        onAllRefreshables { refreshAfterOnJoinGameResponse(responseStatus) }
    }
}