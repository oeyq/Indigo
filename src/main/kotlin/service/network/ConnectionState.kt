package service.network

/**
 *  The enum class [ConnectionState] represented our state for the state machine
 *  to in which connection the network is
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTED,
    HOST_WAITING_FOR_CONFIRMATION,
    GUEST_WAITING_FOR_CONFIRMATION,
    WAITING_FOR_GUEST,
    WAITING_FOR_INIT,
    PLAYING_MY_TURN,
    WAITING_FOR_OPPONENTS_TURN
}