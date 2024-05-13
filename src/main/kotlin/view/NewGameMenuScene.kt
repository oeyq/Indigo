package view

import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.Button
import view.components.Label

/**
 * Represents the menu scene for starting or continuing a new game.
 *@param indigoApp represents an object of [IndigoApplication]
 */
class NewGameMenuScene(indigoApp: IndigoApplication) :
    MenuScene(1920, 1080, background = ImageVisual("new_game_menu_background.png")), Refreshable {
    // Button for starting a new game

    private val startButton = Button(696, 337, 528, 207, "Start new game", 48).apply {
        onMouseClicked = {
            indigoApp.showGameScene(indigoApp.modusScene)
            indigoApp.hideMenuScene()
        }
    }
    // Button for continuing an existing game

    private val continueButton = Button(696, 664, 528, 207, "Continue game", 48).apply {
        onMouseClicked = {
            indigoApp.rootService.gameService.loadGame("./indigogames/GameState.json")
            indigoApp.showGameScene(indigoApp.gameScene)
            indigoApp.hideMenuScene()
        }
    }

    // Label for the game name or title
    private val indigoLabel = Label(775, 62, 370, 155, "Indigo", 120)

    /**
     * Initializes the NewGameMenuScene with default values and sets up UI components.
     */
    init {
        // Set the initial opacity of the scene
        opacity = 1.0
        // Add components to the scene
        addComponents(indigoLabel, startButton, continueButton)
    }

}