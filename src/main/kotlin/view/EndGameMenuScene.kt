package view

import entity.Player
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.Button
import view.components.Label

/**
 * Represents the end game menu scene.
 *
 * This scene is displayed when a game reaches its conclusion. It includes a label indicating the game is over,
 * and buttons for exiting the game or starting a new game.
 *
 * The layout and design of these components are defined in this class.
 *
 * @property indigoApp to get an instance of the RootService to access game-related functionalities.
 */
class EndGameMenuScene(private val indigoApp: IndigoApplication) : MenuScene(1920, 1080), Refreshable {

    // Label to display the "Game Over" Header.
    private val gameOverLabel = Label(650, 48, 620, 155, "Game Over", 120)

    private val betweenGemsLabel = Label(502, 307, 961, 77, "Player 1 has won with XY Points", 64)
    private val player1 = Label(719, 417, 771, 59, "Player 1 has XY Points.", 48).apply {
        isVisible = false
        alignment = Alignment.CENTER_LEFT
    }
    private val player2 = Label(
        width = 771, height = 59, posX = 719, posY = 503, fontSize = 48, text = "Player 2 has XY Points."
    ).apply { alignment = Alignment.CENTER_LEFT }
    private val player3 = Label(
        width = 771, height = 59, posX = 719, posY = 589, text = "Player 3 has XY Points.", fontSize = 48
    ).apply {
        isVisible = false
        alignment = Alignment.CENTER_LEFT
    }
    private val player4 = Label(
        width = 771, height = 59, posX = 719, posY = 675, fontSize = 48, text = "Player 4 has XY Points."
    ).apply {
        isVisible = false
        alignment = Alignment.CENTER_LEFT
    }


    // Button for exiting and starting a new game.
    private val exitButton = Button(1331, 871, 440, 158, "Exit", 38).apply {
        onMouseClicked = {
            indigoApp.exit()
        }
    }
    private val newGameButton = Button(143, 871, 440, 158, "Start new game", 38).apply {
        onMouseClicked = {
            indigoApp.networkConfigureScene.resetSettings()
            indigoApp.rootService.networkService.disconnect()
            indigoApp.configurePlayersScene.clearPlayerView()
            indigoApp.showMenuScene(indigoApp.startScene)
        }
    }

    private val restartButton = Button(737, 871, 440, 158, "Restart game", 38).apply {
        onMouseClicked = {

            val newPlayers = mutableListOf<Player>()
            for (player in indigoApp.rootService.currentGame!!.players) {
                player.score = 0
                player.collectedGems.clear()
                newPlayers.add(player)
            }
            indigoApp.rootService.gameService.startGame(newPlayers, indigoApp.notSharedGates, false)
            indigoApp.hideMenuScene()
        }
    }

    // Setting the background and adding components to the scene.
    init {
        background = ImageVisual("five_gems_background.png")
        opacity = 1.0
        addComponents(
            betweenGemsLabel,
            player1,
            player2,
            player3,
            player4,
            gameOverLabel,
            exitButton,
            newGameButton,
            restartButton
        )
    }

    /**
     * Refreshes the scene after the end of a game.
     * This function may handle tasks specific to updating the graphical user interface after the game concludes.
     * Additional implementation details can be added here.
     */
    override fun refreshAfterEndGame() {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game)
        val players = game.players
        val sortedPlayers =
            players.sortedWith(compareByDescending<Player> { it.score }.thenByDescending { it.collectedGems.size })
        betweenGemsLabel.text = sortedPlayers[0].name + " has " + sortedPlayers[0].score + "Points."
        player2.text = sortedPlayers[1].name + " has " + sortedPlayers[1].score + "Points."
        if (sortedPlayers[0].score == sortedPlayers[1].score) {
            betweenGemsLabel.text =
                "${sortedPlayers[0].name} has  ${sortedPlayers[0].score}  " +
                        "Points and ${sortedPlayers[0].collectedGems.size} Gems "
            player2.text =
                "${sortedPlayers[1].name} has  ${sortedPlayers[1].score}  " +
                        "Points and ${sortedPlayers[1].collectedGems.size} Gems "
        }
        if (sortedPlayers[0].score == sortedPlayers[1].score &&
            sortedPlayers[0].collectedGems.size == sortedPlayers[1].collectedGems.size
        ) {
            betweenGemsLabel.text = "It's a Tie between " +
                    sortedPlayers[0].name + " and " + sortedPlayers[1].name
            player1.isVisible = true
            player1.text =
                players[0].name + " has " + players[0].score + "Points and " +
                        players[0].collectedGems.size + " Gems."
            player1.posX = 605.0
            player2.text =
                players[1].name + " has " + players[1].score + "Points and " +
                        players[1].collectedGems.size + " Gems."
            player2.posX = 605.0
        }

        if (players.size >= 3) {
            player3.text = players[2].name + " has " + players[2].score + "Points."
            player3.isVisible = true
            if (sortedPlayers[2].score == sortedPlayers[0].score) {
                player3.text =
                    players[2].name + " has " + players[2].score + "Points and " +
                            players[2].collectedGems.size + " Gems."
                player3.posX = 605.0
            }
            if (sortedPlayers[0].score == sortedPlayers[2].score &&
                sortedPlayers[0].collectedGems.size == sortedPlayers[2].collectedGems.size
            ) {
                betweenGemsLabel.text =
                    "It's a Tie between " + sortedPlayers[0].name + ", " +
                            sortedPlayers[1].name + " and " + sortedPlayers[2].name
                player3.isVisible = true
                player3.text =
                    players[2].name + " has " + players[2].score + "Points and " +
                            players[2].collectedGems.size + " Gems."
                player3.posX = 605.0
            }
        }
        if (players.size == 4) {
            player4.text = players[3].name + " has " + players[3].score + "Points."
            player4.isVisible = true
            if (sortedPlayers[0].score == sortedPlayers[3].score) {
                player4.text =
                    players[3].name + " has " + players[3].score + "Points and " +
                            players[3].collectedGems.size + " Gems."
                player4.posX = 605.0
            }
            if (sortedPlayers[0].score == sortedPlayers[3].score &&
                sortedPlayers[0].collectedGems.size == sortedPlayers[3].collectedGems.size
            ) {
                betweenGemsLabel.text =
                    "It's a Tie between " + sortedPlayers[0].name + ", " +
                            sortedPlayers[1].name + ", " + sortedPlayers[2].name + " and " +
                            sortedPlayers[3].name
                player4.isVisible = true
                player4.text =
                    players[3].name + " has " + players[3].score +
                            "Points and " + players[3].collectedGems.size +
                            " Gems."
                player4.posX = 605.0
            }
        }
    }
}