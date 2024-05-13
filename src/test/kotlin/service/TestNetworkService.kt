package service

import service.network.ConnectionState
import service.network.NetworkService
//import tools.aqua.bgw.observable.lists.ObservableArrayList
import tools.aqua.bgw.observable.properties.Property

/**
 *  The [TestNetworkService] creates is a [NetworkService]
 *  for test proposes
 *
 * @param rootService The root service providing access to the core functionality of the application.
 */
class TestNetworkService(rootService: RootService) : NetworkService(rootService) {
    // Observable list to store opponent names
    //var opponentsProperty: ObservableArrayList<String> = ObservableArrayList()

    // Property to observe the connection state
    var connectionStateProperty: Property<ConnectionState> = Property(this.connectionState)

    // Constant for the server address
    companion object {
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"
    }

    /**
     * Connects to the network with the specified secret and player name.
     *
     * @param secret The secret token for secure communication.
     * @param name The name of the player initiating the connection.
     * @return `true` if the connection is successful, `false` otherwise.
     */
    override fun connect(secret: String, name: String): Boolean {
        // Create a TestNetworkClient for the connection
        this.client = TestNetworkClient(
            name,
            SERVER_ADDRESS,
            secret,
            networkService = this
        )
        // Check if the client is not already connected
        check(this.client?.isOpen == false) {
            "client is already connected"
        }
        // Retrieve the client instance
        val client = this.client ?: return false
        // Attempt to connect the client
        val isConnected = client.connect()
        // Update the connection state based on the result
        if (isConnected) {
            updateConnectionState(ConnectionState.CONNECTED)
        } else {
            updateConnectionState(ConnectionState.DISCONNECTED)
        }
        // Return the connection status
        return isConnected
    }

}