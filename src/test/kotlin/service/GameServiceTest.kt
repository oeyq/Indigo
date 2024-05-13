package service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import entity.*
import entity.GemColor.*
import org.junit.jupiter.api.*

/**
 * Test class for testing the methods in GameService
 */
class GameServiceTest {


    private lateinit var rootService: RootService
    private lateinit var gameService: GameService

    private val fourPlayers = listOf(
        Player("Alice", Date(0), TokenColor.WHITE, false),
        Player("Bob", Date(0), TokenColor.PURPLE, false),
        Player("Emily", Date(0), TokenColor.BLUE, false),
        Player("Jack", Date(0), TokenColor.RED, false)

    )

    private val tile0 = Tile(
        listOf(
            Pair(Edge.ZERO, Edge.TWO), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.THREE, Edge.FIVE)
        ),
        TileType.Type_0,
    )

    /**
     * Set up method executed before each test.
     */
    @BeforeEach
    fun setUp() {
        rootService = RootService()
        gameService = GameService(rootService)

    }

    /**
     * Test the startGame function to ensure a new game is correctly initialized.
     */
    @Test
    fun startGameTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterStartGameCalled)
        assertNull(rootService.currentGame)
        rootService.gameService.startGame(
            fourPlayers.toMutableList()
        )
        assertTrue(refreshableTest.refreshAfterStartGameCalled)
        refreshableTest.reset()

        var testGame = rootService.currentGame
        assertNotNull(testGame)

        val player1 = Player("Alice", Date(0), TokenColor.WHITE, false)
        val player2 = Player("Bob", Date(0), TokenColor.PURPLE, false)
        val player3 = Player("Emily", Date(0), TokenColor.BLUE, false)
        val player4 = Player("Jack", Date(0), TokenColor.RED, false)
        val playerListe = mutableListOf(player1, player2, player3, player4)
        playerListe.toList()


        assertEquals(playerListe.size, testGame!!.players.size)
        for (i in playerListe.indices) {
            assertEquals(playerListe[i].name, testGame.players[i].name)
            assertEquals(playerListe[i].color, testGame.players[i].color)
            assertNotNull(testGame.players[i].handTile)
        }
        assertEquals(0, testGame.currentPlayerIndex)
        assertEquals(50, testGame.routeTiles.size)


        rootService.gameService.startGame(fourPlayers.toMutableList(), random = true)
        assertTrue(refreshableTest.refreshAfterStartGameCalled)
        refreshableTest.reset()
        testGame = rootService.currentGame
        assertEquals(playerListe.size, testGame!!.players.size)
        //assertNotEquals(fourPlayers, testGame.players)
        for (i in playerListe.indices) {
            assertNotNull(testGame.players[i].handTile)
        }
    }

    /**
     * Test the restartGame function.
     */
    @Test
    fun restartGameTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterRestartGameCalled)
        assertNull(rootService.currentGame)
        rootService.gameService.restartGame(
            fourPlayers.toMutableList(), notSharedGate = false, random = false
        )
        assertTrue(refreshableTest.refreshAfterRestartGameCalled)
        refreshableTest.reset()
        var testGame = rootService.currentGame
        assertNotNull(rootService.currentGame)

        val player1 = Player("Alice", Date(0), TokenColor.WHITE, false)
        val player2 = Player("Bob", Date(0), TokenColor.PURPLE, false)
        val player3 = Player("Emily", Date(0), TokenColor.BLUE, false)
        val player4 = Player("Jack", Date(0), TokenColor.RED, false)
        val playerListe = mutableListOf(player1, player2, player3, player4)
        playerListe.toList()


        assertEquals(playerListe.size, testGame!!.players.size)
        for (i in playerListe.indices) {
            assertEquals(playerListe[i].name, testGame.players[i].name)
            assertEquals(playerListe[i].color, testGame.players[i].color)
            assertNotNull(testGame.players[i].handTile)
        }
        assertEquals(0, testGame.currentPlayerIndex)
        assertEquals(50, testGame.routeTiles.size)

        rootService.gameService.restartGame(fourPlayers.toMutableList(), notSharedGate = true, random = true)
        assertTrue(refreshableTest.refreshAfterRestartGameCalled)
        refreshableTest.reset()

        testGame = rootService.currentGame
        assertEquals(playerListe.size, testGame!!.players.size)
        // assertEquals(fourPlayers, testGame.players)
        for (i in playerListe.indices) {
            assertNotNull(testGame.players[i].handTile)
        }
    }

    /**
     * Test the endGame function.
     */
    @Test
    fun endGameTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterEndGameCalled)

        assertThrows<IllegalStateException> { gameService.endGame() }
        rootService.gameService.startGame(fourPlayers.toMutableList())
        assertTrue(refreshableTest.refreshAfterDistributeNewTileCalled)
        assertTrue(refreshableTest.refreshAfterChangePlayerCalled)
        assertTrue(refreshableTest.refreshAfterStartGameCalled)
        assertFalse(gameService.endGame())
        rootService.currentGame!!.gems.clear()
        rootService.playerTurnService.placeRouteTile(Coordinate(-3, 1), tile0)
        assertTrue(gameService.endGame())
        assertTrue(refreshableTest.refreshAfterEndGameCalled)
        refreshableTest.reset()

        //start new game und with no more hand tile
        rootService.gameService.startGame(fourPlayers.toMutableList())
        assertFalse(gameService.endGame())
        rootService.playerTurnService.placeRouteTile(Coordinate(-4, 1), tile0)
        rootService.currentGame!!.players[rootService.currentGame!!.currentPlayerIndex].handTile = null
        assertTrue(gameService.endGame())
        refreshableTest.reset()
    }

    /**
     * Test the checkPlacement function.
     */
    @Test
    fun checkPlacementTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterCheckPlacementCalled)
        val game = rootService.currentGame
        assertNull(game)
        rootService.gameService.startGame(
            mutableListOf(Player("a", color = TokenColor.BLUE), Player("b", color = TokenColor.PURPLE))
        )
        assertTrue(refreshableTest.refreshAfterStartGameCalled)

        //tileID 0 initialise
        val tile0 = Tile(
            listOf(Pair(Edge.ZERO, Edge.TWO), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.THREE, Edge.FIVE)),
            TileType.Type_0,
            mutableMapOf(Pair(1, Gem(EMERALD)))
        )

        //tileID 2 initialise
        val tile2 = Tile(
            listOf(Pair(Edge.ZERO, Edge.FIVE), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.TWO, Edge.THREE)),
            TileType.Type_2,
            mutableMapOf(Pair(1, Gem(EMERALD)))
        )

        //tileID 4 initialise
        val tile4 = Tile(
            listOf(Pair(Edge.ZERO, Edge.FIVE), Pair(Edge.ONE, Edge.TWO), Pair(Edge.THREE, Edge.FOUR)),
            TileType.Type_4,
            mutableMapOf(Pair(1, Gem(EMERALD)))
        )

        //rotate tile0 and place it in (-1,-3) ,then check that the place is occupied for other tile.
        assertFalse(refreshableTest.refreshAfterLeftRotationCalled)
        rootService.playerTurnService.rotateTileLeft(tile0)
        assertTrue(rootService.gameService.checkPlacement(Coordinate(-1, -3), tile0, false))
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        assertTrue(refreshableTest.refreshAfterLeftRotationCalled)
        refreshableTest.reset()

        assertFalse(rootService.gameService.checkPlacement(Coordinate(0, 0), tile2))
        val exception1 = assertThrows<IllegalStateException> {
            rootService.gameService.checkPlacement(Coordinate(-1, -3), tile2)
        }
        assertEquals(exception1.message, "this place is occupied")

        //rotate tile2 and place it in (-2,-2) ,
        // then check that the gate is blocked,
        // then rotate right and place it,then the place is occupied for other tile.
        assertFalse(refreshableTest.refreshAfterLeftRotationCalled)
        rootService.playerTurnService.rotateTileLeft(tile2)
        assertTrue(refreshableTest.refreshAfterLeftRotationCalled)
        assertFalse(refreshableTest.refreshAfterRightRotationCalled)
        refreshableTest.reset()/*
                val exception2 = assertThrows<Exception> {
                    rootService.gameService.checkPlacement(Coordinate(-2, -2), tile2)
                }*/
        //assertEquals(exception2.message, "tile blocks exit, please rotate Tile")
        rootService.playerTurnService.rotateTileRight(tile2)
        rootService.playerTurnService.rotateTileRight(tile2)
        assertTrue(refreshableTest.refreshAfterRightRotationCalled)
        assertFalse(refreshableTest.refreshAfterCheckPlacementCalled)
        //assertTrue(rootService.gameService.checkPlacement(Coordinate(-2, -2), tile2))
        rootService.playerTurnService.placeRouteTile(Coordinate(-2, -2), tile2)
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)

        refreshableTest.reset()
        val exception3 = assertThrows<IllegalStateException> {
            rootService.gameService.checkPlacement(Coordinate(-2, -2), tile4)
        }
        assertEquals(exception3.message, "this place is occupied")

        //rotate tile4 and place it in (-2,-2) , then check that is occupied, then rotate right und the place it in (-3,-1).
        //assertFalse(rootService.gameService.checkPlacement(Coordinate(-2, -2), tile4))
        val exception4 = assertThrows<IllegalStateException> {
            rootService.gameService.checkPlacement(Coordinate(-2, -2), tile4)
        }
        assertEquals(exception4.message, "this place is occupied")

        val exception5 = assertThrows<IllegalStateException> {
            rootService.gameService.checkPlacement(Coordinate(-3, -1), tile4)
        }
        assertEquals(exception5.message, "tile blocks exit, please rotate Tile")
        assertFalse(refreshableTest.refreshAfterRightRotationCalled)
        rootService.playerTurnService.rotateTileRight(tile4)
        assertTrue(refreshableTest.refreshAfterRightRotationCalled)
        //place tile4 in (-3,-2) and check refresh after place Tile
        assertFalse(refreshableTest.refreshAfterPlaceTileCalled)
        rootService.playerTurnService.placeRouteTile(Coordinate(-3, -2), tile2)
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)

    }


    /**
     * Test the checkCollision function.
     */
    @Test
    fun checkCollisionTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterCollisionCalled)

        //tileID 0 initialise and check collision.
        val tile = Tile(
            listOf(Pair(Edge.ZERO, Edge.TWO), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.THREE, Edge.FIVE)),
            TileType.Type_0,
            mutableMapOf(Pair(0, Gem(AMBER)), Pair(2, Gem(AMBER)))
        )
        assertEquals(2, tile.gemEndPosition.size)

        assertTrue(rootService.gameService.checkCollision(tile))
        rootService.gameService.checkCollision(tile)
        assertEquals(0, tile.gemEndPosition.size)
        assertTrue(refreshableTest.refreshAfterCollisionCalled)
        refreshableTest.reset()
        //checkCollision for tile after removeGems
        assertFalse(rootService.gameService.checkCollision(tile))

    }

    /**
     * Test the saveGame function.
     */

    //@Test
    fun saveGameTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterSaveGameCalled)
        assertFalse(refreshableTest.refreshAfterLoadGameCalled)

        rootService.gameService.startGame(fourPlayers.toMutableList())
        rootService.gameService.changePlayer()
        rootService.currentGame!!.gems.clear()
        rootService.currentGame!!.tokens.clear()
        val player3HandTile = rootService.currentGame!!.players[2].handTile

        println(player3HandTile.toString())

        val gameToSave = rootService.currentGame
        assertNotNull(gameToSave)
        val testPath = "gameToSaveNew.json"
        rootService.gameService.saveGame(testPath)
        assertTrue(refreshableTest.refreshAfterSaveGameCalled)
        refreshableTest.reset()
        rootService.gameService.endGame()

        //assertNotNull(File(testPath))
        rootService.gameService.loadGame(testPath)
        assertTrue(refreshableTest.refreshAfterLoadGameCalled)
        val loadedGame = rootService.currentGame
        assertEquals(1, loadedGame!!.currentPlayerIndex)
        assertEquals(0, loadedGame.gems.size)
        assertEquals(0, loadedGame.tokens.size)
        assertEquals(4, loadedGame.players.size)
        assertEquals(player3HandTile, loadedGame.players[2].handTile)

    }

    /**
     * Test the loadGame function.
     */
    //@Test
    fun loadGameTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterLoadGameCalled)

        rootService.gameService.startGame(fourPlayers.toMutableList())
        rootService.gameService.changePlayer()
        rootService.gameService.changePlayer()
        rootService.currentGame!!.gems.clear()
        rootService.currentGame!!.tokens.clear()
        val newPlayer2handTile = rootService.currentGame!!.players[1].handTile
        val testPath = "gameToSaveNew.json"
        val toSaveGame = rootService.currentGame
        assertNotNull(toSaveGame)


        rootService.gameService.saveGame(testPath)
        assertTrue(refreshableTest.refreshAfterSaveGameCalled)
        rootService.gameService.endGame()


        rootService.gameService.loadGame(testPath)
        assertTrue(refreshableTest.refreshAfterLoadGameCalled)
        refreshableTest.reset()
        val loadedGame = rootService.currentGame
        assertEquals(2, loadedGame!!.currentPlayerIndex)
        assertEquals(0, loadedGame.gems.size)
        assertEquals(0, loadedGame.tokens.size)
        assertEquals(newPlayer2handTile, loadedGame.players[1].handTile)
    }

    /**
     * Additional test for loadGame
     */
    //@Test
    fun loadGameTest2() {
        //al savedGameFile = this::class.java.getResource("GameSaved1.json")?.toExternalForm()
        //rintln(savedGameFile)
        rootService.gameService.loadGame("GameSaved1.json")
        val loadedGame = rootService.currentGame
        assertEquals(2, loadedGame?.players?.size)
        assertEquals("sss", loadedGame?.players?.get(0)?.name)
        assertEquals(0, loadedGame?.players?.get(0)?.score)
        assertEquals(TokenColor.BLUE, loadedGame?.players?.get(0)?.color)

        assertEquals("sss", loadedGame?.players?.get(1)?.name)
        assertEquals(0, loadedGame?.players?.get(1)?.score)
        assertEquals(TokenColor.RED, loadedGame?.players?.get(1)?.color)
        assertNull(loadedGame!!.nextGameState)
        println(loadedGame.previousGameState!!.id)
        println(loadedGame.id)
    }

    /**
     * Test the changePlayer function.
     */

    @Test
    fun changePlayerTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterChangePlayerCalled)
        assertThrows<IllegalStateException> {
            rootService.gameService.changePlayer()
        }
        rootService.gameService.startGame(fourPlayers.toMutableList())
        val testGame = rootService.currentGame
        checkNotNull(testGame)
        rootService.gameService.changePlayer()
        assertTrue(refreshableTest.refreshAfterChangePlayerCalled)
        refreshableTest.reset()
        var currentPlayerIndex = testGame.currentPlayerIndex
        assertEquals(1, currentPlayerIndex)
        repeat(3) {
            rootService.gameService.changePlayer()
        }
        currentPlayerIndex = testGame.currentPlayerIndex
        assertEquals(0, currentPlayerIndex)
    }


    /**
     * Test the moveGems function.
     */
    @Test
    fun moveGemsTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterPlaceTileCalled)
        assertFalse(refreshableTest.refreshAfterCollisionCalled)
        assertFalse(refreshableTest.refreshAfterMoveGemsCalled)
        assertThrows<IllegalStateException> {
            gameService.moveGems(Coordinate(0, 1), Coordinate(1, 1), 2)
        }
        //firstTile Typ 1 initialisieren
        val testTile1 = Tile(
            listOf(
                Pair(Edge.ZERO, Edge.THREE), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.TWO, Edge.FIVE)
            ), TileType.Type_1
        )
        //secondTile Typ 1 initialisieren
        val testTile2 = Tile(
            listOf(
                Pair(Edge.ZERO, Edge.THREE), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.TWO, Edge.FIVE)
            ), TileType.Type_1, mutableMapOf(Pair(1, Gem(EMERALD)), Pair(4, Gem(EMERALD)))
        )
        rootService.gameService.startGame(fourPlayers.toMutableList())
        //val treasureTile1 = rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 4)]

        rootService.playerTurnService.placeRouteTile(Coordinate(0, 2), testTile1)
        //test refreshable
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        refreshableTest.reset()
        val firstPlacedTile = rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 2)]


        rootService.playerTurnService.placeRouteTile(Coordinate(0, 3), testTile2)
        val secondPlacedTile = rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 3)]
        assertEquals(0, rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 4)]!!.gemEndPosition.size)
        assertEquals(2, rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 3)]!!.gemEndPosition.size)
        assertEquals(1, rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 2)]!!.gemEndPosition.size)
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        assertTrue(refreshableTest.refreshAfterMoveGemsCalled)

        refreshableTest.reset()

        assertNotNull(firstPlacedTile!!.gemEndPosition[5])
        assertNull(secondPlacedTile!!.gemEndPosition[5])
        //thirdTile Typ 0 initialisieren
        val testTile3 = Tile(
            listOf(
                Pair(Edge.ZERO, Edge.TWO), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.THREE, Edge.FIVE)
            ), TileType.Type_0
        )
        rootService.playerTurnService.rotateTileRight(testTile3)
        rootService.playerTurnService.placeRouteTile(Coordinate(0, 1), testTile3)
        val thirdPlacedTile = rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 1)]
        assertEquals(10, rootService.currentGame!!.gems.size)
        assertEquals(5, rootService.currentGame!!.middleTile.gemPosition.size)
        assertEquals(0, thirdPlacedTile!!.gemEndPosition.size)
        assertTrue(refreshableTest.refreshAfterRightRotationCalled)
        assertTrue(refreshableTest.refreshAfterMoveGemsCalled)
        assertTrue(refreshableTest.refreshAfterCollisionCalled)
        refreshableTest.reset()

        testTile2.gemEndPosition.clear()
        testTile1.gemEndPosition.clear()
        testTile1.gemEndPosition[1] = Gem(AMBER)
        testTile2.gemEndPosition[4] = Gem(AMBER)

        rootService.playerTurnService.placeRouteTile(Coordinate(-3, -1), testTile2)
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        refreshableTest.reset()
        rootService.playerTurnService.placeRouteTile(Coordinate(-2, -2), testTile1)
        assertTrue(refreshableTest.refreshAfterPlaceTileCalled)
        refreshableTest.reset()
        assertEquals(0, rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(-3, -1)]!!.gemEndPosition.size)
        assertEquals(0, rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(-2, -2)]!!.gemEndPosition.size)
        assertEquals(8, rootService.currentGame!!.gems.size)

        testTile2.gemEndPosition.clear()
        testTile1.gemEndPosition.clear()

    }


    /**
     * Test the removeGems function.
     */

    @Test
    fun removeGemsReachedGateTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterRemoveGemsCalled)
        //tileID 0 initialisieren
        val tile0 = Tile(
            listOf(Pair(Edge.ZERO, Edge.TWO), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.THREE, Edge.FIVE)),
            TileType.Type_0,
            mutableMapOf(Pair(0, Gem(EMERALD)), Pair(5, Gem(AMBER)))
        )

        //tileID 2 initialisieren
        val tile2 = Tile(
            listOf(Pair(Edge.ZERO, Edge.FIVE), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.TWO, Edge.THREE)),
            TileType.Type_2,
            mutableMapOf(Pair(3, Gem(EMERALD)), Pair(4, Gem(SAPPHIRE)))
        )

        //tileID 4 initialisieren
        val tile4 = Tile(
            listOf(Pair(Edge.ZERO, Edge.FIVE), Pair(Edge.ONE, Edge.TWO), Pair(Edge.THREE, Edge.FOUR)),
            TileType.Type_4,
            mutableMapOf(Pair(2, Gem(AMBER)), Pair(3, Gem(EMERALD)))
        )
        assertThrows<IllegalStateException> { rootService.gameService.removeGemsReachedGate(tile0, Coordinate(4, -2)) }
        rootService.gameService.startGame(
            fourPlayers.toMutableList()
        )

        var indigo = rootService.currentGame
        checkNotNull(indigo)

        var players = indigo.players

        //gate4 no gems after the method because is removed
        rootService.gameService.removeGemsReachedGate(tile0, Coordinate(-1, -3))
        //test refreshable
        assertTrue(refreshableTest.refreshAfterRemoveGemsCalled)
        refreshableTest.reset()
        // assertEquals(0, tile0.gemEndPosition.size)
        assertEquals(2, players[2].collectedGems.size)
        assertEquals(3, players[2].score)
        assertEquals(2, players[3].collectedGems.size)
        assertEquals(3, players[3].score)
        assertEquals(10, rootService.currentGame!!.gems.size)
        //gate3 only one Gem is there
        rootService.gameService.removeGemsReachedGate(tile2, Coordinate(2, -4))
        assertEquals(1, tile2.gemEndPosition.size)
        assertEquals(3, players[2].collectedGems.size)
        assertEquals(6, players[2].score)
        assertEquals(1, players[0].collectedGems.size)
        assertEquals(3, players[0].score)
        assertEquals(9, rootService.currentGame!!.gems.size)

        //gate2 both gems are in the tile
        rootService.gameService.removeGemsReachedGate(tile4, Coordinate(2, 2))
        assertEquals(0, tile4.gemEndPosition.size)
        assertEquals(3, players[0].collectedGems.size)
        assertEquals(6, players[0].score)
        assertEquals(4, players[3].collectedGems.size)
        assertEquals(6, players[3].score)
        assertEquals(7, rootService.currentGame!!.gems.size)

        // starte game with two players
        //tileID 0 initialisieren
        val tile00 = Tile(
            listOf(Pair(Edge.ZERO, Edge.TWO), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.THREE, Edge.FIVE)),
            TileType.Type_0,
            mutableMapOf(Pair(0, Gem(EMERALD)))
        )

        //tileID 2 initialisieren
        val tile22 = Tile(
            listOf(Pair(Edge.ZERO, Edge.FIVE), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.TWO, Edge.THREE)),
            TileType.Type_2,
            mutableMapOf(Pair(3, Gem(EMERALD)))
        )

        //tileID 4 initialisieren
        val tile44 = Tile(
            listOf(Pair(Edge.ZERO, Edge.FIVE), Pair(Edge.ONE, Edge.TWO), Pair(Edge.THREE, Edge.FOUR)),
            TileType.Type_4,
            mutableMapOf(Pair(2, Gem(SAPPHIRE)))
        )
        val twoPlayers = listOf(
            Player("John", color = TokenColor.RED), Player("Jack", color = TokenColor.BLUE)
        )
        rootService.gameService.startGame(
            twoPlayers.toMutableList(), true
        )


        indigo = rootService.currentGame
        checkNotNull(indigo)

        players = indigo.players

        //gate0 no gems after the method because is removed
        rootService.gameService.removeGemsReachedGate(tile00, Coordinate(-2, -2))
        assertTrue(refreshableTest.refreshAfterRemoveGemsCalled)
        refreshableTest.reset()
        //assertEquals(2, tile0.gemEndPosition.size)
        assertEquals(0, tile00.gemEndPosition.size)
        assertEquals(0, players[0].collectedGems.size)
        assertEquals(0, players[0].score)
        assertEquals(1, players[1].collectedGems.size)
        assertEquals(2, players[1].score)
        assertEquals(11, rootService.currentGame!!.gems.size)

        //gate4 only on Gem is there
        rootService.gameService.removeGemsReachedGate(tile22, Coordinate(4, -1))
        assertEquals(0, tile22.gemEndPosition.size)
        assertEquals(0, players[0].collectedGems.size)
        assertEquals(0, players[0].score)
        assertEquals(2, players[1].collectedGems.size)
        assertEquals(4, players[1].score)
        assertEquals(10, rootService.currentGame!!.gems.size)

        //gate3 both gems are in the tile
        rootService.gameService.removeGemsReachedGate(tile44, Coordinate(1, 3))
        assertEquals(0, tile44.gemEndPosition.size)
        assertEquals(1, players[0].collectedGems.size)
        assertEquals(3, players[0].score)
        assertEquals(2, players[1].collectedGems.size)
        assertEquals(4, players[1].score)
        assertEquals(9, rootService.currentGame!!.gems.size)

    }

    /**
     * Test the distributeNewTile function.
     */
    @Test
    fun distributeNewTileTest() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterDistributeNewTileCalled)
        assertThrows<IllegalStateException> { rootService.gameService.distributeNewTile() }
        val allTiles = mutableListOf<Tile>()
        for (i in 0 until 6) {
            val gemPos = (i + 3) % 6
            allTiles.add(
                Tile(
                    listOf(
                        Pair(
                            Edge.values()[(Edge.values().size + gemPos - 1) % 6],
                            Edge.values()[(Edge.values().size + gemPos + 1) % 6]
                        )
                    ), TileType.Type_0, mutableMapOf(Pair(gemPos, Gem(AMBER)))
                )
            )
        }
        val routeTiles = createTestRouteTile()
        allTiles.addAll(routeTiles)
        val testSettings = GameSettings(fourPlayers)
        rootService.currentGame = Indigo(
            testSettings,
            GameBoard(),
            allTiles,
            RootService().gameService.initializeGems(),
            RootService().gameService.initializeTokens()
        )
        val testGame = rootService.currentGame
        testGame!!.gameBoard.gateTokens = createTestGateTokens(testGame, false)

        // test refreshable
        assertFalse(refreshableTest.refreshAfterDistributeNewTileCalled)
        rootService.gameService.distributeNewTile()
        assertTrue(refreshableTest.refreshAfterDistributeNewTileCalled)
        refreshableTest.reset()

        var testTile = testGame.players[0].handTile
        assertNotNull(testTile)
        assertEquals(tile0, testTile)
        testGame.routeTiles.clear()
        gameService.distributeNewTile()
        testTile = testGame.players[0].handTile
        assertNull(testTile)
    }

    /**
     * Test the initializeGems function.
     */

    @Test
    fun initializeGemsTest() {
        assertNull(rootService.currentGame)
        rootService.gameService.startGame(
            fourPlayers.toMutableList()
        )
        val game = rootService.currentGame
        checkNotNull(game)
        val amber = AMBER
        val emerald = EMERALD
        val sapphire = SAPPHIRE

        for (i in game.gems.indices) {
            if (i in 0 until 6) {
                assertEquals(amber, game.gems[i].gemColor)
            }
            if (i in 6 until 11) {
                assertEquals(emerald, game.gems[i].gemColor)
            }
            assertEquals(sapphire, game.gems[game.gems.size - 1].gemColor)
        }
    }

    /**
     * Initializes token test for a specific scenario.
     */
    @Test
    fun initializeTokenTest() {
        assertNull(rootService.currentGame)
        rootService.gameService.startGame(
            fourPlayers.toMutableList()
        )
        val game = rootService.currentGame
        checkNotNull(game)
        val white = TokenColor.WHITE
        val purple = TokenColor.PURPLE
        val blue = TokenColor.BLUE
        val red = TokenColor.RED

        val amountBlue = game.tokens.count { it == Token(blue) }
        val amountPurple = game.tokens.count { it == Token(purple) }
        val amountWhite = game.tokens.count { it == Token(white) }
        val amountRed = game.tokens.count { it == Token(red) }
        assertEquals(6, amountRed)
        assertEquals(6, amountWhite)
        assertEquals(6, amountBlue)
        assertEquals(6, amountPurple)
        for (i in game.tokens.indices) {
            if (i in 0 until 6) {
                assertEquals(white, game.tokens[i].color)
            }
            if (i in 6 until 12) {
                assertEquals(purple, game.tokens[i].color)
            }
            if (i in 12 until 18) {
                assertEquals(blue, game.tokens[i].color)
            }
            if (i in 18 until 24) {
                assertEquals(red, game.tokens[i].color)
            }
        }
    }

    /**
     * Tests the scenario where the second player is controlled by artificial intelligence (KI).
     */
    @Test
    fun testSecondPlayerIsKI() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        assertFalse(refreshableTest.refreshAfterAITurnCalled)
        assertNull(rootService.currentGame)

        val twoPlayer = mutableListOf(
            Player("Alice", Date(0), TokenColor.WHITE, false), Player("Bob", Date(0), TokenColor.PURPLE, true)
        )
        rootService.gameService.startGame(twoPlayer)
        rootService.gameService.changePlayer()
        //assertTrue(refreshableTest.refreshAfterAITurnCalled)
        refreshableTest.reset()
        val testGame = rootService.currentGame
        checkNotNull(testGame)
        //tileID 0 initialisieren
        val tile0 = Tile(
            listOf(Pair(Edge.ZERO, Edge.TWO), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.THREE, Edge.FIVE)),
            TileType.Type_0,
            mutableMapOf(Pair(1, Gem(EMERALD)))
        )
        assertEquals(true, testGame.players[1].isAI)
        assertEquals(false, testGame.players[0].isAI)
        rootService.playerTurnService.placeRouteTile(Coordinate(-2, 4), tile0)
        assertEquals(true, testGame.players[1].isAI)
        assertEquals(false, testGame.players[0].isAI)
        rootService.playerTurnService.placeRouteTile(Coordinate(-1, 4), tile0)
        assertEquals(true, testGame.players[1].isAI)
        assertEquals(false, testGame.players[0].isAI)
    }

    /**
     * Tests the undo functionality for a specific scenario (test case).
     */
    @Test
    fun testUndo2() {
        val refreshableTest = RefreshableTest()
        rootService.addRefreshable(refreshableTest)
        //tileID 2 initialise
        val tile2 = Tile(
            listOf(Pair(Edge.ZERO, Edge.FIVE), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.TWO, Edge.THREE)),
            TileType.Type_2,
            mutableMapOf(Pair(1, Gem(EMERALD)))
        )
        // second tileID 2 initialise
        val tile3 = Tile(
            listOf(Pair(Edge.ZERO, Edge.FIVE), Pair(Edge.ONE, Edge.FOUR), Pair(Edge.TWO, Edge.THREE)),
            TileType.Type_2,
        )
        val game = rootService.currentGame
        assertNull(game)

        val twoPlayer = mutableListOf(
            Player("Alice", Date(0), TokenColor.WHITE, false), Player("Bob", Date(0), TokenColor.PURPLE, true)
        )
        rootService.gameService.startGame(twoPlayer)
        checkNotNull(rootService.currentGame)
        rootService.currentGame!!.players[0].handTile = tile2
        //val player1HandTile = rootService.currentGame!!.players[0].handTile

        rootService.playerTurnService.placeRouteTile(Coordinate(0, 2), tile2)
        rootService.currentGame!!.players[1].handTile = tile3
        rootService.playerTurnService.placeRouteTile(Coordinate(-1, 3), tile3)
        assertEquals(0, rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 2)]!!.gemEndPosition.size)
        assertEquals(1, rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(-1, 3)]!!.gemEndPosition.size)

        rootService.playerTurnService.undo()
        rootService.playerTurnService.undo()
        assertNull(rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(-1, 3)])
        assertNotNull(rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 2)])
        assertEquals(1, rootService.currentGame!!.gameBoard.gameBoardTiles[Coordinate(0, 2)]!!.gemEndPosition.size)
    }
}
