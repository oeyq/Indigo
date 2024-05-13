package entity

import java.util.Date

/**
 * class modelling a Player not controlled by human input, extends [Player]
 *
 * @constructor creates a CPU Player with the given customizations
 *
 * @param name [String] for the CPU Player's name
 * @param age (optional) [Date] to give the CPU Player a birthdate, defaults to Unix time 0
 * @param color [TokenColor] for the CPU Player's assigned Tokens
 * @param difficulty (optional) [String] to set the CPU Player difficulty, defaults to "easy"
 * @param simulationSpeed (optional) [Int] to set the CPU Player action speed/delay
 * @property isAI to check if any player is CPU controlled, always set to true
 * @property score [Int] to keep track of accumulated points, initially 0
 */
class CPUPlayer(name: String,
                age: Date = Date(0),
                color: TokenColor,
                var difficulty:String = "easy",
                var simulationSpeed:Int = 1)
                : Player(name,age,color,true)