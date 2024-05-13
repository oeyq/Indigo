package service

import  entity.Indigo
import entity.Token

/**
 *  the function [createTestGateTokens] is a function a test set of gateTokens.
 *
 *  @param game The indigo game which you want to create gateTokens
 *  @param notSharedGates The notSharedGates is to if the gateTokens have
 *  sharedGates or notSharedGates
 * @param game The Indigo game instance for which test gate tokens are created.
 * @param notSharedGates A flag indicating whether the created gate tokens are not shared among players.
 * @return A MutableList<Token> containing the created test gate tokens.
 */
fun createTestGateTokens(game: Indigo, notSharedGates: Boolean): MutableList<Token> {
    // Retrieve the players and determine the size of the player list
    val players = game.players
    val playerSize = players.size
    // Initialize a mutable list to store the generated gate tokens
    val gateTokens = mutableListOf<Token>()
    // Generate gate tokens based on the specified conditions

    if (notSharedGates) {
        // Create gate tokens for each player in a cyclic manner
        for (i in 0 until 6) {
            gateTokens.add(Token(players[i % playerSize].color))
            gateTokens.add(Token(players[i % playerSize].color))
        }
    } else {
        // Generate gate tokens based on the number of players
        if (playerSize == 4) {
            gateTokens.add(Token(players[0].color))
            gateTokens.add(Token(players[1].color))
            gateTokens.add(Token(players[1].color))
            gateTokens.add(Token(players[2].color))
            gateTokens.add(Token(players[0].color))
            gateTokens.add(Token(players[3].color))
            gateTokens.add(Token(players[3].color))
            gateTokens.add(Token(players[1].color))
            gateTokens.add(Token(players[2].color))
            gateTokens.add(Token(players[0].color))
            gateTokens.add(Token(players[2].color))
            gateTokens.add(Token(players[3].color))
        }
        if (playerSize == 3) {
            gateTokens.add(Token(players[0].color))
            gateTokens.add(Token(players[0].color))
            gateTokens.add(Token(players[0].color))
            gateTokens.add(Token(players[1].color))
            gateTokens.add(Token(players[2].color))
            gateTokens.add(Token(players[2].color))
            gateTokens.add(Token(players[2].color))
            gateTokens.add(Token(players[0].color))
            gateTokens.add(Token(players[1].color))
            gateTokens.add(Token(players[1].color))
            gateTokens.add(Token(players[1].color))
            gateTokens.add(Token(players[2].color))
        }
    }
    // Return the generated list of gate tokens
    return gateTokens
}
