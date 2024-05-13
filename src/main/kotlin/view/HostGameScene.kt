package view

import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.uicomponents.RadioButton
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.ToggleGroup
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import view.components.BackPfeil
import view.components.Button
import view.components.Label


/**
 * Represents the scene for hosting a game.
 *
 * This scene includes elements for entering host and game details, such as name and game ID,
 * along with a button to initiate hosting a game.
 * The layout and interaction logic for these components are defined within this class.
 *
 * @property rootService An instance of RootService to access and manipulate game data.
 */
class HostGameScene(val indigoApp: IndigoApplication) : MenuScene(990, 1080), Refreshable {

    private var aiHost = false
    private val rootService = indigoApp.rootService
    //private val game = indigoApp.rootService.currentGame

    // Label to display the "Host Game" Header.
    private val hostLabel = Label(42, 105, 900, 116, "Host Game", 120)

    // Label for the Name.
    private val nameLabel = Label(80, 321, 300, 58, "Name :", 48)

    // Label for the ID.
    private val sessionIdLabel = Label(80, 486, 350, 116, "Session ID (optional):", 28)

    // TextField for the host's name with an event handler to
    // enable or disable the host game button based on text input.
    private val hostName: TextField = TextField(
        width = 454,
        height = 69,
        posX = 390,
        posY = 320,
    ).apply {
        onKeyTyped = {
            hostGameButton.isDisabled = this.text.isBlank()
        }
    }

    // TextField for entering the session ID.
    private val sessionId: TextField = TextField(
        width = 454,
        height = 69,
        posX = 390,
        posY = 510
    )

    private val aiLabel = Label(130, 693, width = 200, text = "AI : ", fontSize = 48)
    private val yesLabel = Label(440, 693, width = 80, text = "yes", fontSize = 48)
    private val noLabel = Label(640, 693, width = 80, text = "no", fontSize = 48)


    private val toggleGroup = ToggleGroup()
    private val yesButton = RadioButton(posX = 390, posY = 693, toggleGroup = toggleGroup).apply {
        onMouseClicked = {
            aiHost = true
        }
    }
    private val noButton = RadioButton(posX = 590, posY = 693, isSelected = true, toggleGroup = toggleGroup).apply {
        onMouseClicked = {
            aiHost = false
        }
    }

    // Button for host to game.
    private val hostGameButton = Button(247, 798, 532, 207, "Host game", 48).apply {
        isDisabled = hostName.text.isBlank()
        onMouseClicked = {
            indigoApp.rootService.networkService.hostGame(name = hostName.text, sessionID = sessionId.text)
            indigoApp.aiGame = aiHost
        }
    }

    private val textMessageLabel = Label(
        15,
        320,
        960,
        480,
        "Waiting for Confirmation",
        48
    ).apply {
        visual = ImageVisual("message.png")
        isVisible = false
        isDisabled = true
    }

    private val backPfeil = BackPfeil(60, 40, 70, 60).apply {
        onMouseClicked = {
            indigoApp.showMenuScene(indigoApp.networkScene)
            resetSettings()
            indigoApp.rootService.networkService.disconnect()
        }
    }


    // Setting the scene's opacity and adding all components
    init {
        opacity = 0.7
        addComponents(
            hostLabel,
            nameLabel,
            sessionIdLabel,
            hostName,
            sessionId,
            hostGameButton,
            yesLabel,
            noLabel,
            aiLabel,
            yesButton,
            noButton,
            backPfeil,
            textMessageLabel,
        )
    }

    /**
     * Refreshes the scene after hosting a game.
     */
    override fun refreshAfterHostGame() {
        textMessageLabel.isVisible = true
        textMessageLabel.isDisabled = false
        textMessageLabel.text = rootService.networkService.connectionState.name
    }

    /**
     * Refreshes the scene after receiving a response from creating a game.
     *
     * @param sessionID The session ID associated with the created game, or null if creation failed.
     */
    override fun refreshAfterOnCreateGameResponse(sessionID: String?) {
        textMessageLabel.isVisible = true
        textMessageLabel.isDisabled = false
        if (sessionID == null) {
            if (sessionId.text.isBlank()) {
                textMessageLabel.text = "Connection failed"
            } else {
                textMessageLabel.text = "Please use another Session ID"
            }
        } else {
            textMessageLabel.text = "Session ID: $sessionID"
        }

        playAnimation(DelayAnimation(duration = 4000).apply {
            onFinished = {
                textMessageLabel.isVisible = false
                textMessageLabel.isDisabled = true
                if (sessionID != null) {
                    resetSettings()
                    indigoApp.hideMenuScene()
                    indigoApp.showGameScene(indigoApp.networkConfigureScene)
                }
            }
        }
        )
    }

    /**
     * Resets the settings of the scene.
     */
    private fun resetSettings() {
        textMessageLabel.isVisible = false
        textMessageLabel.isDisabled = true
        hostName.text = ""
        sessionId.text = ""
        yesButton.isSelected = false
        noButton.isSelected = true
    }
}