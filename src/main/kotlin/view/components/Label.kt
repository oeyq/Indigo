package view.components
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.util.Font

/**
 * Custom Label class extending the Label component from the BGW framework.
 *
 * This class provides a label with customized styling and font settings.
 *
 * @param posX The X-coordinate position of the label.
 * @param posY The Y-coordinate position of the label.
 * @param width The width of the label.
 * @param height The height of the label.
 * @param text The text displayed on the label.
 * @param fontSize The font size of the label's text.
 */
class Label(posX : Int = 0, posY : Int = 0, width : Int = 0, height : Int = 0,
            text : String = "I'm a Label", fontSize : Int = 20) :
    Label(
        posX = posX,
        posY = posY,
        width = width,
        height = height,
        text = text,
        font = Font(size = fontSize, family = "Irish Grover")
    )
{
    // Set the style of the button component
    init {
        this.componentStyle = "-fx-text-fill: linear-gradient(to bottom, #061598, #06987E);"
    }
}