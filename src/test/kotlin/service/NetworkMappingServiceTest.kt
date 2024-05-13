package service

import edu.udo.cs.sopra.ntf.GameMode
import edu.udo.cs.sopra.ntf.TileType
import edu.udo.cs.sopra.ntf.Player
import edu.udo.cs.sopra.ntf.PlayerColor
import entity.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


/**
 *  The class [NetworkMappingServiceTest] is testing
 *  all function of the NetworkMappingService.
 */
class NetworkMappingServiceTest {
    private val players = listOf(
        Player(name = "John", color = TokenColor.PURPLE),
        Player(name = "Alice", color = TokenColor.BLUE),
        Player(name = "Bob", color = TokenColor.WHITE),
    )
    private val gameSetting = GameSettings(players)
    private val tile0 = listOf(
        Pair(Edge.ZERO, Edge.TWO),
        Pair(Edge.ONE, Edge.FOUR),
        Pair(Edge.THREE, Edge.FIVE)
    )
    private val tile1 = listOf(
        Pair(Edge.TWO, Edge.FIVE),
        Pair(Edge.ONE, Edge.FOUR),
        Pair(Edge.ZERO, Edge.THREE)
    )
    private val tile2 = listOf(
        Pair(Edge.ZERO, Edge.FIVE),
        Pair(Edge.ONE, Edge.FOUR),
        Pair(Edge.TWO, Edge.THREE)
    )
    private val tile3 = listOf(
        Pair(Edge.ZERO, Edge.FIVE),
        Pair(Edge.ONE, Edge.THREE),
        Pair(Edge.TWO, Edge.FOUR)
    )
    private val tile4 = listOf(
        Pair(Edge.ZERO, Edge.FIVE),
        Pair(Edge.ONE, Edge.TWO),
        Pair(Edge.THREE, Edge.FOUR)
    )
    private val treasureTiles = mutableListOf(
        Tile(listOf(),entity.TileType.Type_5),
        Tile(listOf(),entity.TileType.Type_5),
        Tile(listOf(),entity.TileType.Type_5),
        Tile(listOf(),entity.TileType.Type_5),
        Tile(listOf(),entity.TileType.Type_5),
        Tile(listOf(),entity.TileType.Type_5),
    )
    private val routeTiles = mutableListOf(
        Tile(tile0,entity.TileType.Type_0),
        Tile(tile1,entity.TileType.Type_1),
        Tile(tile2,entity.TileType.Type_2),
        Tile(tile3,entity.TileType.Type_3),
        Tile(tile4,entity.TileType.Type_4)
    )
    private val threeNotSharedTokens = mutableListOf(
        Token(TokenColor.PURPLE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.BLUE),
        Token(TokenColor.BLUE),
        Token(TokenColor.WHITE),
        Token(TokenColor.WHITE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.BLUE),
        Token(TokenColor.BLUE),
        Token(TokenColor.WHITE),
        Token(TokenColor.WHITE)
    )

    private val threeSharedTokens = mutableListOf(
        Token(TokenColor.PURPLE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.BLUE),
        Token(TokenColor.WHITE),
        Token(TokenColor.WHITE),
        Token(TokenColor.WHITE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.BLUE),
        Token(TokenColor.BLUE),
        Token(TokenColor.BLUE),
        Token(TokenColor.WHITE)
    )

    private val twoNotSharedTokens = mutableListOf(
        Token(TokenColor.PURPLE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.BLUE),
        Token(TokenColor.BLUE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.BLUE),
        Token(TokenColor.BLUE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.BLUE),
        Token(TokenColor.BLUE)
    )

    private val fourSharedTokens = mutableListOf(
        Token(TokenColor.PURPLE),
        Token(TokenColor.BLUE),
        Token(TokenColor.BLUE),
        Token(TokenColor.WHITE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.RED),
        Token(TokenColor.RED),
        Token(TokenColor.BLUE),
        Token(TokenColor.WHITE),
        Token(TokenColor.PURPLE),
        Token(TokenColor.WHITE),
        Token(TokenColor.RED)
    )

    private val gems = mutableListOf(
        Gem(GemColor.SAPPHIRE),
        Gem(GemColor.EMERALD),
        Gem(GemColor.EMERALD),
        Gem(GemColor.AMBER),
        Gem(GemColor.AMBER),
        Gem(GemColor.AMBER),
    )

