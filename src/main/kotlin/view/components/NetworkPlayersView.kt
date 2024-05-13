package view.components

import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.visual.ColorVisual

/**
 * NetworkPlayersView class representing a custom view for displaying network player information.
 *
 * This class extends Pane and is used to create a specific layout for network player components
 * such as buttons and labels. Each instance of this class must be provided with specific X and Y
 * coordinates for positioning.
 *
 * @param posX The X-coordinate position of the view.
 * @param posY The Y-coordinate position of the view.
 */
class NetworkPlayersView (posX : Int = 0, posY : Int = 0) :
    Pane<ComponentView>(posX,posY,1643,141, visual = ColorVisual(253,240,216))
{
   val button = Button(1212,16,367,109,"Configure",30)
   val label = Label(141,44,956,58,"Player1 : Name", 48)

    init {
        this.add(button)
        this.add(label)
    }
}
