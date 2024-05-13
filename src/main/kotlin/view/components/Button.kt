package view.components
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual

/**
 * Custom Button class extending the Button component from the BGW framework.
 *
 * @param posX X-coordinate position of the button.
 * @param posY Y-coordinate position of the button.
 * @param width Width of the button.
 * @param height Height of the button.
 * @param text Text displayed on the button.
 * @param fontSize Font size of the text on the button.
 */

class Button (posX : Int = 0, posY : Int = 0, width : Int = 0, height : Int = 0,
              text : String = "Button", fontSize : Int = 20) :
    Button(
        posX = posX,
        posY = posY,
        width = width,
        height = height,
        text = text,
        font = Font(size = fontSize, family = "Irish Grover"),
        visual = ImageVisual("button.png")
    )
{
        // Set the style of the button component
        init {
            this.componentStyle="-fx-text-fill: linear-gradient(to bottom, #061598, #06987E);"
        }
    }
