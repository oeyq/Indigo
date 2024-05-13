package view

import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.BackPfeil
import view.components.Button
import view.components.Label

/**
 * NetworkMenuScene class represents a menu scene related to network operations in an Indigo application.
 *
 * @param indigoApp The IndigoApplication instance associated with the network menu scene.
 */
class NetworkMenuScene(indigoApp: IndigoApplication) :
    MenuScene(1920, 1080, background = ImageVisual("seven_gems2_background.png")), Refreshable {
    // Buttons for network game options
    private val hostButton = Button(266, 642, 528, 207, "Host game", 48).apply {
        onMouseClicked = { indigoApp.showMenuScene(indigoApp.hostGameScene) }
    }

    private val joinButton = Button(1100, 642, 528, 207, "Join game", 48).apply {
        onMouseClicked = { indigoApp.showMenuScene(indigoApp.joinGameScene) }
    }

    // Labels for providing instructions
    private val networkLabel1 = Label(381, 370, 1111, 85, "Please, choose one of the following", 60)
    private val networkLabel2 = Label(381, 469, 1111, 85, "options :", 60)

    private val backPfeil = BackPfeil(100, 40, 70, 60).apply {
        onMouseClicked = {
            indigoApp.networkMode = false
            indigoApp.showGameScene(indigoApp.modusScene)
            indigoApp.hideMenuScene()
        }
    }

    // Initializes the NetworkMenuScene with default values and sets up UI components.
    init {
        // Set the initial opacity of the scene
        opacity = 1.0
        // Add components to the scene
        addComponents(networkLabel1, networkLabel2, hostButton, joinButton, backPfeil)
    }
}