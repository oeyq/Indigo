package view

import entity.CPUPlayer
import entity.Player
import entity.TokenColor
import service.network.ConnectionState
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.RadioButton
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.ToggleGroup
import tools.aqua.bgw.core.MenuScene
import view.components.BackPfeil
import view.components.Button
import view.components.Label
import view.components.NetworkPlayersView

/**
 * Represents a scene for creating a new player within the Indigo game application. This scene allows users to
 * configure player details, such as name, age (optional), color (optional), turn order (optional), and specify
 * whether the player is an AI or a human.
 *
 * Inherits from MenuScene and implements Refreshable for refreshing the scene components when needed.
 *
 * @property indigoApp The main application instance which this scene is a part of.
 */
class NewPlayerScene(val indigoApp: IndigoApplication) : MenuScene(990, 1080), Refreshable {

    // Available colors and turns for players.
    var colors = mutableListOf("blue", "purple", "red", "white")
    var turns = mutableListOf(1, 2, 3, 4)

    // Indicator for AI player selection.
    private var aiPlayer = false

    // UI components for displaying information and receiving input from the user.
    private val label = Label(42, 120, 900, 116, "Configure Player", 96)

    private val nameLabel = Label(65, 273, 300, 98, text = "Name: ", fontSize = 48)

    private val colorLabel = Label(80, 413, width = 300, text = "Color (opt): ", fontSize = 48)
    var colorBox = ComboBox(360, 405, 454.34, 69, prompt = "Select your color!", items = colors)

    private val turnLabel = Label(80, 553, width = 300, text = "Turn (opt): ", fontSize = 48)
    var turnBox = ComboBox(360, 545, 454.34, 69, prompt = "Select your turn!", items = turns)

    private val aiLabel = Label(140, 673, width = 200, text = "AI : ", fontSize = 48)
    private val yesLabel = Label(400, 673, width = 80, text = "yes", fontSize = 48)
    private val noLabel = Label(600, 673, width = 80, text = "no", fontSize = 48)

    // Buttons for AI selection.
    private val toggleGroup = ToggleGroup()
    private val yesButton = RadioButton(posX = 350, posY = 690, toggleGroup = toggleGroup).apply {
        onMouseClicked = {
            aiPlayer = true
        }
    }
    private val noButton = RadioButton(posX = 550, posY = 690, isSelected = true, toggleGroup = toggleGroup).
    apply {
        onMouseClicked = {
            aiPlayer = false
        }
    }

    private val playerName: TextField = TextField(
        width = 350,
        height = 50,
        posX = 360,
        posY = 290,
    ).apply {
        onKeyTyped = {
            addNewPlayerButton.isDisabled = this.text.isBlank()
        }
    }

    private val backPfeil = BackPfeil (40, 40 ,70, 60).apply {
        onMouseClicked = {
            indigoApp.hideMenuScene()
            resetSettings()

        }
    }

    // Button for adding a new player
    private val addNewPlayerButton = Button(250, 780, 528, 207, "Add new player", 48).apply {
        isDisabled = playerName.text.isBlank()
        onMouseClicked = {
            indigoApp.hideMenuScene()
            val newPlayerColor =
                when (colorBox.selectedItem) {
                    "purple" -> TokenColor.PURPLE
                    "blue" -> TokenColor.BLUE
                    "red" -> TokenColor.RED
                    "white" -> TokenColor.WHITE
                    else -> indigoApp.availableColors[0]
                }
            indigoApp.availableColors.remove(newPlayerColor)
            val colorText = toColorText(newPlayerColor)
            colors.remove(colorText)
            colorBox.items = colors
            var newPlayer = Player(name = playerName.text, color = newPlayerColor)
            if (aiPlayer) {
                newPlayer = CPUPlayer(name = playerName.text, color = newPlayerColor)
            }
            when (turnBox.selectedItem ?: (turns.removeFirst())) {
                1 -> indigoApp.players[0] = newPlayer
                2 -> indigoApp.players[1] = newPlayer
                3 -> indigoApp.players[2] = newPlayer
                4 -> indigoApp.players[3] = newPlayer
            }
            addPlayerToTheScene(newPlayer)
            indigoApp.aiGame = indigoApp.aiGame||aiPlayer
            refreshScene()
            //refreshAfterAddNewPlayer()
        }
    }

