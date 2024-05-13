package view.components
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.visual.ImageVisual

/**
 * PlayerView class extending GridPane for displaying player information in a grid layout.
 *
 * This class creates a grid layout to display the name, color indicator, and turn information of a player.
 *
 * @param name The name of the player.
 * @param color The color associated with the player. If a color is not specified, no color indicator is shown.
 * @param turn The turn number of the player. If null, no turn information is displayed.
 * @param posX The X-coordinate position of the view.
 * @param posY The Y-coordinate position of the view.
 */
class PlayerView(name : String, color:String ="", turn : Int?, posX : Int = 0, posY : Int = 0) :
    GridPane<Label>(posX,posY, layoutFromCenter = false, columns = 3, rows = 1)
{

    private val turnOutput = turn?.toString()?:"random"
    init {
        //name
        this[0,0] = Label(text = name, width = 200, height = 65, fontSize = 40)
        //color
        getGem(color)

        this[2,0] = Label(text = "turn : " + turnOutput, width = 400, height = 65,fontSize = 40)

    }

    /**
     * Sets the visual representation of the player's color.
     * If a valid color is provided, a corresponding image is displayed.
     * If no valid color is provided, a default label is shown.
     *
     * @param color The color string that determines which color visual to display.
     */
    private fun getGem(color:String)
    {
        if(color!="null")
        {
            if(color=="red") this[1, 0] = Label(text = "").apply{visual = ImageVisual("tokenRed.png") }
            if(color=="white") this[1, 0] = Label(text = "").apply{visual = ImageVisual("tokenWhite.png")}
            if(color=="blue") this[1, 0] = Label(text = "").apply{visual = ImageVisual("tokenBlue.png")}
            if(color=="purple") this[1, 0] = Label(text = "").apply{visual = ImageVisual("tokenPurple.png")}
        }
        else this[1, 0] = Label(text = "color : random", width = 400, height = 65,fontSize = 40)
    }
}