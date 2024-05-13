package service.ai

import entity.*

/**
 * The `ServiceAi` class provides AI-related functionalities for the Indigo game.
 */
class ServiceAi {

    /**
     * Companion object containing AI-related functions.
     */
    companion object {

        /**
         * Executes a move in the game by placing a tile on the board at the specified coordinate for AI analysis.
         *
         * @param newIndigoo The current state of the Indigo game.
         * @param coordinate The coordinate where the tile should be placed.
         * @return The updated state of the Indigo game after the move.
         */

        // To Do : to add the simulation for the opponent to calculate the score at the end
        fun doMove(newIndigoo: Indigo, coordinate: Coordinate): Indigo {
            // Create a copy of the current Indigo state to modify
            val newIndigo = newIndigoo.copyTo()

            // Get the tile from the current player's hand or route tiles
            val tile = newIndigo.players[newIndigo.currentPlayerIndex].handTile


            if (tile == null) {
                GameServiceAi(newIndigo).distributeNewTile()
            }

            if (GameServiceAi(newIndigo).checkPlacement(
                    coordinate,
                    tile!!,
                    false
                )
            ) { //when it blocks an exist we rotate it till it s correct
                // Place the tile on the board at the specified coordinate
                GameServiceAi(newIndigo).placeTile(coordinate, tile)
            } else { // Rotate the tile until it can be placed

                while (!GameServiceAi(newIndigo).checkPlacement(coordinate, tile, false)) {
                    rotateTileLeft(tile)
                }

                // Place the tile on the board at the specified coordinate after rotations
                GameServiceAi(newIndigo).placeTile(coordinate, tile)
            }

            //to fix to calculate the score for the Ai analysis
            /*val neighbors = servicee(newIndigo).getNeighboringCoordinates(coordinate)

            for (i in neighbors.indices) {
                servicee(newIndigo).moveGems(coordinate, neighbors[i], i)
             }*/


            // Return the updated Indigo state after the move
            return newIndigo
        }


        /**
         * Checks if the game is over by determining if there are any available moves left on the game board.
         *
         * @param state The current state of the Indigo game.
         * @return `true` if there are no available moves left, indicating the game is over; `false` otherwise.
         */
        fun isGameOver(state: Indigo): Boolean {
            // List to store available coordinates for placing tiles
            val availableMoves: MutableList<Coordinate> = mutableListOf()

            // Iterate over the game board and find available moves
            for (row in -4..4) {
                for (col in Integer.max(-4, -row - 4)..Integer.min(4, -row + 4)) {
                    val coordinate = Coordinate(row, col)
                    if (state.gameBoard.gameBoardTiles[coordinate] == null) {
                        availableMoves.add(Coordinate(row, col))
                    }
                }
            }

            // If the list of available moves is empty or no more moves available, the game is over
            return availableMoves.isEmpty() || state.gems.isEmpty()
        }


        /**
         * Rotates the given tile to the left by moving its edges.
         *
         * @param tile The tile to be rotated.
         */
        private fun rotateTileLeft(tile: Tile) {
            // Add the first edge to the end of the list (left rotation)
            tile.edges.addAll(tile.edges.subList(0, 1))

            // Remove the original first edge (which is now at the end after rotation)
            tile.edges.removeAll(tile.edges.subList(0, 1))
        }


    }

}



