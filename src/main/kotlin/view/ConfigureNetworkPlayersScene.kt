package view

import entity.CPUPlayer
import entity.Player
import entity.TokenColor
import service.RootService
import service.network.ConnectionState
import view.components.*

import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.ImageVisual

/**
 * Scene for configuring network players before starting a new game.
 * @param indigoApp The IndigoApplication instance associated with the configuration scene.
 */
class ConfigureNetworkPlayersScene(val indigoApp: IndigoApplication) : BoardGameScene(
    1920, 1080, background = ImageVisual("plain_background.png")
), Refreshable {    // Title label for the scene

    private val label = Label(453, 21, 1050, 155, "Configure Players", 120)

    // Grid for displaying NetworkPlayersView for each game
    val grid = GridPane<NetworkPlayersView>(960, 484, 1, 1, 10, true)

    // Number of games minus one to get the correct index
    //private val size = games.size - 1

    // Button for adding a new player
    val addButton = Button(188, 806, 528, 207, "Add new player", 40).apply {
        onMouseClicked = { indigoApp.showMenuScene(indigoApp.newPlayerScene) }
        isDisabled = grid.rows == 4
    }

    // Button for starting a new game
    val startButton = Button(1217, 806, 528, 207, "Start new game", 40).apply {
        isDisabled = grid.rows < 2
        onMouseClicked = {
            //indigoApp.showGameScene(indigoApp.gameScene)
            val color = mutableListOf(TokenColor.BLUE, TokenColor.RED, TokenColor.WHITE, TokenColor.PURPLE)
            indigoApp.players = indigoApp.players.filterNotNull().toMutableList()
            println(indigoApp.players.toString())
            var sameColor = false
            for (i in 0 until indigoApp.players.size - 1) {
                println(indigoApp.players[i]?.color.toString() + indigoApp.players[i + 1]?.color.toString())
                if (indigoApp.players[i]?.color == indigoApp.players[i + 1]?.color) sameColor = true
            }
            if (sameColor) {
                val randomColorPlayers = indigoApp.players.map { players ->
                    val randomColor = color.random()
                    color.remove(randomColor)
                    when (players) {
                        is CPUPlayer -> CPUPlayer(
                            players.name,
                            players.age,
                            randomColor,
                            players.difficulty,
                            players.simulationSpeed
                        )

                        is Player -> Player(
                            players.name,
                            players.age,
                            randomColor,
                            players.isAI
                        )

                        else -> {
                            players
                        }

                    }
                }
                indigoApp.players = randomColorPlayers.toMutableList()
            }
            val players = indigoApp.players.filterNotNull().toMutableList()
            indigoApp.notSharedGates = true
            if (players.size == 4) indigoApp.notSharedGates = false
            if (players.size == 3) {
                indigoApp.showMenuScene(indigoApp.gatesScene)
            } else {
                if (indigoApp.aiGame) {
                    indigoApp.hideMenuScene()
                    indigoApp.showMenuScene(indigoApp.aiMenuScene)
                } else {
                    val notSharedGates = indigoApp.notSharedGates
                    val isRandom = indigoApp.isRandom
                    indigoApp.rootService.networkService.startNewHostedGame(players, notSharedGates, isRandom)
                }
            }
        }
    }


    private val backPfeil = BackPfeil().apply {
        onMouseClicked = {
            indigoApp.rootService.networkService.disconnect()
            indigoApp.showMenuScene(indigoApp.networkScene)
        }
    }

    /**
     * Initializes the ConfigureNetworkPlayersScene.
     */
    init {
        // Populate the grid with NetworkPlayersView instances

        //  for (i in 0.size) {
        grid[0, 0] = NetworkPlayersView().apply {
            this.button.onMouseClicked = { indigoApp.showMenuScene(indigoApp.configurePlayerXScene) }
        }
        //}
        // Add components to the scene
        addComponents(label, grid, addButton, startButton, backPfeil)
    }

    /**
     *  The function set the first row as the Host Player
     *
     *  @param sessionID give an sessionID  if is not Error from the server
     */
    override fun refreshAfterOnCreateGameResponse(sessionID: String?) {
        val networkService = indigoApp.rootService.networkService
        val connectionState = networkService.connectionState
        if (connectionState == ConnectionState.WAITING_FOR_GUEST) {
            val hostName = networkService.client!!.playerName
            indigoApp.players.add(
                Player(name = hostName, color = TokenColor.BLUE, isAI = indigoApp.aiGame)
            )
            grid[0, 0] = NetworkPlayersView().apply {
                label.text = "Player " + grid.rows + ": " + hostName
                this.button.onMouseClicked = {
                    indigoApp.showMenuScene(indigoApp.configurePlayerXScene)
                    indigoApp.configurePlayerXScene.playerName = hostName
                    indigoApp.configurePlayerXScene.currentRow = 1
                }
            }
        }
    }

    /**
     * Resets the settings by removing rows from the grid and updating button states.
     */
    fun resetSettings() {
        for (i in grid.rows - 1 downTo 1) {
            grid.removeRow(i)
        }
        startButton.isDisabled = true
        addButton.isDisabled = false
    }

    /**
     *  The Methode [refreshAfterPlayerJoined] added in the grid the new joined Player
     *
     *  @param newPlayerName is a String which contains the name of the new joined Player
     */
    override fun refreshAfterPlayerJoined(newPlayerName: String) {
        val currentRows = grid.rows
        if (currentRows < 4) {
            grid.addRows(currentRows)
            indigoApp.players.add(
                Player(name = newPlayerName, color = TokenColor.BLUE)
            )
            val newNetworkPlayer = NetworkPlayersView(0, 0).apply {
                label.text = "Player " + grid.rows + ": " + newPlayerName
                this.button.onMouseClicked = {
                    indigoApp.showMenuScene(indigoApp.configurePlayerXScene)
                    indigoApp.configurePlayerXScene.playerName = newPlayerName
                    indigoApp.configurePlayerXScene.currentRow = grid.rows
                }
                //label.posY = (151 * (currentRows - 1)).toDouble()
                //button.posY = (151 * (currentRows - 1)).toDouble()
            }
            grid[0, currentRows] = newNetworkPlayer
            startButton.isDisabled = grid.rows < 2
            addButton.isDisabled = grid.rows == 4
        } else {
            val rootService = RootService()
            val networkClient = rootService.networkService.client
            checkNotNull(networkClient)
            networkClient.otherPlayers.remove(newPlayerName)
        }
    }

    /**
     *  The Methode [refreshAfterPlayerJoined] remove the new left Player in the grid
     *
     *  @param playerLeftName is a String which contains the name of the left Player
     */
    override fun refreshAfterPlayerLeft(playerLeftName: String) {
        val connectionState = indigoApp.rootService.networkService.connectionState
        if (connectionState == ConnectionState.WAITING_FOR_GUEST) {
            for (i in 1 until grid.rows) {
                val networkPlayer = grid[0, i] ?: continue
                val name = networkPlayer.label.text.substringAfter(": ")
                if (name == playerLeftName) {
                    grid.removeRow(i)
                    break
                }
            }
            val removePlayer = indigoApp.players.find { it?.name == playerLeftName }
            indigoApp.players.remove(removePlayer)
            grid.removeEmptyRows()/*for (i in 0 until grid.rows) {
         val networkPlayer = grid.get(0, i) ?: continue
        networkPlayer.apply {
            posY = (151 * i).toDouble()
            label.posY = (151 * i).toDouble()
            button.posY = (151 * i).toDouble()
        }
        }*/
            startButton.isDisabled = grid.rows < 2
            addButton.isDisabled = grid.rows == 4
        }
    }

    /**
     * Refreshes the UI after starting a game, showing the game scene if the network connection is established.
     */
    override fun refreshAfterStartGame() {
        val networkService = indigoApp.rootService.networkService
        val connectionState = networkService.connectionState
        if (connectionState != ConnectionState.DISCONNECTED) {
            indigoApp.showGameScene(indigoApp.gameScene)
        }
    }
}
