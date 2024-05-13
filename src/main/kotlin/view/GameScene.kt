package view

import entity.*
import entity.Coordinate
import service.network.ConnectionState
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.style.BorderColor
import tools.aqua.bgw.style.BorderWidth
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import view.components.Label

/**
 * Represents the game scene containing a hexagonal grid.
 * @param indigoApp The IndigoApplication instance associated with the game scene.
 */

class GameScene(val indigoApp: IndigoApplication) :
    BoardGameScene(1920, 1080, background = ImageVisual("plain_background.png")), Refreshable {
    private val gemMap = mutableMapOf<Gem, Label>()
    private val rootService = indigoApp.rootService
    private var rotationDegree = 0
    private val middleMap = mutableMapOf(
        0 to Position(916.0, 438.0),
        1 to Position(894.0, 410.0),
        2 to Position(933.0, 408.0),
        3 to Position(948.0, 442.0),
        4 to Position(914.0, 468.0),
        5 to Position(881.0, 443.0)
    )

    private val gemEndPos = mutableMapOf(
        0 to Position(0.0, -36.0),
        1 to Position(28.0, -19.0),
        2 to Position(28.0, 19.0),
        3 to Position(0.0, 36.0),
        4 to Position(-34.0, 19.0),
        5 to Position(-34.0, -19.0)
    )

    //maps the grid coordinates auf (posX,posY) on the Scene where the middle of the tile would be
    private val coordMap = mutableMapOf<Coordinate, Position>()

    //view von dem angeklickten Place am GameBoard (für Highlighten)
    private var chosenPlace: HexagonView? = null

    // Coordinaten vom angeklicktem Place am GameBoard(zum Platzieren)
    private var chosenCol: Int? = null
    private var chosenRow: Int? = null

    //View von dem Tile vom currentPlayer (zum Platzieren)
    //private var tileToPlace: HexagonView? = null //braucht man nicht mehr

    // Hexagonal grid for the game board
    private val hexagonGrid: HexagonGrid<HexagonView> =
        HexagonGrid<HexagonView>(
            coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL,
            posX = 820,
            posY = 420
        ).apply { rotate(30) }

    // reserveStack component
    private val reserveStack = HexagonView(posX = 869, posY = 870, visual = ImageVisual("plaintile.png"))

    // undo Button component
    val undoButton =
        view.components.Button(posX = 650, posY = 880, width = 160, height = 68, text = "Undo", fontSize = 40).apply {
            if (indigoApp.networkMode) {
                isVisible = false
                isDisabled = true
            }

            onMouseClicked = { indigoApp.rootService.playerTurnService.undo() }
        }

    // redoButton component
    val redoButton =
        view.components.Button(posX = 650, posY = 980, width = 160, height = 68, text = "Redo", fontSize = 40).apply {
            if (indigoApp.networkMode) {
                isVisible = false
                isDisabled = true
            }
            onMouseClicked = { indigoApp.rootService.playerTurnService.redo() }
        }

    // saveButton component
    val saveButton =
        view.components.Button(posX = 1055, posY = 980, width = 160, height = 68, text = "Save", fontSize = 40)
            .apply { onMouseClicked = { indigoApp.showMenuScene(indigoApp.saveGameScene) } }

    // Player components
    private var player1Label =
        Label(posX = 110, posY = 68, width = 200, text = "Player 1", fontSize = 48)
    private var player2Label =
        Label(posX = 1573, posY = 68, width = 200, text = "Player 2", fontSize = 48)
    private var player3Label =
        Label(posX = 143, posY = 917, width = 200, text = "Player 3", fontSize = 48)
    private var player4Label =
        Label(posX = 1579, posY = 917, width = 200, text = "Player 4", fontSize = 48)

    // PlayerScore components
    private var player1ScoreLabel =
        Label(posX = 110, posY = 114, width = 200, text = "0 points", fontSize = 48)
    private var player2ScoreLabel =
        Label(posX = 1573, posY = 114, width = 200, text = "0 points", fontSize = 48)
    private var player3ScoreLabel =
        Label(posX = 143, posY = 964, width = 200, text = "0 points", fontSize = 48)
    private var player4ScoreLabel =
        Label(posX = 1579, posY = 964, width = 200, text = "0 points", fontSize = 48)

    // PlayerToken components
    private var player1Token = Label(posX = 60, posY = 70, text = "")
    private var player2Token = Label(posX = 1523, posY = 70, text = "")
    private var player3Token = Label(posX = 93, posY = 921, text = "")
    private var player4Token = Label(posX = 1529, posY = 921, text = "")

    // PlayerGreenGem components
    private var player1greenGem = Label(posX = 399, posY = 142, text = "", width = 81, height = 65)
    private var player2greenGem = Label(posX = 1351, posY = 142, text = "", width = 81, height = 65)
    private var player3greenGem = Label(posX = 399, posY = 830, text = "", width = 81, height = 65)
    private var player4greenGem = Label(posX = 1351, posY = 830, text = "", width = 81, height = 65)

    // PlayerYellowGem components
    private var player1yellowGem = Label(posX = 490, posY = 86, text = "", width = 81, height = 65)
    private var player2yellowGem = Label(posX = 1262, posY = 86, text = "", width = 81, height = 65)
    private var player3yellowGem = Label(posX = 490, posY = 764, text = "", width = 81, height = 65)
    private var player4yellowGem = Label(posX = 1262, posY = 764, text = "", width = 81, height = 65)

    // PlayerGreenGemCounter components
    private var player1greenGemCounter = Label(posX = 413, posY = 95, text = "0", fontSize = 48)
    private var player2greenGemCounter = Label(posX = 1365, posY = 95, text = "0", fontSize = 48)
    private var player3greenGemCounter = Label(posX = 413, posY = 783, text = "0", fontSize = 48)
    private var player4greenGemCounter = Label(posX = 1365, posY = 783, text = "0", fontSize = 48)

    // PlayerYellowGemCounter components
    private var player1yellowGemCounter = Label(posX = 504, posY = 39, text = "0", fontSize = 48)
    private var player2yellowGemCounter = Label(posX = 1276, posY = 39, text = "0", fontSize = 48)
    private var player3yellowGemCounter = Label(posX = 504, posY = 717, text = "0", fontSize = 48)
    private var player4yellowGemCounter = Label(posX = 1276, posY = 717, text = "0", fontSize = 48)

    //BlueGem appears when collected by player/players
    private val blueGemCounter1 = Label(posX = 411, posY = 240, text = "", width = 81, height = 75).apply {
        visual = ImageVisual("blue_gem.png")
        isVisible = false
    }

    private val blueGemCounter2 = Label(posX = 1365, posY = 240, text = "", width = 81, height = 75).apply {
        visual = ImageVisual("blue_gem.png")
        isVisible = false
    }

    private val blueGemCounter3 = Label(posX = 492, posY = 904, text = "", width = 81, height = 75).apply {
        visual = ImageVisual("blue_gem.png")
        isVisible = false
    }

    private val blueGemCounter4 = Label(posX = 1271, posY = 904, text = "", width = 81, height = 75).apply {
        visual = ImageVisual("blue_gem.png")
        isVisible = false
    }


    // PlayerHandTile components
    // player1 oben links player2 oben rechts player3 unten links player4 unten rechts
    private var player1handTile = HexagonView(posX = 122, posY = 184, size = 70.0, visual = ImageVisual("tile1.png"))
        .apply { rotate(-30) }
    private var player2handTile = HexagonView(posX = 1611, posY = 184, size = 70.0, visual = ImageVisual("tile1.png"))
        .apply { rotate(-30) }
    private var player3handTile = HexagonView(posX = 122, posY = 754, size = 70.0, visual = ImageVisual("tile1.png"))
        .apply { rotate(-30) }
    private var player4handTile = HexagonView(posX = 1611, posY = 754, size = 70.0, visual = ImageVisual("tile1.png"))
        .apply { rotate(-30) }


    // PlayerturnHighlight components (currentPlayer bekommt einen blauen Hintergrund (egal welcher Player))
    private val player1turnHighlight =
        HexagonView(posX = 112, posY = 174, size = 80.0, visual = ColorVisual(Color.BLUE))
            .apply { rotate(30) }
    private val player2turnHighlight =
        HexagonView(posX = 1600, posY = 174, size = 80.0, visual = ColorVisual(Color.BLUE))
            .apply { rotate(30) }
    private val player3turnHighlight =
        HexagonView(posX = 112, posY = 744, size = 80.0, visual = ColorVisual(Color.BLUE))
            .apply { rotate(30) }
    private val player4turnHighlight =
        HexagonView(posX = 1600, posY = 744, size = 80.0, visual = ColorVisual(Color.BLUE))
            .apply { rotate(30) }

    // PlayerRotateButton components
    private val player1leftButton =
        Button(posX = 60, posY = 360, width = 80, height = 50, visual = ImageVisual("leftbutton.png"))
    private val player1rightButton =
        Button(posX = 240, posY = 360, width = 80, height = 50, visual = ImageVisual("rightbutton.png"))
    private val player1checkButton =
        Button(posX = 150, posY = 360, width = 80, height = 50, visual = ImageVisual("checkbutton.png"))

    private val player2leftButton =
        Button(posX = 1553, posY = 360, width = 80, height = 50, visual = ImageVisual("leftbutton.png"))
    private val player2rightButton =
        Button(posX = 1733, posY = 360, width = 80, height = 50, visual = ImageVisual("rightbutton.png"))
    private val player2checkButton =
        Button(posX = 1643, posY = 360, width = 80, height = 50, visual = ImageVisual("checkbutton.png"))

    private val player3leftButton =
        Button(posX = 60, posY = 680, width = 80, height = 50, visual = ImageVisual("leftbutton.png"))
    private val player3rightButton =
        Button(posX = 240, posY = 680, width = 80, height = 50, visual = ImageVisual("rightbutton.png"))
    private val player3checkButton =
        Button(posX = 150, posY = 680, width = 80, height = 50, visual = ImageVisual("checkbutton.png"))

    private val player4leftButton =
        Button(posX = 1553, posY = 680, width = 80, height = 50, visual = ImageVisual("leftbutton.png"))
    private val player4rightButton =
        Button(posX = 1733, posY = 680, width = 80, height = 50, visual = ImageVisual("rightbutton.png"))
    private val player4checkButton =
        Button(posX = 1643, posY = 680, width = 80, height = 50, visual = ImageVisual("checkbutton.png"))


    // GateToken components
    //gate oben links und dann im Uhrzeigersinn
    private var gate1Token1 = Label(posX = 666, posY = 105, text = "")
    private var gate1Token2 = Label(posX = 748, posY = 57, text = "")

    private var gate2Token1 = Label(posX = 1065, posY = 57, text = "")
    private var gate2Token2 = Label(posX = 1147, posY = 105, text = "")

    private var gate3Token1 = Label(posX = 1306, posY = 385, text = "")
    private var gate3Token2 = Label(posX = 1306, posY = 475, text = "")

    private var gate4Token1 = Label(posX = 1147, posY = 750, text = "")
    private var gate4Token2 = Label(posX = 1065, posY = 797, text = "")

    private var gate5Token1 = Label(posX = 748, posY = 797, text = "")
    private var gate5Token2 = Label(posX = 666, posY = 750, text = "")

    private var gate6Token1 = Label(posX = 510, posY = 475, text = "")
    private var gate6Token2 = Label(posX = 510, posY = 385, text = "")

    // GameboardGem components
    private var blueGem = Label(posX = 916, posY = 438, text = "", width = 30, height = 30)

    //oben links im Uhrzeigersinn
    private var greenGem1 = Label(posX = 894, posY = 410, text = "", width = 30, height = 30)
    private var greenGem2 = Label(posX = 933, posY = 408, text = "", width = 30, height = 30)
    private var greenGem3 = Label(posX = 948, posY = 442, text = "", width = 30, height = 30)
    private var greenGem4 = Label(posX = 914, posY = 468, text = "", width = 30, height = 30)
    private var greenGem5 = Label(posX = 881, posY = 443, text = "", width = 30, height = 30)

    //oben im Uhrzeigersinn
    private var yellowGem1 = Label(posX = 914, posY = 70, text = "", width = 30, height = 30)
    private var yellowGem2 = Label(posX = 1230, posY = 254, text = "", width = 30, height = 30)
    private var yellowGem3 = Label(posX = 1230, posY = 619, text = "", width = 30, height = 30)
    private var yellowGem4 = Label(posX = 914, posY = 801, text = "", width = 30, height = 30)
    private var yellowGem5 = Label(posX = 590, posY = 619, text = "", width = 30, height = 30)
    private var yellowGem6 = Label(posX = 590, posY = 254, text = "", width = 30, height = 30)

    //GameComponents listed
    private val playerlabels = listOf(player1Label, player2Label, player3Label, player4Label)
    private val playerScores = listOf(player1ScoreLabel, player2ScoreLabel, player3ScoreLabel, player4ScoreLabel)
    private val playerGreenGemCounters =
        listOf(player1greenGemCounter, player2greenGemCounter, player3greenGemCounter, player4greenGemCounter)
    private val playerYellowGemCounters =
        listOf(player1yellowGemCounter, player2yellowGemCounter, player3yellowGemCounter, player4yellowGemCounter)
    private val playerGreenGems = listOf(player1greenGem, player2greenGem, player3greenGem, player4greenGem)
    private val playerYellowGems = listOf(player1yellowGem, player2yellowGem, player3yellowGem, player4yellowGem)
    private val playerTurnHighlights =
        listOf(player1turnHighlight, player2turnHighlight, player3turnHighlight, player4turnHighlight)
    private val playerLeftButtons = listOf(player1leftButton, player2leftButton, player3leftButton, player4leftButton)
    private val playerRightButtons =
        listOf(player1rightButton, player2rightButton, player3rightButton, player4rightButton)
    private val playerCheckButtons =
        listOf(player1checkButton, player2checkButton, player3checkButton, player4checkButton)
    private val playerHandtiles = listOf(player1handTile, player2handTile, player3handTile, player4handTile)
    private val playerTokens = listOf(player1Token, player2Token, player3Token, player4Token)
    private val blueGems = listOf(blueGemCounter1, blueGemCounter2, blueGemCounter3, blueGemCounter4)

    /**
     * Initializes the GameScene with default values and sets up the hexagonal grid.
     */
    init {

        initializeGameBoardGrid()
        //hexagonGrid.rotate(60)
        hexagonGrid.reposition(910, 380)

        //initialize reserveStack
        reserveStack.rotate(30)
        reserveStack.resize(width = 110, height = 110)
        reserveStack.scaleY(0.6)
        reserveStack.scaleX(0.6)

        //initialize tokenViews
        player1Token.visual = ImageVisual("token_white.png")
        player2Token.visual = ImageVisual("token_blue.png")
        player3Token.visual = ImageVisual("token_purple.png")
        player4Token.visual = ImageVisual("token_red.png")

        //initialize gemViews
        player1greenGem.visual = ImageVisual("green_gem.png")
        player2greenGem.visual = ImageVisual("green_gem.png")
        player3greenGem.visual = ImageVisual("green_gem.png")
        player4greenGem.visual = ImageVisual("green_gem.png")
        player1yellowGem.visual = ImageVisual("yellow_gem.png")
        player2yellowGem.visual = ImageVisual("yellow_gem.png")
        player3yellowGem.visual = ImageVisual("yellow_gem.png")
        player4yellowGem.visual = ImageVisual("yellow_gem.png")

        blueGem.visual = ImageVisual("blue_gem.png")
        greenGem1.visual = ImageVisual("green_gem.png")
        greenGem2.visual = ImageVisual("green_gem.png")
        greenGem3.visual = ImageVisual("green_gem.png")
        greenGem4.visual = ImageVisual("green_gem.png")
        greenGem5.visual = ImageVisual("green_gem.png")
        yellowGem1.visual = ImageVisual("yellow_gem.png")
        yellowGem2.visual = ImageVisual("yellow_gem.png")
        yellowGem3.visual = ImageVisual("yellow_gem.png")
        yellowGem4.visual = ImageVisual("yellow_gem.png")
        yellowGem5.visual = ImageVisual("yellow_gem.png")
        yellowGem6.visual = ImageVisual("yellow_gem.png")

        //initialize gateTokenViews
        gate1Token1.visual = ImageVisual("token_red.png")
        gate1Token2.visual = ImageVisual("token_blue.png")

        gate2Token1.visual = ImageVisual("token_red.png")
        gate2Token2.visual = ImageVisual("token_blue.png")

        gate3Token1.visual = ImageVisual("token_red.png")
        gate3Token2.visual = ImageVisual("token_blue.png")

        gate4Token1.visual = ImageVisual("token_red.png")
        gate4Token2.visual = ImageVisual("token_blue.png")

        gate5Token1.visual = ImageVisual("token_red.png")
        gate5Token2.visual = ImageVisual("token_blue.png")

        gate6Token1.visual = ImageVisual("token_red.png")
        gate6Token2.visual = ImageVisual("token_blue.png")


        // Add the hexagonal grid to the components of the game scene
        addComponents(
            hexagonGrid,
            reserveStack,
            undoButton,
            redoButton,
            saveButton,
            player1Label,
            player2Label,
            player3Label,
            player4Label,
            player1ScoreLabel,
            player2ScoreLabel,
            player3ScoreLabel,
            player4ScoreLabel,
            player1Token,
            player2Token,
            player3Token,
            player4Token,
            player1greenGem,
            player2greenGem,
            player3greenGem,
            player4greenGem,
            player1yellowGem,
            player2yellowGem,
            player3yellowGem,
            player4yellowGem,
            player1greenGemCounter,
            player2greenGemCounter,
            player3greenGemCounter,
            player4greenGemCounter,
            player1yellowGemCounter,
            player2yellowGemCounter,
            player3yellowGemCounter,
            player4yellowGemCounter,
            player1turnHighlight,
            player2turnHighlight,
            player3turnHighlight,
            player4turnHighlight,
            player1handTile,
            player2handTile,
            player3handTile,
            player4handTile,
            gate1Token1,
            gate1Token2,
            gate2Token1,
            gate2Token2,
            gate3Token1,
            gate3Token2,
            gate4Token1,
            gate4Token2,
            gate5Token1,
            gate5Token2,
            gate6Token1,
            gate6Token2,
            blueGem,
            greenGem1,
            greenGem2,
            greenGem3,
            greenGem4,
            greenGem5,
            yellowGem1,
            yellowGem2,
            yellowGem3,
            yellowGem4,
            yellowGem5,
            yellowGem6,
            blueGemCounter1,
            blueGemCounter2,
            blueGemCounter3,
            blueGemCounter4,
            player1leftButton,
            player1rightButton,
            player1checkButton,
            player2leftButton,
            player2rightButton,
            player2checkButton,
            player3leftButton,
            player3rightButton,
            player3checkButton,
            player4leftButton,
            player4rightButton,
            player4checkButton
        )
    }

    /**
     * invokes player actions to the buttons
     * @param players The list of players for whom buttons will be invoked or updated.
     */
    private fun invokeButtons(players: List<Player>) {
        val count = players.size

        for (i in 0 until count) {
            playerLeftButtons[i].onMouseClicked = {
                val game = indigoApp.rootService.currentGame!!
                val index = game.currentPlayerIndex
                val tile = players[index].handTile!!
                rootService.playerTurnService.rotateTileLeft(tile)
            }
        }

        for (i in 0 until count) {
            playerRightButtons[i].onMouseClicked = {
                val game = indigoApp.rootService.currentGame!!
                val index = game.currentPlayerIndex
                val tile = players[index].handTile!!
                rootService.playerTurnService.rotateTileRight(tile)
            }
        }

        for (i in 0 until count) {
            playerCheckButtons[i].onMouseClicked = {
                val game = indigoApp.rootService.currentGame!!
                val index = game.currentPlayerIndex
                val tile = players[index].handTile!!
                println(index)
                println(players[index].handTile!!.toString())
                println(tile.toString())
                callPlaceTile(tile)
            }
        }
    }

    /**
     * Initialize game board grid
     */
    private fun initializeGameBoardGrid() {
        // Populate the hexagonal grid with HexagonView instances
        for (row in -4..4) {
            for (col in -4..4) {
                val hexagon = HexagonView(size = 55.0, visual = ImageVisual("plaintile.png")).apply {
                    onMouseClicked = {
                        chooseTile(this, col, row)
                    }
                }

                /* hexagon.resize(width = 110, height = 110)
                 hexagon.scaleY(0.6)
                 hexagon.scaleX(0.6)*/
                hexagonGrid[col, row] = hexagon
            }
        }
        // Rotate the hexagonal grid by -30 degrees
        // hexagonGrid.rotate(-30)
        // Middle Tile
        hexagonGrid[0, 0]?.apply {
            visual = ImageVisual("middletile.png")
            isDisabled = true
        }
        // Gate Tiles
        //1
        hexagonGrid[0, -4]?.apply {
            visual = ImageVisual("gatetile1.png")
            isDisabled = true
        }
        //2
        hexagonGrid[4, -4]?.apply {
            visual = ImageVisual("gatetile2.png")
            isDisabled = true
        }
        //3
        hexagonGrid[4, 0]?.apply {
            visual = ImageVisual("gatetile3.png")
            isDisabled = true
        }
        //4
        hexagonGrid[0, 4]?.apply {
            visual = ImageVisual("gatetile4.png")
            isDisabled = true
        }
        //5
        hexagonGrid[-4, 4]?.apply {
            visual = ImageVisual("gatetile5.png")
            isDisabled = true
        }
        //6
        hexagonGrid[-4, 0]?.apply {
            visual = ImageVisual("gatetile6.png")
            isDisabled = true
        }


        //hides the unused hexagon components left and right
        for (i in 0..3) {
            for (j in -4..-4 + i) {
                //left board side
                hexagonGrid[-(i + 1), j]?.visual = Visual.EMPTY
                //right board side
                hexagonGrid[(i + 1), -j]?.visual = Visual.EMPTY
            }
        }
    }

    /**
     *  Initialize GateTokens
     */
    private fun initialzeGateTokens() {
        val guiGateTokens = listOf(
            gate2Token1,
            gate2Token2,
            gate3Token1,
            gate3Token2,
            gate4Token1,
            gate4Token2,
            gate5Token1,
            gate5Token2,
            gate6Token1,
            gate6Token2,
            gate1Token1,
            gate1Token2,
        )
        val currentGame = indigoApp.rootService.currentGame
        val entityGateTokens = currentGame!!.gameBoard.gateTokens

        for (i in entityGateTokens.indices) {
            guiGateTokens[i].visual = entityGateTokens[i].color.toImg()
        }
    }

    /**
     * Initializes the GUI after a new game was created. Creates views of players, their tiles and gates
     */
    override fun refreshAfterStartGame() {
        val game = rootService.currentGame
        checkNotNull(game) { "No started game found." }
        rotationDegree = 0
        val players = game.players
        setPlayers(players)
        initialzeGateTokens()
        invokeButtons(players)
        val startHandTile = mutableListOf<Tile?>()
        for (player in players) {
            startHandTile.add(player.handTile)
        }

        for (blueGemCounter in blueGems) {
            blueGemCounter.isVisible = false
        }
        println(startHandTile.toString())
        mapGems()
        fillMap()
        cleanGameScene()
    }

    /**
     * private function to reset the game scene components
     */
    private fun cleanGameScene() {
        for (i in 0..3) {
            playerScores[i].text = "0 points"
            playerGreenGemCounters[i].text = "0"
            playerYellowGemCounters[i].text = "0"
        }
        initializeGameBoardGrid()
        blueGem.reposition(916, 438)
        greenGem1.reposition(894, 410)
        greenGem2.reposition(933, 408)
        greenGem3.reposition(948, 442)
        greenGem4.reposition(914, 468)
        greenGem5.reposition(881, 443)
        yellowGem1.reposition(914, 70)
        yellowGem2.reposition(1230, 254)
        yellowGem3.reposition(1230, 619)
        yellowGem4.reposition(914, 801)
        yellowGem5.reposition(590, 619)
        yellowGem6.reposition(590, 254)
        blueGem.isVisible = true
        greenGem1.isVisible = true
        greenGem2.isVisible = true
        greenGem3.isVisible = true
        greenGem4.isVisible = true
        greenGem5.isVisible = true
        yellowGem1.isVisible = true
        yellowGem2.isVisible = true
        yellowGem3.isVisible = true
        yellowGem4.isVisible = true
        yellowGem5.isVisible = true
        yellowGem6.isVisible = true


    }

    /**
     * For every player set a view of name and token color and disable superfluous players views
     */
    private fun setPlayers(players: List<Player>) {
        val currentGame = indigoApp.rootService.currentGame
        checkNotNull(currentGame)

        val count = players.size

        for (i in count downTo 1 step 1) {
            playerlabels[i - 1].text = players[i - 1].name
            getGem(playerTokens[i - 1], players[i - 1].color)
        }
        for (i in count downTo 2 step 1) {
            playerlabels[i - 1].text = players[i - 1].name
            getGem(playerTokens[i - 1], players[i - 1].color)

            playerlabels[i - 1].apply {
                isVisible = true
            }
            playerScores[i - 1].apply {
                isVisible = true
            }
            playerTokens[i - 1].apply {
                isVisible = true
            }
            playerGreenGems[i - 1].apply {
                isVisible = true
            }
            playerGreenGemCounters[i - 1].apply {
                isVisible = true
            }
            playerYellowGems[i - 1].apply {
                isVisible = true
            }
            playerYellowGemCounters[i - 1].apply {
                isVisible = true
            }
            playerHandtiles[i - 1].apply {
                isVisible = true
            }
        }
        refreshAfterChangePlayer()
        for (a in count until 4) {
            when (a) {
                2 -> {
                    player3Label.apply { isVisible = false }
                    player3ScoreLabel.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3Token.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3greenGem.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3greenGemCounter.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3yellowGem.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3yellowGemCounter.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3turnHighlight.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3handTile.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3leftButton.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3rightButton.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player3checkButton.apply {
                        isDisabled = true
                        isVisible = false
                    }
                }

                3 -> {
                    player4Label.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4ScoreLabel.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4Token.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4greenGem.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4greenGemCounter.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4yellowGem.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4yellowGemCounter.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4turnHighlight.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4handTile.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4leftButton.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4rightButton.apply {
                        isDisabled = true
                        isVisible = false
                    }
                    player4checkButton.apply {
                        isDisabled = true
                        isVisible = false
                    }
                }
            }
        }

    }


    /**
     * Sets Token-Label with an Image of the given TokenColor
     * @param label The Label associated with the gem.
     * @param color The TokenColor representing the color of the gem to be retrieved.
     */
    private fun getGem(label: Label, color: TokenColor) {
        when (color) {
            TokenColor.WHITE -> label.apply { visual = ImageVisual("token_white.png") }

            TokenColor.PURPLE -> label.apply { visual = ImageVisual("token_purple.png") }

            TokenColor.BLUE -> label.apply { visual = ImageVisual("token_blue.png") }

            TokenColor.RED -> label.apply { visual = ImageVisual("token_red.png") }
        }
    }

    /**
     * Update the Gui after a players turn. For the current player show highlight and interaction buttons
     */
    override fun refreshAfterChangePlayer() {
        val currentGame = indigoApp.rootService.currentGame
        rotationDegree = 0
        checkNotNull(currentGame)
        invokeButtons(currentGame.players)
        val currentPlayerIndex = currentGame.currentPlayerIndex
        for (i in playerTurnHighlights.indices) {
            if (i == currentPlayerIndex) {
                playerTurnHighlights[i].isVisible = true
                playerRightButtons[i].isVisible = true
                playerRightButtons[i].isDisabled = false
                playerLeftButtons[i].isVisible = true
                playerLeftButtons[i].isDisabled = false
                playerCheckButtons[i].isVisible = true
                playerCheckButtons[i].isDisabled = false
            } else {
                playerTurnHighlights[i].isVisible = false
                playerRightButtons[i].isVisible = false
                playerRightButtons[i].isDisabled = true
                playerLeftButtons[i].isVisible = false
                playerLeftButtons[i].isDisabled = true
                playerCheckButtons[i].isVisible = false
                playerCheckButtons[i].isDisabled = true

            }
        }
        rotationDegree = 0
        refreshAfterMoveTile()
    }

    /**
     *  Update the GameScene with the new tile from the currentplayer
     */
    override fun refreshAfterDistributeNewTile() {
        val currentGame = indigoApp.rootService.currentGame
        checkNotNull(currentGame)
        val players = currentGame.players
        for (i in players.indices) {
            if (players[i].handTile == null) {
                playerHandtiles[i].isVisible = false
            } else {
                playerHandtiles[i].visual = players[i].handTile!!.type.toImg()
                playerHandtiles[i].isVisible = true
                for (tile in playerHandtiles) {
                    tile.rotationProperty.value = -30.0
                }

            }
        }
        if (currentGame.routeTiles.isEmpty()) {
            reserveStack.isVisible = false
        }
    }

    /**
     *  refreshes the GameScene after EndGame was called
     *
     *  @throws IllegalStateException if no game is running
     */
    override fun refreshAfterEndGame() {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game) { "No game found." }
        indigoApp.showMenuScene(indigoApp.endGameMenuScene)
    }

    /**
     *  refreshes the GameScene after PlayerTurn rotateTileLeft was called
     * @param currentPlayerIndex The index of the current player in the game.
     *  @throws IllegalStateException if no game is running
     */
    override fun refreshAfterLeftRotation(currentPlayerIndex: Int) {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game) { "No game found." }
        val handTileLabels =
            listOf(player1handTile, player2handTile, player3handTile, player4handTile)
        handTileLabels[currentPlayerIndex].rotate(-60)
        rotationDegree--
    }

    /**
     *  refreshes the GameScene after PlayerTurn rotateTileRight was called
     * @param currentPlayerIndex The index of the current player in the game.
     *  @throws IllegalStateException if no game is running
     */
    override fun refreshAfterRightRotation(currentPlayerIndex: Int) {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game) { "No game found." }
        val handTileLabels =
            listOf(player1handTile, player2handTile, player3handTile, player4handTile)
        handTileLabels[currentPlayerIndex].rotate(60)
        rotationDegree++
    }

    /**
     * Refreshes the scene after redoing a move or action.
     */
    override fun refreshAfterRedo() {
        refreshAfterChangePlayer()
        val currentGame = rootService.currentGame
        //val nextState = currentGame?.nextGameState
        currentGame?.gameBoard?.gameBoardTiles?.values
        getPlayersScores()
        refreshAfterDistributeNewTile()
        val currentTiles = indigoApp.rootService.currentGame!!.gameBoard.gameBoardTiles
        val nextTiles = indigoApp.rootService.currentGame!!.previousGameState!!.gameBoard.gameBoardTiles

        val differingTileEntry = currentTiles.entries.find { entry ->
            !nextTiles.containsKey(entry.key)
        }
        //val currentGems = indigoApp.rootService.currentGame?.gems
        //val nextStateGems = indigoApp.rootService.currentGame?.nextGameState?.gems
        //val nextToUpdatePosition = currentGems.entries

        if (differingTileEntry != null) {
            val differingCoordinate = differingTileEntry.key
            val differingTile = differingTileEntry.value
            val rotation = differingTile.edges.indexOf(Edge.ZERO)
            println("Found differing tile at coordinate $differingCoordinate with tile $differingTile")
            val tileCol = differingTileEntry.key.column
            val tileRow = differingTileEntry.key.row
            hexagonGrid[tileCol, tileRow] =
                HexagonView(size = 55, visual = differingTile.type.toImg()).apply {
                    rotate(-60)
                    rotate(60 * rotation)
                }
           // val neighbors = rootService.gameService.getNeighboringCoordinates(differingCoordinate)
            for ((exit,gem) in differingTile.gemEndPosition) {
                //rootService.gameService.moveGems(differingCoordinate, neighbors[i], i)

                refreshAfterMoveGems(

                    gem,
                    differingCoordinate,
                    exit
                )
            }
        } else {
            println("No differing tile found.")
        }
        refreshAfterDistributeNewTile()
        repositionGems()
        refreshAfterChangePlayer()
    }

    /**
     * Refreshes the scene after undoing a move or action.
     */

    override fun refreshAfterUndo() {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game) { "No game found." }
        getPlayersScores()
        refreshAfterChangePlayer()
        refreshAfterDistributeNewTile()
        val currentTiles = game.gameBoard.gameBoardTiles
        val previousTiles = game.nextGameState!!.gameBoard.gameBoardTiles
        val differingTileEntries = previousTiles.entries.filter { entry ->
            !currentTiles.containsKey(entry.key)
        }

        if (differingTileEntries.isNotEmpty()) {
            differingTileEntries.forEach { differingTileEntry ->
                val differingCoordinate = differingTileEntry.key
                val differingTile = differingTileEntry.value
                println("Found differing tile at coordinate $differingCoordinate with tile $differingTile")
                val tileCol = differingCoordinate.column
                val tileRow = differingCoordinate.row
                hexagonGrid[tileCol, tileRow]?.apply {
                    this.visual = ImageVisual("plaintile.png")
                    onMouseClicked = {
                        chooseTile(this, differingCoordinate.column, differingCoordinate.row)
                    }
                }
            }
        } else {
            println("No differing tiles found.")
        }

        refreshAfterDistributeNewTile()
        repositionGems()

    }

    /**
     * Repositions gems on the game board after a player undoes a turn
     */
    private fun repositionGems() {
        val gameBoardTiles = indigoApp.rootService.currentGame!!.gameBoard.gameBoardTiles
        val middle = indigoApp.rootService.currentGame!!.middleTile
        for ((coordinate, tile) in gameBoardTiles) {
            for ((gemPosition, gem) in tile.gemEndPosition) {
                val position = coordMap[coordinate]
                val posX = position!!.x + gemEndPos[gemPosition]!!.x
                val posY = position.y + gemEndPos[gemPosition]!!.y
                lock()
                if(gemMap[gem] != null) {
                    playAnimation(DelayAnimation(1000).apply {
                        gemMap[gem]!!.reposition(posX, posY)

                        gemMap[gem]!!.apply { isVisible = true }
                        unlock()
                    })
                }
            }

        }

        for ((int, gem) in middle.gemPosition) {
            if(gemMap[gem] != null){
            val label = gemMap[gem]!!
            val newX  = middleMap[int]!!.x
            val newY  = middleMap[int]!!.y
                label.apply { posX = newX; posY = newY}
            }
        }

    }


    /**
     * Refreshes the GUI after placing a tile at a specified coordinate.
     *
     * @param coordinate The coordinate where the tile is placed.
     * @param tile The tile to be placed.
     */
    override fun refreshAfterPlaceTile(coordinate: Coordinate, tile: Tile) {
        val col = coordinate.column
        val row = coordinate.row

        hexagonGrid[col, row] =
            HexagonView(size = 55.0, visual = tile.type.toImg()).apply {
                rotate(-60)
                rotate(60 * rotationDegree)
            }
        rotationDegree = 0
    }

    /**
     * Sets chosen space back after the player tried to place tile at the occupied space
     */
    override fun refreshAfterCheckPlacement() {
        refreshAfterMoveTile()
    }

    /**
     * Highlights the clicked tile and sets [chosenPlace] to the currently chosen space
     * Saves the coordinates of the chosen space in [chosenCol] and [chosenRow]
     * @param tile The HexagonView representing the chosen tile.
     * @param col The column index of the chosen tile on the game board.
     * @param row The row index of the chosen tile on the game board.
     */
    private fun chooseTile(tile: HexagonView, col: Int, row: Int) {
        if (chosenPlace != null) chosenPlace!!.visual.borderWidth = BorderWidth(0)

        tile.apply {
            visual.borderWidth = BorderWidth(5)
            visual.borderColor = BorderColor.RED
        }

        chosenPlace = tile
        chosenRow = row
        chosenCol = col


    }

    /**
     * Creates Coordinate Object of chosenRow and chosenCol. Asserts if no space was chosen yet.
     * tileToPlace saves the latest View of currentPlayers tile to place on the gameBoard
     * Calls placeRouteTile with the given tile and created Coordinate
     * @param tile The Tile to be placed on the game board.
     */
    private fun callPlaceTile(tile: Tile) {
        checkNotNull(chosenPlace) { "Please, choose space on the board and press ✓" }

        val coordinates = Coordinate(chosenRow!!, chosenCol!!)
        rootService.playerTurnService.placeRouteTile(coordinates, tile)
    }

    /**
     * Sets chosen space back after the player tried to or placed his/her tile
     */
    private fun refreshAfterMoveTile() {
        if (chosenPlace != null) {
            chosenPlace!!.visual.borderWidth = BorderWidth(0)
            chosenPlace = null
            chosenRow = null
            chosenCol = null

        }

    }

    /**
     * Moves the view of a gem to its new position on the scene
     * @param gem The Gem that has been moved.
     * @param coordinate The target Coordinate where the gem is moved to.
     * @param exit The exit index indicating the direction of the movement.
     */
    override fun refreshAfterMoveGems(gem: Gem, coordinate: Coordinate, exit: Int) {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game) { "No game found." }
        val position = coordMap[coordinate]
        val posX = position!!.x + gemEndPos[exit]!!.x
        val posY = position.y + gemEndPos[exit]!!.y
        lock()
        playAnimation(DelayAnimation(1000).apply {
            gemMap[gem]!!.reposition(posX, posY)
            unlock()
        })

    }
    /**
     * Refreshes the scene after a collision between two gems.
     *
     * @param gem1 The first Gem involved in the collision.
     * @param gem2 The second Gem involved in the collision.
     */
    override fun refreshAfterCollision(gem1: Gem, gem2: Gem) {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game) { "No game found." }
        val gemLabel1 = gemMap[gem1]
        gemLabel1?.apply {
            isVisible = false
        }
        val gemLabel2 = gemMap[gem2]
        gemLabel2?.apply {
            isVisible = false
        }
        val gameBoard = rootService.currentGame?.gameBoard

        val gemsOnBoard = mutableSetOf<Gem>()
        for ((_, tile) in gameBoard?.gameBoardTiles ?: emptyMap()) {
            if (tile.gemEndPosition.isNotEmpty()) {
                gemsOnBoard.addAll(tile.gemEndPosition.values)
            }
        }

    }
    /**
     * Refreshes the scene after removing a gem from the game board.
     *
     * @param gem The Gem that has been removed.
     */
    override fun refreshAfterRemoveGems(gem: Gem) {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game) { "No game found." }

        getPlayersScores()

        val gemLabel = gemMap[gem]
        gemLabel?.apply {
            isVisible = false
        }
        val gameBoard = rootService.currentGame?.gameBoard

        val gemsOnBoard = mutableSetOf<Gem>()
        for ((_, tile) in gameBoard?.gameBoardTiles ?: emptyMap()) {
            if (tile.gemEndPosition.isNotEmpty()) {
                gemsOnBoard.addAll(tile.gemEndPosition.values)
            }
        }
    }


    /**
     * Refreshes the GUI after the network player's turn based on the current connection state.
     * Updates the visibility and disabled state of rotation buttons for each player.
     */
    override fun refreshAfterNetworkPlayerTurn() {
        val connectionState = indigoApp.rootService.networkService.connectionState
        val playerRotateRights = listOf(player1rightButton, player2rightButton, player3rightButton, player4rightButton)
        val playerRotateLefts = listOf(player1leftButton, player2leftButton, player3leftButton, player4leftButton)
        val playerRotateCheck = listOf(player1checkButton, player2checkButton, player3checkButton, player4checkButton)
        val currentGame = indigoApp.rootService.currentGame
        checkNotNull(currentGame)
        val currentPlayerIndex = currentGame.currentPlayerIndex
        invokeButtons(currentGame.players)
        if (connectionState == ConnectionState.WAITING_FOR_OPPONENTS_TURN) {
            playerRotateRights[currentPlayerIndex].isVisible = false
            playerRotateRights[currentPlayerIndex].isDisabled = true
            playerRotateLefts[currentPlayerIndex].isVisible = false
            playerRotateLefts[currentPlayerIndex].isDisabled = true
            playerRotateCheck[currentPlayerIndex].isVisible = false
            playerRotateCheck[currentPlayerIndex].isDisabled = true
        }
    }
    /**
     * Refreshes the scene after receiving a new tile with a specified rotation.
     *
     * @param rotation The rotation value of the received tile.
     */
    override fun refreshAfterReceivedTile(rotation: Int) {
        rotationDegree = rotation
        /* val currentPlayer = rootService.currentGame!!.currentPlayerIndex
         when (currentPlayer) {
             0 -> {
                 tileToPlace = HexagonView(visual = player1handTile.visual)
                     .apply {
                         resize(width = 110, height = 110)
                         scaleY(0.6)
                         scaleX(0.6)
                     }
             }

             1 -> {
                 tileToPlace = HexagonView(visual = player2handTile.visual)
                     .apply {
                         resize(width = 110, height = 110)
                         scaleY(0.6)
                         scaleX(0.6)
                     }
             }

             2 -> {
                 tileToPlace = HexagonView(visual = player3handTile.visual)
                     .apply {
                         resize(width = 110, height = 110)
                         scaleY(0.6)
                         scaleX(0.6)
                     }
             }

             3 -> {
                 tileToPlace = HexagonView(visual = player4handTile.visual)
                     .apply {
                         resize(width = 110, height = 110)
                         scaleY(0.6)
                         scaleX(0.6)
                     }
             }
         }

         */
    }
    /**
     * Maps the gems on the game board.
     * This function may handle tasks specific to organizing or updating the positions of gems on the game board.
     */
    private fun mapGems() {
        val game = rootService.currentGame
        checkNotNull(game)
        val gem0 = game.middleTile.gemPosition[0]
        val gem1 = game.middleTile.gemPosition[1]
        val gem2 = game.middleTile.gemPosition[2]
        val gem3 = game.middleTile.gemPosition[3]
        val gem4 = game.middleTile.gemPosition[4]
        val gem5 = game.middleTile.gemPosition[5]
        val yellowgem1 = game.treasureTiles[0].gemEndPosition[3]
        val yellowgem2 = game.treasureTiles[1].gemEndPosition[4]
        val yellowgem3 = game.treasureTiles[2].gemEndPosition[5]
        val yellowgem4 = game.treasureTiles[3].gemEndPosition[0]
        val yellowgem5 = game.treasureTiles[4].gemEndPosition[1]
        val yellowgem6 = game.treasureTiles[5].gemEndPosition[2]


        gemMap[gem0!!] = blueGem
        gemMap[gem1!!] = greenGem1
        gemMap[gem2!!] = greenGem2
        gemMap[gem3!!] = greenGem3
        gemMap[gem4!!] = greenGem4
        gemMap[gem5!!] = greenGem5
        gemMap[yellowgem1!!] = yellowGem1
        gemMap[yellowgem2!!] = yellowGem2
        gemMap[yellowgem3!!] = yellowGem3
        gemMap[yellowgem4!!] = yellowGem4
        gemMap[yellowgem5!!] = yellowGem5
        gemMap[yellowgem6!!] = yellowGem6
    }

    /**
     * fills the [coordMap] with keys (Coordinate) and values (Position)
     */
    private fun fillMap() {
        //Aufpassen : bei Grid[col,row] und Coord(row,col)
        val y1 = 257.0 //y9
        val y2 = 211.0 //y8
        val y3 = 163.0 //y7
        val y4 = 114.0 //y6
        val y5 = 68.0


        //Reihe 1
        for (i in 0..4) {
            coordMap[Coordinate(i, -4)] = Position(592.0, y1 + i * 95)
        }

        //Reihe 9
        for (i in -4..0) {
            val count = i + 4
            coordMap[Coordinate(i, 4)] = Position(1248.0, y1 + count * 95)

        }

        //Reihe 2
        for (i in -1..4) {
            val count = i + 1
            coordMap[Coordinate(i, -3)] = Position(674.0, y2 + count * 95)
        }

        //Reihe 8
        for (i in -4..1) {
            val count = i + 4
            coordMap[Coordinate(i, 3)] = Position(1166.0, y2 + count * 95)
        }

        //Reihe 3
        for (i in -2..4) {
            val count = i + 2
            coordMap[Coordinate(i, -2)] = Position(756.0, y3 + count * 95)
        }

        //Reihe 7
        for (i in -4..2) {
            val count = i + 4
            coordMap[Coordinate(i, 2)] = Position(1084.0, y3 + count * 95)
        }

        //Reihe 4
        for (i in -3..4) {
            val count = i + 3
            coordMap[Coordinate(i, -1)] = Position(838.0, y4 + count * 95)
        }

        //Reihe 6
        for (i in -4..3) {
            val count = i + 4
            coordMap[Coordinate(i, 1)] = Position(1002.0, y4 + count * 95)
        }

        //Reihe 5
        for (i in -4..4) {
            val count = i + 4
            coordMap[Coordinate(i, 0)] = Position(920.0, y5 + count * 95)
        }
    }

    /**
     * Updates GUI and sets gameboard after a saved game was reloaded
     */
    override fun refreshAfterLoadGame() {
        var game = indigoApp.rootService.currentGame
        checkNotNull(game)
        refreshAfterStartGame()
        mapGems()
        repositionGems()
        while(game?.nextGameState != null){
            indigoApp.rootService.currentGame = indigoApp.rootService.currentGame?.nextGameState
            refreshAfterRedo()
            game = indigoApp.rootService.currentGame
        }
    }

    /**
     * Actualizes the gemCounters and Scores of the players of a gameState
     *
     */
    private fun getPlayersScores () {
        val game = indigoApp.rootService.currentGame
        checkNotNull(game)
        val count = game.players.size
        for (i in count downTo 1 step 1) {
            val yellowGems = game.players[i - 1].collectedGems.count {
                it.gemColor == GemColor.AMBER
            }
            val greenGems = game.players[i - 1].collectedGems.count {
                it.gemColor == GemColor.EMERALD
            }

            val blueCounter = game.players[i - 1].collectedGems.count {
                it.gemColor == GemColor.SAPPHIRE
            }

            playerScores[i - 1].text = game.players[i - 1].score.toString() + " points"
            playerYellowGemCounters[i - 1].text = yellowGems.toString()
            playerGreenGemCounters[i - 1].text = greenGems.toString()

            blueGems[i - 1].isVisible = blueCounter > 0
        }
    }
}
