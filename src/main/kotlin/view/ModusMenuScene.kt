package view

import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.BackPfeil
import view.components.Button
import view.components.Label

/**
 * Represents the menu scene for selecting game modes : HotSeat and Network.
 * HotSeat game is played by all players on the same device by taking turns while network game is played
 * by each player locally on his/her device with other players via network
 * @param indigoApp represents an object of [IndigoApplication]
 */

class ModusMenuScene(indigoApp: IndigoApplication) :
    BoardGameScene(1920, 1080, background = ImageVisual("seven_gems2_background.png")), Refreshable {
    // Buttons for selecting game modes
    private val hotSeatButton = Button(266, 642, 528, 207, "HotSeat", 48).apply {
        onMouseClicked = {
            indigoApp.showGameScene(indigoApp.configurePlayersScene)
            indigoApp.hideMenuScene()
        }
    }

    private val networkButton = Button(1100, 642, 528, 207, "Network", 48).apply {
        onMouseClicked = {
            indigoApp.networkMode = true
            indigoApp.gameScene.saveButton.apply{ isVisible = false
            isDisabled = true}

            indigoApp.gameScene.redoButton.apply { isVisible = false
                isDisabled = true }
            indigoApp.gameScene.undoButton.apply { isVisible = false
                isDisabled = true }
            indigoApp.showMenuScene(indigoApp.networkScene)
        }
    }

    private val backPfeil = BackPfeil ().apply {
        onMouseClicked = {
            indigoApp.showMenuScene(indigoApp.startScene)
        }
    }

    // Labels for providing instructions
    private val modusLabel1 = Label(639, 388, 642, 85, "Please, choose your", 60)
    private val modusLabel2 = Label(639, 455, 642, 85, "game mode:", 60)

    /**
     * Initializes the ModusMenuScene with default values and sets up UI components.
     */
    init {
        // Set the initial opacity of the scene
        opacity = 1.0
        // Add components to the scene
        addComponents(modusLabel1, modusLabel2, networkButton, hotSeatButton, backPfeil)
    }
}