    /**
     * The function [toGameModeTest] test all function of toGameMode with alle functionality
     */
    @Test
    fun toGameModeTest() {
        val testGame = RootService()
        assertThrows<IllegalStateException> { (testGame.networkMappingService.toGameMode()) }
        testGame.currentGame = Indigo(
            gameSetting,
            allTiles = treasureTiles.toList(),
            gameBoard = GameBoard(),
            gems = gems,
            tokens = threeNotSharedTokens
        )
        testGame.currentGame!!.gameBoard.gateTokens = threeNotSharedTokens
        var gameMode = testGame.networkMappingService.toGameMode()
        assertEquals(GameMode.THREE_NOT_SHARED_GATEWAYS, gameMode)
        testGame.currentGame = Indigo(
            gameSetting,
            allTiles = treasureTiles.toList(),
            gameBoard = GameBoard(),
            gems = gems,
            tokens = threeSharedTokens
        )
        testGame.currentGame!!.gameBoard.gateTokens = threeSharedTokens
        gameMode = testGame.networkMappingService.toGameMode()
        assertEquals(GameMode.THREE_SHARED_GATEWAYS, gameMode)
        val fourPlayers = players.toMutableList()
        fourPlayers.add(Player("Charlie", color = TokenColor.RED))
        var gameSettings = GameSettings(fourPlayers.toList())
        val twoPlayers = players.subList(0, 2)
        testGame.currentGame = Indigo(
            gameSettings,
            allTiles = treasureTiles.toList(),
            gameBoard = GameBoard(),
            gems = gems,
            tokens = fourSharedTokens
        )
        testGame.currentGame!!.gameBoard.gateTokens = fourSharedTokens
        gameMode = testGame.networkMappingService.toGameMode()
        assertEquals(GameMode.FOUR_SHARED_GATEWAYS, gameMode)

        gameSettings = GameSettings(twoPlayers)
        testGame.currentGame = Indigo(
            gameSettings,
            allTiles = treasureTiles.toList(),
            gameBoard = GameBoard(),
            gems = gems,
            tokens = twoNotSharedTokens
        )
        testGame.currentGame!!.gameBoard.gateTokens = twoNotSharedTokens
        gameMode = testGame.networkMappingService.toGameMode()
        assertEquals(GameMode.TWO_NOT_SHARED_GATEWAYS, gameMode)
    }

    /**
     *  The function[toTileTypeListTest] test the function to toTileTypeList
     */
    @Test
    fun toTileTypeListTest() {
        val testGame = RootService()
        assertThrows<IllegalStateException> { (testGame.networkMappingService.toTileTypeList()) }
        treasureTiles.addAll(routeTiles)
        testGame.currentGame = Indigo(
            gameSetting,
            allTiles = treasureTiles.toList(),
            gameBoard = GameBoard(),
            gems = gems,
            tokens = threeSharedTokens
        )
        var testTileList = listOf(
            TileType.TYPE_0,
            TileType.TYPE_1,
            TileType.TYPE_2,
            TileType.TYPE_3,
            TileType.TYPE_4
        )
        repeat(players.size) {
            testGame.gameService.distributeNewTile()
            testGame.gameService.changePlayer()
        }
        var tileList = testGame.networkMappingService.toTileTypeList()
        assertEquals(testTileList, tileList)
        testTileList = listOf(
            TileType.TYPE_1,
            TileType.TYPE_2,
            TileType.TYPE_4,
            TileType.TYPE_3,
            TileType.TYPE_0
        )
        val testRouteTiles = listOf(
            Tile(tile1,entity.TileType.Type_1 ,mutableMapOf()),
            Tile(tile2,entity.TileType.Type_2 ,mutableMapOf()),
            Tile(tile4,entity.TileType.Type_4 ,mutableMapOf()),
            Tile(tile3,entity.TileType.Type_3 ,mutableMapOf()),
            Tile(tile0,entity.TileType.Type_0,mutableMapOf())
        )
        val allTiles = treasureTiles.take(6).toMutableList()
        allTiles.addAll(testRouteTiles)
        testGame.currentGame = Indigo(
            gameSetting,
            allTiles = allTiles,
            gameBoard = GameBoard(),
            gems = gems,
            tokens = threeSharedTokens
        )
        repeat(players.size) {
            testGame.gameService.distributeNewTile()
            testGame.gameService.changePlayer()
        }
        tileList = testGame.networkMappingService.toTileTypeList()
        assertEquals(testTileList, tileList)
        assertEquals(2, testGame.currentGame!!.routeTiles.size)
    }

