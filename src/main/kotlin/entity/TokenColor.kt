package entity

import tools.aqua.bgw.visual.ImageVisual

/**
 * enum class for the different Token colors
 */
enum class TokenColor {
    WHITE,
    PURPLE,
    BLUE,
    RED,
    ;
    /**
     * function to provide an image to represent this tokenColor.
     * returns the matching image
     */
    fun toImg() =
        when(this) {
            WHITE -> ImageVisual("token_white.png")
            PURPLE -> ImageVisual("token_purple.png")
            BLUE -> ImageVisual("token_blue.png")
            RED -> ImageVisual("token_red.png")
        }
}