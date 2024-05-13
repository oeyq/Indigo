package entity

/**
 * Class representing a tile in the game.
 *
 * @property paths List of pairs of edges defining the paths on the tile.
 * @property gemEndPosition Map representing the positions of gems on the tile.
 * The key is the position (0-5), and the value is the gem at that position.
 * defaults to an empty map.
 * @property edges List of edges on the tile.
 */

data class Tile(
    val paths: List<Pair<Edge, Edge>>,
    val type: TileType,
    val gemEndPosition: MutableMap<Int, Gem> = mutableMapOf()
) {
    val edges: MutableList<Edge> = mutableListOf(Edge.ZERO, Edge.ONE, Edge.TWO, Edge.THREE, Edge.FOUR, Edge.FIVE)

    override fun toString(): String {
        return type.toString()
    }
}