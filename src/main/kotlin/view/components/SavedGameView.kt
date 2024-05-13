package view.components

import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.visual.ColorVisual

/**
 * SavedGameView class extending Pane for displaying saved game information.
 *
 * This class creates a view to display components like a button and a label specifically
 * for the context of a saved game interface.
 *
 * @param posX The X-coordinate position of the view.
 * @param posY The Y-coordinate position of the view.
 */

class SavedGameView (posX : Int = 0, posY : Int = 0) :
    Pane<ComponentView>(posX,posY,1643,217, visual = ColorVisual(253,240,216))
{
    private val button = Button(1126,32,394,153,"Continue",40)
    private val label = Label(141,79,956,58,"Player1 x Player2 x Player3 x Player4", 40)

    init {
        this.add(button)
        this.add(label)
    }
}
