package view.components
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.visual.ImageVisual

/**
 * Custom BackPfeil class extending the Button component from the BGW framework.
 * @param posX X-coordinate position of the button.
 * @param posY Y-coordinate position of the button.
 * @param width Width of the button.
 * @param height Height of the button.
 */

class BackPfeil (posX : Int = 30, posY : Int = 30, width : Int = 70, height : Int = 60) :
    Button(
        posX = posX,
        posY = posY,
        width = width,
        height = height,
        visual = ImageVisual("backPfeil.png")
    )
{
    // Set the style of the button component
    init {
        this.componentStyle="-fx-text-fill: linear-gradient(to bottom, #061598, #06987E);"
    }
}
