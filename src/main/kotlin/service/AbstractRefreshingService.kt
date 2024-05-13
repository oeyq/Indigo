package service

import view.Refreshable

/**
 * Abstract service class that handles multiples [Refreshable]s (usually UI elements, such as
 * specialized GameScene classes/instances) which are notified
 * of changes to refresh via the [onAllRefreshables] method.
 */
abstract class AbstractRefreshingService {

    private val refreshables = mutableListOf<Refreshable>()

    /**
     * Adds a new Refreshable instance to the list of refreshable components.
     *
     * @param newRefreshable The Refreshable instance to be added.
     */
    fun addRefreshable(newRefreshable : Refreshable) {
        refreshables += newRefreshable
    }

    /**
     * Executes the passed method (usually a lambda) on all
     * [Refreshable]s registered with the service class that
     * extends this [AbstractRefreshingService]
     *
     * Example usage (from any method within the service):
     * ```
     * onAllRefreshables {
     * }

     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) =
        refreshables.forEach { it.method() }

}