package view



import entity.TokenColor
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.core.MenuScene
import view.components.BackPfeil
import view.components.Button
import view.components.Label

/**
 * Represents the game menu scene in which a player can choose his TokenColor and his turn.
 *
 * This scene is displayed when a player is chosen to be configured. It includes
 * a label indicating the title,
 * two comboBoxes with matching labels for TokenColor and turn to choose from
 * and a button for saving the configurations.
 *
 * The layout and design of these components are defined in this class.
 *
 * @parameter indigoApp
 */
class ConfigurePlayerXScene(val indigoApp: IndigoApplication) : MenuScene(990, 1080), Refreshable {
    //private val game = rootService.currentGame
    //irgendwie noch an zu bearbeitenden Spieler drankommen jetzt noch X
    //ich hab die Box noch nicht durch tokens geändert ist etwas komplizierter

    var playerName = ""
    var currentRow = 0
    private val titleLabel = Label(42, 80, 900, 116, "Configure Player X", 96)
    private val saveChangeButton = Button(247, 779, 528, 207, "Save changes", 48).apply {
        onMouseClicked = {
            configuredPlayer()
            indigoApp.showGameScene(indigoApp.networkConfigureScene)
            indigoApp.hideMenuScene()
        }
    }

    private val colorLabel = Label(80, 370, width = 300, text = "color : ", fontSize = 48)
    val colorBox = ComboBox<String>(320, 370, 454.34, 69, prompt = "Select your color!")
    private val turnLabel = Label(80, 535, width = 300, text = "turn : ", fontSize = 48)
    val turnBox = ComboBox<Int>(320, 535, 454.34, 69, prompt = "Select your turn!")

    var turn = mutableListOf(1, 2, 3, 4)
    var colors = mutableListOf("blue", "purple", "red", "white")

    private val backPfeil = BackPfeil().apply {
        onMouseClicked = {
            indigoApp.hideMenuScene()
            reset()
        }
    }

    init {
        opacity = 0.5
        addComponents(
            titleLabel,saveChangeButton,turnBox,turnLabel,colorLabel,colorBox, backPfeil)
        turnBox.items = turn
        colorBox.items = colors
    }
    /**
     * Performs actions related to a configured player.
     * This function may handle tasks specific to the configuration of players.
     * Additional implementation details can be added here.
     */
    private fun configuredPlayer() {
        val color: TokenColor
        when (colorBox.selectedItem) {
            "blue" -> color = TokenColor.BLUE
            "purple" -> color = TokenColor.PURPLE
            "red" -> color = TokenColor.RED
            "white" -> color = TokenColor.WHITE
            else -> {
                color = indigoApp.availableColors[0]
                colors.remove(mapToColorString(indigoApp.availableColors[0]))
                indigoApp.newPlayerScene.colors.remove(mapToColorString(indigoApp.availableColors[0]))
            }
        }
        colors.remove(colorBox.selectedItem)
        indigoApp.newPlayerScene.colors.remove(colorBox.selectedItem)
        indigoApp.newPlayerScene.colors.remove(mapToColorString(indigoApp.availableColors[0]))
        indigoApp.players.find { it?.name == playerName }?.color = color
        val player = indigoApp.players.find { it?.name == playerName }
        indigoApp.availableColors.remove(color)
        when (turnBox.selectedItem) {
            1 -> {
                indigoApp.players.remove(player)
                indigoApp.players[0] = player
                turn.remove(1)
                indigoApp.newPlayerScene.turns.remove(1)
            }

            2 -> {
                indigoApp.players.remove(player)
                indigoApp.players[1] = player
                turn.remove(2)
                indigoApp.newPlayerScene.turns.remove(2)
            }

            3 -> {
                indigoApp.players.remove(player)
                indigoApp.players[2] = player
                turn.remove(3)
                indigoApp.newPlayerScene.turns.remove(3)
            }

            4 -> {
                indigoApp.players.remove(player)
                indigoApp.players[3] = player
                turn.remove(4)
                indigoApp.newPlayerScene.turns.remove(4)
            }
        }
        refreshScene()
    }
    /**
     * Maps a TokenColor to its corresponding string representation.
     *
     * @param tokenColor The TokenColor to be mapped to a string.
     * @return The string representation of the specified TokenColor.
     */
    private fun mapToColorString(tokenColor: TokenColor): String {
        var color = ""
        when (tokenColor) {
            TokenColor.RED -> {
                color = "red"
            }

            TokenColor.PURPLE -> {
                color = "purple"
            }

            TokenColor.BLUE -> {
                color = "blue"
            }

            TokenColor.WHITE -> {
                color = "white"
            }
        }
        return color
    }
    /**
     * Refreshes the scene.
     * This function may handle tasks specific to refreshing the graphical user interface of the scene.
     * Additional implementation details can be added here.
     */
    private fun refreshScene() {

        colors.remove(colorBox.selectedItem)
        colorBox.items = colors
        indigoApp.newPlayerScene.colorBox.items = colors
        colorBox.selectedItem = null
        indigoApp.newPlayerScene.colorBox.selectedItem = null

        turn.remove(turnBox.selectedItem)
        turnBox.items = turn
        indigoApp.newPlayerScene.turnBox.items = turn
        turnBox.selectedItem = null
        indigoApp.networkConfigureScene.grid[0, currentRow-1]!!.button.isDisabled = true
    }
    /**
     * Resets the configuration
     */
    private fun reset(){
        colorBox.selectedItem=null
        turnBox.selectedItem=null
    }
}