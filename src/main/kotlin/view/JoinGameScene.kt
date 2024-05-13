package view

import entity.CPUPlayer
import entity.Player
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import service.network.ConnectionState
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus
import tools.aqua.bgw.visual.ImageVisual
import view.components.BackPfeil
import view.components.Button
import view.components.Label

/**
 * Represents the join game menu scene.
 *
 * This scene when a player enters the option to join a game in the scene before ("SzenenName").
 *
 * The layout and design of these components are defined in this class.
 * @param indigoApp The IndigoApplication instance associated with the join game scene.
 */
class JoinGameScene(val indigoApp: IndigoApplication) : MenuScene(990, 1080), Refreshable {
    //private val game = rootService.currentGame

    private val rootService = indigoApp.rootService

    var difficulty = "easy"
    var simulationSpeed = 0

    //irgendwie noch an zu bearbeitenden Spieler drankommen jetzt noch X
    private val titleLabel = Label(42, 80, 900, 116, "Configure Player", 96)

    private val nameLabel = Label(80, 370, width = 300, text = "Name : ", fontSize = 48)
    private val nameInput: TextField = TextField(width = 420, height = 69, posX = 320, posY = 370).apply {
        onKeyTyped = {
            joinButton.isDisabled = this.text.isBlank() || idInput.text.isBlank()
        }
    }

    private val idLabel = Label(80, 535, width = 300, text = "Session ID : ", fontSize = 40)
    private val idInput: TextField = TextField(width = 420, height = 69, posX = 320, posY = 535).apply {
        onKeyTyped = {
            joinButton.isDisabled = this.text.isBlank() || nameInput.text.isBlank()
        }
    }

    private val joinButton = Button(247, 800, 528, 207, "Join", 48).apply {
        isDisabled = nameInput.text.isBlank() || idInput.text.isBlank()
        onMouseClicked = {
            if (yesButton.isSelected) {
                indigoApp.aiGame = true
                indigoApp.hideMenuScene()
                indigoApp.showMenuScene(indigoApp.aiMenuScene)
            } else {
                indigoApp.rootService.networkService.joinGame(
                    name = nameInput.text, sessionID = idInput.text
                )
            }
        }
    }

    private val aiLabel = Label(80, 700, width = 300, text = "AI : ", fontSize = 48)

    private val yesLabel = Label(370, 700, width = 80, text = "yes", fontSize = 48)
    private val noLabel = Label(670, 700, width = 80, text = "no", fontSize = 48)

    private val toggleGroup = ToggleGroup()
    private val yesButton = RadioButton(posX = 320, posY = 700, toggleGroup = toggleGroup)
    private val noButton = RadioButton(posX = 620, posY = 700, toggleGroup = toggleGroup)

    private val textMessageLabel = Label(
        15, 320, 960, 480, "Waiting for Confirmation", 48
    ).apply {
        visual = ImageVisual("message.png")
        isVisible = false
        isDisabled = true
    }

    private val backPfeil = BackPfeil(60, 40, 70, 60).apply {
        onMouseClicked = {
            reset()
            indigoApp.rootService.networkService.disconnect()
            indigoApp.showMenuScene(indigoApp.networkScene)
        }
    }

    //Initializes the JoinGameScene with default values and sets up UI components.
    init {
        // Set the initial opacity of the scene
        opacity = 0.7
        // Add components to the scene
        addComponents(
            titleLabel, idLabel, nameLabel, aiLabel, nameInput,
            idInput, yesButton, noButton, yesLabel, noLabel,
            joinButton, textMessageLabel, backPfeil
        )

        // Set alignment for specific labels
        nameLabel.alignment = Alignment.CENTER_LEFT
        aiLabel.alignment = Alignment.CENTER_LEFT
        idLabel.alignment = Alignment.CENTER_LEFT
    }

    /**
     * Refreshes the scene after joining a game.
     */
    override fun refreshAfterJoinGame() {
        textMessageLabel.isVisible = true
        textMessageLabel.isDisabled = false
        textMessageLabel.text = rootService.networkService.connectionState.name
    }

