package view

import tools.aqua.bgw.components.layoutviews.CameraPane
import view.components.*
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.ImageVisual

/**
 * Represents the scene for displaying saved games in the menu.
 *
 * @property indigoApp The Indigo application instance.
 */
class SavedGamesMenuScene(val indigoApp: IndigoApplication) :
    BoardGameScene(1920, 1080, background = ImageVisual("plain_background.png")),
    Refreshable {    // Label for the scene title

    private val label = Label(566, 22, 777, 155, "Saved games", 120)

    // Grid for displaying saved game views
    private val grid = GridPane<SavedGameView>(960, 540, 1, 4, 10, true)

    // The size of the list of saved games
    private val size = 4 - 1

    // Camera pane for controlling the view of the saved games grid
    private val camera = CameraPane(
        139, 176,
        1920, 857, target = grid
    ).apply {
        interactive = true
        isHorizontalLocked = true
        isVerticalLocked = false
        isZoomLocked = true
    }

    /**
     * Initializes the SavedGamesMenuScene with default values and sets up UI components.
     */

    init {
        // Populate the grid with SavedGameView instances

        for (i in 0..size) {
            grid[0, i] = SavedGameView().apply {
                onMouseClicked={
                    indigoApp.rootService.gameService.loadGame("GameSaved1")
                }
            }
        }
        // Add components to the scene
        addComponents(label, camera)
    }

}
