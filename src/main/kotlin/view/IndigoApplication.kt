package view


import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import java.io.FileNotFoundException
import entity.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Implementation of the BGW [BoardGameApplication] for the example game "Indigo"
 */

class IndigoApplication : BoardGameApplication("Indigo Game"), Refreshable {

    // Central service from which all others are created/accessed
    // also holds the currently active game
    var aiGame = false //ob es KI Spieler gibt
    var players: MutableList<Player?> = mutableListOf(null,null,null,null) // list of players for the Game
    var networkMode : Boolean=false //sagt, ob wir Hot seat oder Network spielen (wird in ModusMenuScene gesetzt)
    var isRandom = false //if the participant are all random
    var notSharedGates = true // if you play on notSharedGates and SharedGAtes mode
    var availableColors = mutableListOf(TokenColor.BLUE,TokenColor.RED,TokenColor.WHITE,TokenColor.PURPLE)
    val rootService = RootService()

    val startScene = NewGameMenuScene(this)
    val modusScene = ModusMenuScene(this)
    val configurePlayersScene = ConfigurePlayersGameScene(this)

    val gatesScene = GateMenuScene(this)
    val networkScene = NetworkMenuScene(this)
    val configurePlayerXScene = ConfigurePlayerXScene(this)
    val joinGameScene = JoinGameScene(this)

    val endGameMenuScene = EndGameMenuScene(this)
    val gameScene = GameScene(this)
    val aiMenuScene = AIMenuScene(this)
    val gameSavedScene = GameSavedMenuScene(this)
    val saveGameScene = SaveGameMenuScene(this)
    val hostGameScene = HostGameScene(this)
    val savedGamesScene = SavedGamesMenuScene(this)
    val networkConfigureScene = ConfigureNetworkPlayersScene(this/*, listOf("one", "two", "three")*/)
    val newPlayerScene = NewPlayerScene (this)


    init {
        //das ladet unser IrishGrover Font
        rootService.addRefreshables(
            this,
            startScene,
            modusScene,
            configurePlayersScene,
            gatesScene,
            configurePlayerXScene,
            joinGameScene,
            networkScene,
            endGameMenuScene ,
            //aiMenuScene,
            gameSavedScene,
            saveGameScene,
            hostGameScene,
            savedGamesScene,
            networkConfigureScene,
            gameScene,
            newPlayerScene
        )

        val resource = IndigoApplication::class.java.getResourceAsStream("/IrishGrover.ttf")
            ?: throw FileNotFoundException()
        val fontFile = File.createTempFile("font", ".tmp")
        Files.copy(resource, fontFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        loadFont(fontFile)

        this.showMenuScene(startScene)

        //Testen der GameScene
        //this.showGameScene(gameScene)
    }


    //In jeder Szene : private val gradient ="-fx-text-fill: linear-gradient(to bottom, #061598, #06987E);"
    // a jedem Component mit Text : font = Font(family: "Irish Grover")
    // .apply { componentStyle = gradient }


}