    /**
     *The function [toNetworkPlayerTest] make test the function of the entity
     *if all translating from the entity player model to the network player model
     * are correct.
     */
    @Test
    fun toNetworkPlayerTest() {
        val testGame = RootService()
        assertThrows<IllegalStateException> { (testGame.networkMappingService.toNetworkPlayer()) }
        val fourPlayers = players.toMutableList()
        fourPlayers.add(Player("Charlie", color = TokenColor.RED))
        val gameSettings = GameSettings(fourPlayers.toList())
        testGame.currentGame = Indigo(
            gameSettings,
            allTiles = treasureTiles.toList(),
            gameBoard = GameBoard(),
            gems = gems,
            tokens = threeSharedTokens
        )
        val networkPlayerListResult = listOf(
            Player(name = "John", color = PlayerColor.PURPLE),
            Player(name = "Alice", color = PlayerColor.BLUE),
            Player(name = "Bob", color = PlayerColor.WHITE),
            Player("Charlie", color = PlayerColor.RED)
        )
        val networkPlayerList = testGame.networkMappingService.toNetworkPlayer()
        assertEquals(networkPlayerListResult, networkPlayerList)
    }

    /**
     * The test function [toRouteTilesTest] test the translating
     * from the netWork model to the entity model
     */
    @Test
    fun toRouteTilesTest() {
        val testGame = RootService()
        val tileList = listOf(
            TileType.TYPE_0,
            TileType.TYPE_1,
            TileType.TYPE_2,
            TileType.TYPE_3,
            TileType.TYPE_4
        )
        val routeTilesResult = routeTiles
        val routeTileList = testGame.networkMappingService.toRouteTiles(tileList)
        assertEquals(routeTilesResult, routeTileList)
    }

    /**
     * The test function[toEntityPlayerTest] test the correct
     * translation from player list from the network model
     * to the player list from the entity model
     */
    @Test
    fun toEntityPlayerTest() {
        val testGame = RootService()
        val networkPlayerList = listOf(
            Player(name = "John", color = PlayerColor.PURPLE),
            Player(name = "Alice", color = PlayerColor.BLUE),
            Player(name = "Bob", color = PlayerColor.WHITE),
            Player("Charlie", color = PlayerColor.RED)
        )
        val entityPlayersResult = players.toMutableList()
        entityPlayersResult.add(Player("Charlie", color = TokenColor.RED))
        val entityPlayersActual = testGame.networkMappingService.toEntityPlayer(networkPlayerList)
        assertEquals(entityPlayersResult.size, entityPlayersActual.size)
        for (i in entityPlayersResult.indices) {
            assertEquals(entityPlayersResult[i].name, entityPlayersResult[i].name)
            assertEquals(entityPlayersResult[i].color, entityPlayersResult[i].color)
        }
    }

    /**
     *  The [toGateTokensTest] test the mapping from the network game mode
     *  to the correct gateTokens
     */
    @Test
    fun toGateTokensTest() {
        val testGame = RootService()
        val twoPlayers = players.subList(0, 2).toList()
        var resultGateToken = testGame.networkMappingService.toGateTokens(twoPlayers, GameMode.TWO_NOT_SHARED_GATEWAYS)
        assertEquals(twoNotSharedTokens, resultGateToken)
        val fourPlayers = players.toMutableList()
        fourPlayers.add(Player("Charlie", color = TokenColor.RED))
        resultGateToken =
            testGame.networkMappingService.toGateTokens(fourPlayers.toList(), GameMode.FOUR_SHARED_GATEWAYS)
        assertEquals(fourSharedTokens, resultGateToken)
        resultGateToken = testGame.networkMappingService.toGateTokens(players, GameMode.THREE_NOT_SHARED_GATEWAYS)
        assertEquals(threeNotSharedTokens, resultGateToken)
        resultGateToken = testGame.networkMappingService.toGateTokens(players, GameMode.THREE_SHARED_GATEWAYS)
        assertEquals(threeSharedTokens, resultGateToken)
    }
}