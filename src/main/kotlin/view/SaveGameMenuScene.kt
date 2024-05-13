package view

import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.Button
import view.components.Label

/**
 * Represents the menu scene for saving a game.
 * @param indigoApp The IndigoApplication instance associated with the save game menu scene.
 */
class SaveGameMenuScene(indigoApp: IndigoApplication) :
    MenuScene(1920, 1080, background = ImageVisual("save_menu_scene.png")), Refreshable {
    // Button for continuing the game without saving
    private val continueButton = Button(266, 642, 528, 207, "Continue game", 48).apply {
        onMouseClicked = {
            indigoApp.showGameScene(indigoApp.gameScene)
            indigoApp.hideMenuScene()
        }
    }

    // Button for confirming the decision to save the game
    private val yesButton = Button(1100, 642, 528, 207, "Yes", 48).apply {
        onMouseClicked = {
            indigoApp.rootService.gameService.saveGame("GameState.json")
            indigoApp.showMenuScene(indigoApp.gameSavedScene)
        }
    }

    // Label displaying the first part of the confirmation message
    private val label1 = Label(424, 284, 1072, 116, "Do you want to break of", 86)

    // Label displaying the second part of the confirmation message
    private val label2 = Label(424, 400, 1072, 116, "and save the game?", 86)

    /**
     * Initializes the SaveGameMenuScene with default values and sets up UI components.
     */
    init {
        // Set the initial opacity of the scene
        opacity = 1.0
        // Add components to the scene
        addComponents(label1, label2, continueButton, yesButton)
    }
}