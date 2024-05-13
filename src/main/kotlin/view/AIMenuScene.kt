package view

import entity.CPUPlayer
import entity.Player
import service.network.ConnectionState
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.Button
import view.components.Label

/**
 * Represents the menu scene for an AI game.
 *
 * This scene includes various UI components like labels, a combo box for selecting AI speed, and a button to start the game.
 * The layout and visual elements are defined within this class.
 * @param indigoApp The IndigoApplication instance associated with the menu scene.
 */
class AIMenuScene(val indigoApp: IndigoApplication) : MenuScene(1920, 1080), Refreshable {


    // Label to display the header.
    private val aiGameLabel = Label(397, 71, 1126, 155, "This is an AI Game!", 120)

    // Labels for instructions regarding the simulation speed.
    private val speed1Label = Label(107, 224, 1800, 77, "Please, choose simulation speed and AI-difficulty:", 64)
    private val speed2Label = Label(397, 416, 1192, 58, "(Default : advanced AI and middle speed)", 48)
    private val speed3Label = Label(230, 508, 1192, 58, "Set speed to :", 48)
    private val speed4Label = Label(230, 608, 1192, 58, "Set difficulty to :", 48)

    // ComboBox to allow the user to select the AI speed.
    private val aiSpeed = ComboBox<String>(1015, 495, 300, 69, prompt = "Select ai speed")
    private val aiDiff = ComboBox<String>(1015, 595, 300, 69, prompt = "Select ai difficulty")

    // Button to start the game.
    private val startButton = Button(730, 805, 532, 207, "Start new game", 48).apply {
        onMouseClicked = {
            val players = indigoApp.players
            val actualPlayer = mutableListOf<Player>()
            for (player in players) {
                if (player?.isAI == true) {
                    val cpuPlayer = CPUPlayer(player.name, player.age, player.color,aiDiff(),aiSpeed())
                    actualPlayer.add(cpuPlayer)
                } else {
                    player?.let { it1 -> actualPlayer.add(it1) }
                }
            }
            if (indigoApp.networkMode) {
                val connectionState = indigoApp.rootService.networkService.connectionState
                if (connectionState == ConnectionState.DISCONNECTED) {
                    indigoApp.joinGameScene.difficulty = aiDiff()
                    indigoApp.joinGameScene.simulationSpeed = aiSpeed()
                    indigoApp.hideMenuScene()
                    indigoApp.showMenuScene(indigoApp.joinGameScene)
                    indigoApp.joinGameScene.startJoinGame()
                } else {
                    indigoApp.rootService.networkService.startNewHostedGame(
                        actualPlayer,
                        indigoApp.notSharedGates,
                        indigoApp.isRandom
                    )
                    indigoApp.showGameScene(indigoApp.gameScene)
                    indigoApp.hideMenuScene()
                }
            } else {

                indigoApp.rootService.gameService.startGame(actualPlayer,indigoApp.notSharedGates,indigoApp.isRandom)
                indigoApp.showGameScene(indigoApp.gameScene)
                indigoApp.hideMenuScene()
            }
        }
    }

    // Setting the background and adding all components to the scene.
    init {
        background = ImageVisual("three_gems_background.png")
        opacity = 1.0
        addComponents(
            aiGameLabel,
            speed1Label,
            speed2Label,
            speed3Label,
            speed4Label,
            startButton,
            aiSpeed,
            aiDiff
        )
        aiSpeed.items = mutableListOf("slow", "middle", "fast")
        //aiDiff.items = mutableListOf("easy", "medium","advanced")
        aiDiff.items = mutableListOf("easy","medium","advanced") //the medium is set to the MCTS and still needs to be fixed

    }

    /**
     * Sets chosen ai simulationSpeed to the corresponding integer value for [CPUPlayer]
     */
    private fun aiSpeed(): Int {
        when (aiSpeed.selectedItem) {
            "slow" -> return 1
            "middle" -> return 2
            "fast" -> return 5
        }
        return 2
    }

    /**
     * Handles default ai difficulty if no difficulty was set by player
     */
    private fun aiDiff(): String {
        return if(aiDiff.selectedItem != null) {
            this.aiDiff.selectedItem.toString()
        } else {
            "advanced"
        }
    }

    /**
     * hides [AIMenuScene] and shows [GameScene]
     */
    override fun refreshAfterStartGame() {
        indigoApp.hideMenuScene()
        indigoApp.showGameScene(indigoApp.gameScene)
    }
}