    /**
     * Refreshes the scene after receiving a response from joining a game.
     *
     * @param responseStatus The JoinGameResponseStatus indicating the outcome of the join game attempt.
     */
    override fun refreshAfterOnJoinGameResponse(responseStatus: JoinGameResponseStatus) {
        textMessageLabel.isVisible = true
        textMessageLabel.isDisabled = false
        when (responseStatus) {
            JoinGameResponseStatus.SUCCESS -> {
                textMessageLabel.text = "Waiting for Host to finish \n Game Configuration"
                joinButton.isDisabled = true
            }

            JoinGameResponseStatus.ALREADY_ASSOCIATED_WITH_GAME -> {
                textMessageLabel.text = "Already connected to the Game"
            }

            JoinGameResponseStatus.INVALID_SESSION_ID -> {
                textMessageLabel.text = responseStatus.name + "\n" + "try another Session ID"
            }

            JoinGameResponseStatus.PLAYER_NAME_ALREADY_TAKEN -> {
                textMessageLabel.text =
                    responseStatus.name + "\n" + "try another Player Name"
            }

            else -> {
                textMessageLabel.text = "Another failure"
            }
        }
        playAnimation(DelayAnimation(duration = 2000).apply {
            onFinished = {
                if (responseStatus != JoinGameResponseStatus.SUCCESS) {
                    textMessageLabel.isVisible = false
                    textMessageLabel.isDisabled = true
                    joinButton.isDisabled = false
                }
            }
        })
    }

    /**
     * Refreshes the scene after starting a new game that has been joined.
     */
    override fun refreshAfterStartNewJoinedGame() {
        if (indigoApp.aiGame) {
            val currentGame = indigoApp.rootService.currentGame
            checkNotNull(currentGame)
            val player = currentGame.players.find { it.name == nameInput.text }
            val playerIndex = currentGame.players.indexOf(player)
            val cpuPlayer = CPUPlayer(player!!.name, player.age, player.color, difficulty, simulationSpeed).apply {
                handTile = player.handTile
                score = player.score
                collectedGems = player.collectedGems.toMutableList()
            }
            val newPlayers = mutableListOf<Player>()
            for (i in currentGame.players.indices) {
                if (i == playerIndex) {
                    newPlayers.add(cpuPlayer)
                } else {
                    newPlayers.add(currentGame.players[i])
                }
            }
            currentGame.settings.players = newPlayers.toList()
            currentGame.players = newPlayers.toList()
        }
        reset()
        indigoApp.showGameScene(indigoApp.gameScene)
        indigoApp.hideMenuScene()
        indigoApp.gameScene.refreshAfterStartGame()
        indigoApp.gameScene.refreshAfterNetworkPlayerTurn()
        val players = rootService.currentGame!!.players
        val currentPlayerIndex = rootService.currentGame!!.currentPlayerIndex
        val currentPlayer = players[currentPlayerIndex]
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
    }

    /**
     * Refreshes the scene after a connection was broken because a host left
     * @param [playerLeftName] is name of the host that disconnected
     */
    override fun refreshAfterPlayerLeft(playerLeftName: String) {
        val otherPlayer = indigoApp.rootService.networkService.client!!.otherPlayers
        val connectionState = indigoApp.rootService.networkService.connectionState
        if (connectionState == ConnectionState.WAITING_FOR_INIT) {
            if (playerLeftName != indigoApp.rootService.networkService.client!!.playerName && !otherPlayer.contains(
                    playerLeftName
                )
            )
                textMessageLabel.text = "Host left the Game"
            playAnimation(DelayAnimation(duration = 2000).apply {
                onFinished = {
                    textMessageLabel.isVisible = false
                    textMessageLabel.isDisabled = true
                    joinButton.isDisabled = false
                    reset()
                    indigoApp.showMenuScene(indigoApp.networkScene)
                }
            })
        }
    }

    /**
     * Initiates the process of joining a game.
     */
    fun startJoinGame() {
        indigoApp.rootService.networkService.joinGame(
            name = nameInput.text, sessionID = idInput.text
        )
    }

    /**
     * Refreshes and resets the scene
     */
    private fun reset() {
        textMessageLabel.isVisible = false
        textMessageLabel.isDisabled = true
        joinButton.isDisabled = false
        nameInput.text = ""
        joinButton.isDisabled = true
        idInput.text = ""
        yesButton.isSelected = false
        noButton.isSelected = true
    }
}