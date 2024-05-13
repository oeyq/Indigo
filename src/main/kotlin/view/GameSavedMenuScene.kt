package view

import entity.Player
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.Button
import view.components.Label

/**
 * Menu scene displayed when a game has been successfully saved.
 * Extends [MenuScene] to inherit basic menu scene functionalities.
 * @param indigoApp The IndigoApplication instance associated with the saved game menu scene.
 */
class GameSavedMenuScene(indigoApp: IndigoApplication) :
    MenuScene(1920, 1080, background = ImageVisual("seven_gems2_background.png")), Refreshable {


    // Button for exiting to the main menu or closing the application
    private val exitButton = Button(702, 779, 506, 192, "Exit", 48).apply { onMouseClicked = { indigoApp.exit() } }

    // Button for starting a new game after saving
    private val newGameButton = Button(702, 301, 506, 192, "Start new game", 48).apply {
        onMouseClicked = {
            indigoApp.configurePlayersScene.clearPlayerView()
            indigoApp.showMenuScene(indigoApp.startScene)
        }
    }

    private val restartButton = Button(702, 540, 506, 192, "Restart game", 48).apply {
        onMouseClicked = {

            val newPlayers = mutableListOf<Player>()
            for (player in indigoApp.rootService.currentGame!!.players) {
                player.score = 0
                newPlayers.add(player)
            }
            indigoApp.rootService.gameService.startGame(newPlayers, indigoApp.notSharedGates, false)
            indigoApp.hideMenuScene()
        }
    }

    // Label indicating that the game has been saved
    private val label1 = Label(592, 118, 736, 155, "Game saved!", 120)

    /**
     * Initializes the GameSavedMenuScene.
     */

    init {        // Set the initial opacity

        opacity = 1.0
        // Add components to the scene
        addComponents(label1, exitButton, newGameButton, restartButton)
    }
}