package entity

/**
 * This class defines a gem object with the given [gemColor]. Each gem has a unique color
 * specified by the [GemColor] enum.
 *
 * @property gemColor The color of the gem.
 *
 * @constructor Creates a new gem with the specified [gemColor].
 */
data class Gem(val gemColor: GemColor, val id: Int = IDGenerator.generateID())