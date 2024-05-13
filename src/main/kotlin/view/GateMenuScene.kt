package view

import entity.Player
import service.network.ConnectionState
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.BackPfeil
import view.components.Button
import view.components.Label

/**
 * Represents the menu scene for selecting gate options (shared or separated)
 * for a game of three players.
 *
 * @param indigoApp The IndigoApplication instance associated with the gate menu scene.
 */
class GateMenuScene(val indigoApp: IndigoApplication) :
    MenuScene(1920, 1080, background = ImageVisual("seven_gems2_background.png")), Refreshable {
    // Buttons for selecting game modes
    private val sharedButton = Button(266, 642, 528, 207, "SharedGates", 48).apply {
        onMouseClicked = {
            val isRandom = indigoApp.isRandom
            val players = mutableListOf<Player>()
            for(player in indigoApp.players){
                if(player!=null){
                    players.add(player)
                }
            }
            indigoApp.notSharedGates = false
            val connectionState = indigoApp.rootService.networkService.connectionState
            if (connectionState == ConnectionState.DISCONNECTED) {
                if(indigoApp.aiGame)indigoApp.showMenuScene(indigoApp.aiMenuScene)
                else
                indigoApp.rootService.gameService.startGame(players, indigoApp.notSharedGates, isRandom)
            } else {
                if(indigoApp.aiGame)indigoApp.showMenuScene(indigoApp.aiMenuScene)
                else
                indigoApp.rootService.networkService.startNewHostedGame(players, indigoApp.notSharedGates, isRandom)
            }
            if(indigoApp.aiGame) indigoApp.showMenuScene(indigoApp.aiMenuScene)
            else { indigoApp.showGameScene(indigoApp.gameScene)
            indigoApp.hideMenuScene()}
        }
    }
    private val separatedButton = Button(1100, 642, 528, 207, "SeperatedGates", 48).apply {
        onMouseClicked = {
            val isRandom = indigoApp.isRandom
            val players = mutableListOf<Player>()
            for(player in indigoApp.players){
                if(player!=null){
                    players.add(player)
                }
            }
            val connectionState = indigoApp.rootService.networkService.connectionState
            if (connectionState == ConnectionState.DISCONNECTED) {
                indigoApp.rootService.gameService.startGame(players, true, isRandom)
            } else {
                indigoApp.rootService.networkService.startNewHostedGame(players, true, isRandom)
            }
            if(indigoApp.aiGame) indigoApp.showMenuScene(indigoApp.aiMenuScene)
            else { indigoApp.showGameScene(indigoApp.gameScene)
                indigoApp.hideMenuScene()}

        }
    }

    // Labels for providing instructions
    private val gatesLabel1 = Label(381, 370, 1111, 85, "Please, choose one of the following", 60)
    private val gatesLabel2 = Label(381, 469, 1111, 85, "options :", 60)

    private val backPfeil = BackPfeil (100, 40 ,70, 60).apply {
        onMouseClicked = {
            indigoApp.showGameScene(indigoApp.configurePlayersScene)
        }
    }

    init {        // Set the initial opacity of the scene

        opacity = 1.0
        // Add components to the scene
        addComponents(gatesLabel1, gatesLabel2, sharedButton, separatedButton, backPfeil)
    }
}