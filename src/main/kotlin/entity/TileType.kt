package entity

import tools.aqua.bgw.visual.ImageVisual

/**
 * enum class for the different TileTypes
 *
 * @constructor Create empty Tile type
 */
enum class TileType {
    Type_0,
    Type_1,
    Type_2,
    Type_3,
    Type_4,
    Type_5 //TreasureTiles
    ;

    /**
     * function to provide an image to represent this TileType.
     * returns the matching image
     */
    fun toImg() =
        when(this) {
            Type_0 -> ImageVisual("tile0.png")
            Type_1 -> ImageVisual("tile1.png")
            Type_2 -> ImageVisual("tile2.png")
            Type_3 -> ImageVisual("tile3.png")
            Type_4 -> ImageVisual("tile4.png")
            Type_5 -> ImageVisual("gatetile1.png") //stimmt für nur ein gate da aufgeteilt in 6 Bilder
        }
}