    /**
     * Initializes the Scene with default values and sets up UI components.
     */
    init {
        opacity = 0.7
        addComponents(
            label,
            nameLabel,
            turnBox,
            turnLabel,
            colorLabel,
            colorBox,
            yesLabel,
            noLabel,
            aiLabel,
            yesButton,
            noButton,
            addNewPlayerButton,
            playerName,
            backPfeil
        )
    }

    /**
     * Adds a new player to the scene and updates the UI components accordingly.
     * Handles both local and network players depending on the connection state.
     *
     * @param newPlayer The player object to be added to the scene.
     * min. 2 player, max 4 player
     */
    private fun addPlayerToTheScene(newPlayer: Player) {
        val connectionState = indigoApp.rootService.networkService.connectionState
        if (connectionState != ConnectionState.DISCONNECTED) {
            val configureNetworkPlayersScene = indigoApp.networkConfigureScene
            val currentRows = configureNetworkPlayersScene.grid.rows
            if (currentRows < 4) {
                configureNetworkPlayersScene.grid.addRows(currentRows)
                val grid = configureNetworkPlayersScene.grid
                configureNetworkPlayersScene.grid[0, currentRows] = NetworkPlayersView(0, 0).apply {
                    label.text = "Player " + grid.rows + ": " + newPlayer.name
                    this.button.onMouseClicked = {
                        indigoApp.showMenuScene(indigoApp.configurePlayerXScene)
                    }
                }
                turns.remove(turnBox.selectedItem)
                indigoApp.configurePlayerXScene.colors = colors
                indigoApp.configurePlayerXScene.turn = turns
                indigoApp.configurePlayerXScene.turnBox.items = turns
                indigoApp.configurePlayerXScene.colorBox.items = colors

                indigoApp.networkConfigureScene.grid[0,currentRows]!!.button.isDisabled = true
                indigoApp.networkConfigureScene.addButton.isDisabled = grid.rows == 4
                indigoApp.networkConfigureScene.startButton.isDisabled = grid.rows < 2
            }
        } else {
            indigoApp.configurePlayersScene.addPlayerView(
                playerName.text,
                newPlayer.color,
                turnBox.selectedItem,
                aiPlayer
            )
        }
    }

    /**
     * Clears all the components of the scene and deletes the chosen color and turn from the
     * ComboBoxes, so every color and turn can only be chosen once
     */
    private fun refreshScene() {
        noButton.isSelected = true
        playerName.text = ""

        colors.remove(colorBox.selectedItem)
        colorBox.items = colors
        colorBox.selectedItem = null


        turns.remove(turnBox.selectedItem)
        turnBox.items = turns
        turnBox.selectedItem = null
        addNewPlayerButton.isDisabled = true
        aiPlayer = false
    }
    /**
     * Converts a TokenColor to its corresponding text representation.
     *
     * @param color The TokenColor to be converted.
     * @return The text representation of the TokenColor.
     */
    private fun toColorText(color :TokenColor):String{
        return when(color){
            TokenColor.WHITE -> {
                "white"
            }

            TokenColor.RED -> {
                "red"
            }

            TokenColor.BLUE -> {
                "blue"
            }

            TokenColor.PURPLE -> {
                "purple"
            }
        }
    }

    /**
     * Resets UI components of the scene
     */
    private fun resetSettings(){
        playerName.text = ""
        yesButton.isSelected = false
        noButton.isSelected = true
        colorBox.selectedItem=null
        turnBox.selectedItem=null
    }
}
