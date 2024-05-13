package service

import org.junit.jupiter.api.Test
import entity.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import kotlin.test.BeforeTest

/**
 * Unit tests for the [PlayerTurnService] class.
 */
class PlayerTurnServiceTest {

    private lateinit var rootService: RootService
    private lateinit var gameService: GameService
    private lateinit var playerTurnService: PlayerTurnService
    // Sample players for testing

    private val players = mutableListOf(
        Player(name = "ALice", color = TokenColor.RED),
        Player(name = "Bob", color = TokenColor.BLUE)
    )
    // Sample test tile with specific edge configurations

    private val testTile = Tile(
        listOf(
            Pair(Edge.ZERO, Edge.TWO),
            Pair(Edge.ONE, Edge.FOUR),
            Pair(Edge.THREE, Edge.FIVE)
        ), TileType.Type_3,
        mutableMapOf(Pair(0, Gem(GemColor.AMBER)))
    )


    /**
     * Set up the test environment before each test case.
     */
    @BeforeTest
    fun setUp() {
        // Initialize the necessary services and dependencies
        rootService = RootService()
        gameService = GameService(rootService)
        playerTurnService = PlayerTurnService(rootService)
    }

    /**
     * Test the functionality of placing a route tile on the game board.
     */
    @Test
    fun testPlaceRouteTile() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterPlaceTileCalled)

        assertThrows<IllegalStateException> { playerTurnService.placeRouteTile(Coordinate(0, 0), testTile) }
        // Start a game and attempt to place the tile at another invalid coordinate, expecting an exception
        rootService.gameService.startGame(players, true)
        assertThrows<IllegalStateException> { playerTurnService.placeRouteTile(Coordinate(-4, 0), testTile) }

        // initialise tiles
        val tile1 = Tile(
            listOf(
                Pair(Edge.ZERO, Edge.TWO),
                Pair(Edge.ONE, Edge.FOUR),
                Pair(Edge.THREE, Edge.FIVE)
            ), TileType.Type_3, mutableMapOf(Pair(0, Gem(GemColor.AMBER)))
        )

        val tile2 = Tile(
            listOf(
                Pair(Edge.ZERO, Edge.TWO),
                Pair(Edge.ONE, Edge.FOUR),
                Pair(Edge.THREE, Edge.FIVE)
            ), TileType.Type_3, mutableMapOf(Pair(0, Gem(GemColor.AMBER)))
        )

        rootService.playerTurnService.placeRouteTile(Coordinate(-4, 1), tile1)
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        rootService.gameService.removeGemsReachedGate(tile1, Coordinate(-4, 1))
        assertEquals(0, tile1.gemEndPosition.size)
        assertTrue(refreshableTest.refreshAfterRemoveGemsCalled)
        refreshableTest.reset()


        //second placed tile
        rootService.playerTurnService.rotateTileRight(tile2)
        rootService.playerTurnService.placeRouteTile(Coordinate(-2, 0), tile2)
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        refreshableTest.reset()
        val middleTileGem = rootService.currentGame!!.middleTile.gemPosition
        assertEquals(6, middleTileGem.size)
        val secondPlacedTile = rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(-2, 0)]
        assertEquals(1, secondPlacedTile!!.gemEndPosition.size)

        //third placed tile
        val tile3 = testTile
        rootService.playerTurnService.placeRouteTile(Coordinate(-1, 0), tile3)
        val thirdPlacedTile = rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(-1, 0)]
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        refreshableTest.reset()
        assertEquals(5, middleTileGem.size)
        assertEquals(2, thirdPlacedTile!!.gemEndPosition.size)
        assertEquals(1, secondPlacedTile.gemEndPosition.size)
        assertEquals(11, rootService.currentGame!!.gems.size)

    }

    /**
     * Test the correctness of undo and redo operations.
     */
    @Test
    fun testUndoRedo() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterRedoCalled)
        assertFalse(refreshableTest.refreshAfterUndoCalled)

        // Check that redo and undo operations throw IllegalStateException
        assertThrows<IllegalStateException> { playerTurnService.redo() }
        assertThrows<IllegalStateException> { playerTurnService.undo() }
        // Initialize game and get the initial player's hand tile
        rootService.gameService.startGame(players)
        val testGame = rootService.currentGame
        val player1HandTile = testGame!!.players[0].handTile
        println(player1HandTile.toString())
        assertNotNull(testGame)
        assertNotNull(testGame.previousGameState)
        assertNull(testGame.previousGameState?.previousGameState)
        assertNull(testGame.nextGameState)
        // Perform actions to change the game state and then undo and redo
        //test refreshable
        // Place a route tile and observe the changes in the game state
        rootService.playerTurnService.placeRouteTile(Coordinate(-1, 1), testTile)
        assertNotNull(rootService.currentGame!!.previousGameState)
        rootService.playerTurnService.undo()
        assertTrue(refreshableTest.refreshAfterUndoCalled)
        refreshableTest.reset()
        //test refreshable
        rootService.playerTurnService.redo()
        assertTrue(refreshableTest.refreshAfterRedoCalled)
        refreshableTest.reset()

        var actualGame = rootService.currentGame
        val newPlayer1handTile = rootService.currentGame!!.players[0].handTile
        println(player1HandTile.toString())
        println(newPlayer1handTile.toString())
        assertNull(actualGame!!.nextGameState)
        assertNotNull(actualGame.previousGameState)
        // Validate the consistency of the game state after placing a route tile
        assertEquals(testGame.gameBoard.gateTokens, actualGame.gameBoard.gateTokens)
        assertEquals(testGame.gameBoard.gameBoardTiles, actualGame.gameBoard.gameBoardTiles)
        assertEquals(testGame.gems, actualGame.gems)
        assertEquals(testGame.players.size, actualGame.players.size)
        // Loop through player details to ensure consistency
        for (i in testGame.players.indices) {
            assertEquals(testGame.players[i].name, actualGame.players[i].name)
            assertEquals(testGame.players[i].handTile, actualGame.players[i].handTile)
            assertEquals(testGame.players[i].collectedGems, actualGame.players[i].collectedGems)
            assertEquals(testGame.players[i].color, actualGame.players[i].color)
            assertEquals(testGame.players[i].handTile, actualGame.players[i].handTile)
            assertEquals(testGame.players[i].collectedGems, actualGame.players[i].collectedGems)
            assertEquals(testGame.players[i].age, actualGame.players[i].age)
            assertEquals(testGame.players[i].isAI, actualGame.players[i].isAI)
            assertEquals(testGame.players[i].score, actualGame.players[i].score)
        }
        // Validate route tiles and their count
        assertEquals(testGame.routeTiles, actualGame.routeTiles)
        assertEquals(51, actualGame.routeTiles.size)
        // Undo the last action and observe the changes in the game state

        rootService.playerTurnService.undo()
        assertTrue(refreshableTest.refreshAfterUndoCalled)
        refreshableTest.reset()
        actualGame = rootService.currentGame
        assertNotNull(actualGame!!.previousGameState)
        assertEquals(1, actualGame.currentPlayerIndex)
        assertEquals(51, actualGame.routeTiles.size)
        assertEquals(newPlayer1handTile, actualGame.players[0].handTile)
        assertEquals(7, actualGame.gameBoard.gameBoardTiles.size)
        assertEquals(5, actualGame.middleTile.gemPosition.size)
        // Redo the last undone action and validate the game state

        rootService.playerTurnService.redo()
        actualGame = rootService.currentGame
        assertNull(actualGame!!.nextGameState)
        assertNotNull(actualGame.previousGameState)
        assertTrue(refreshableTest.refreshAfterRedoCalled)
        refreshableTest.reset()
        assertEquals(testGame.gameBoard.gateTokens, actualGame.gameBoard.gateTokens)
        assertEquals(testGame.gameBoard.gameBoardTiles, actualGame.gameBoard.gameBoardTiles)
        assertEquals(testGame.gems, actualGame.gems)
        assertEquals(newPlayer1handTile, actualGame.players[0].handTile)
        assertEquals(testGame.players.size, actualGame.players.size)
        // Loop through player details to ensure consistency after redo
        for (i in testGame.players.indices) {
            assertEquals(testGame.players[i].name, actualGame.players[i].name)
            assertEquals(testGame.players[i].handTile, actualGame.players[i].handTile)
            assertEquals(testGame.players[i].collectedGems, actualGame.players[i].collectedGems)
            assertEquals(testGame.players[i].color, actualGame.players[i].color)
            assertEquals(testGame.players[i].age, actualGame.players[i].age)
            assertEquals(testGame.players[i].isAI, actualGame.players[i].isAI)
            assertEquals(testGame.players[i].score, actualGame.players[i].score)
        }
        // Validate route tiles and their count after redo
        assertEquals(51, actualGame.routeTiles.size)

    }


    /**
     * Tests the undo functionality by checking the game state after undoing a player's move.
     * It involves starting a game, placing a route tile, undoing the move, and verifying the game state consistency.
     */
    @Test
    fun testUndo() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterRedoCalled)
        assertFalse(refreshableTest.refreshAfterUndoCalled)

        // Start the game and get the initial player's hand tile
        rootService.gameService.startGame(players)
        val testGame = rootService.currentGame
        val initialPlayer1HandTile = testGame!!.players[0].handTile
        assertNotNull(initialPlayer1HandTile)

        // Print initial hand tile
        println("Initial Player 1 Hand Tile: $initialPlayer1HandTile")

        // Place the initial tile
        rootService.playerTurnService.placeRouteTile(Coordinate(0, -1), initialPlayer1HandTile!!)
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        refreshableTest.reset()
        var actualGame = rootService.currentGame
        assertNotNull(actualGame)

        // Print hand tile after placing route tile
        println("Player 1 Hand Tile after placing route tile: ${actualGame!!.players[0].handTile}")

        // Undo the move
        rootService.playerTurnService.undo()
        rootService.playerTurnService.undo()
        assertTrue(refreshableTest.refreshAfterUndoCalled)
        refreshableTest.reset()
        actualGame = rootService.currentGame


        // Get the updated hand tile after undo
        val updatedPlayer1HandTile = actualGame!!.players[0].handTile

        // Assertions after undo
        println("Player 1 Hand Tile after undo: $updatedPlayer1HandTile")
        assertEquals(initialPlayer1HandTile.type, updatedPlayer1HandTile?.type)

    }

    /**
     *  The function [rotateTileTest] the function rotate
     */
    @Test
    fun rotateTileTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterRightRotationCalled)
        assertFalse(refreshableTest.refreshAfterLeftRotationCalled)

        rootService.gameService.startGame(
            mutableListOf(Player("a", color = TokenColor.BLUE), Player("b", color = TokenColor.PURPLE))
        )
        // Set up the expected and initial tile configurations
        val expectedTile = testTile
        val expectedTileRightRotated = testTile
        expectedTile.edges.add(0, expectedTile.edges.removeAt(expectedTile.edges.size - 1))
        // Rotate the tile to the right and check the result

        //refreshable test
        rootService.playerTurnService.rotateTileRight(testTile)
        assertTrue(refreshableTest.refreshAfterRightRotationCalled)
        refreshableTest.reset()
        assertEquals(expectedTileRightRotated, testTile)
        // Rotate the tile back to its original position and check
        //refreshable test
        rootService.playerTurnService.rotateTileLeft(testTile)
        assertTrue(refreshableTest.refreshAfterLeftRotationCalled)
        refreshableTest.reset()
        assertEquals(expectedTile, testTile)
    }
}
