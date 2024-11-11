package com.example.blindnavjpc.dataconnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NavigationState(
    val currentLocation: String = "",
    val nextMarker: String = "",
    val direction: String = "",
    val distance: String = "",
    val isNavigating: Boolean = false,
    val error: String? = null
)

data class NavigationUpdate(
    val currentMarkerId: Int,
    val distance: Float,
    val angle: Float
)
class NavigationService (
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val graph = Graph()
    private val navigationManager = NavigationManager()

    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState

    private var fromId: Int? = null
    private var toId: Int? = null
    private var currentArucoId: Int = 0
    private var currentAngle: Float = 0f

    init {
        loadGraphData()
    }
    private fun loadGraphData() {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.getGraphConnections()
                if (response.isSuccessful) {
                    response.body()?.forEach { connection ->
                        graph.addEdge(
                            connection.from_marker_id,
                            connection.to_marker_id,
                            connection.distance,
                            connection.angle
                        )
                    }
                    updateState { it.copy(error = null) }
                } else {
                    updateState { it.copy(error = "Error loading graph data: ${response.code()}") }
                }
            } catch (e: Exception) {
                updateState { it.copy(error = "Error loading graph data: ${e.message}") }
            }
        }
    }

    suspend fun startNavigation(fromMarkerId: Int, toMarkerId: Int): Boolean {
        fromId = fromMarkerId
        if (toId == null) {  // Only assign if toId is not already set
            toId = toMarkerId
        }

        try {
            loadGraphData()
            val path = graph.findShortestPath(fromMarkerId, toMarkerId)
            if (path == null) {
                updateState { it.copy(error = "No path found between markers") }
                return false
            }

            if (path != null) {
                navigationManager.setRoute(path)
            }
//            updateNavigationDisplay()
            return true
        } catch (e: Exception) {
            updateState { it.copy(error = "Error: ${e.message}") }
            return false
        }
    }

    suspend fun updateLocation(currentMarkerId: Int) {
        val currentStep = navigationManager.getCurrentStep()
        val nextStep = navigationManager.getNextStep() ?: return

        if (nextStep == null) {
            updateState { it.copy(
                isNavigating = false,
                direction = "Navigasi Selesai!"
            )}
            return
        }
        if (nextStep.node == toId){
            updateState { it.copy(
                isNavigating = false,
                direction = "Navigasi Selesai!"
            )}
            return
        }

        if (currentMarkerId == nextStep.node) {
            navigationManager.moveToNextStep()
//            updateNavigationDisplay()
        }
        else {
            // Recalculate route from current position
//            toId?.let { startNavigation(currentMarkerId, it) }
            if (currentStep != null) {
                if (currentMarkerId != currentStep.node){
                    startNavigation(currentMarkerId, toId ?: return)

                }
            }
        }
    }

    suspend fun updatePositionInfo(update: NavigationUpdate) {
        val nextStep = navigationManager.getNextStep() ?: return
        navigationManager.updateCurrentPosition(update.distance, update.angle)

        val currentStep = navigationManager.getCurrentStep() ?: return
        currentArucoId = update.currentMarkerId
        currentAngle = update.angle
        val currentDescription = fetchMarkerDescription(currentStep.node)
        val remainingDistance = navigationManager.getRemainingDistance()
        val direction = navigationManager.getDirection(nextStep.angle)
//        val navigationInstruction = navigationManager.getNavigationInstruction(nextStep.angle)
        val navigationInstruction = if (nextStep.node == toId) navigationManager.getNavigationInstruction(graph.getAngleToNextMarker(currentStep.node, nextStep.node ))+   " selanjutnya anda tiba di depan  ruangan yang dituju " else navigationManager.getNavigationInstruction(graph.getAngleToNextMarker(currentStep.node, nextStep.node ))

        updateState { it.copy(
            currentLocation = "Lokasi saat ini: Marker ${currentStep.node}\n$currentDescription",
            nextMarker = "Marker selanjutnya: ${nextStep.node}",
            direction = "Arah: $direction",
            distance = "$navigationInstruction"
        )}

        if (navigationManager.isCloseToMarker()) {
            updateState { it.copy(
                direction = "Anda berada dekat dengan marker. Tolong scan marker selanjutnya"
            )}
        }
    }

//    private suspend fun updateNavigationDisplay() {
//        val currentStep = navigationManager.getCurrentStep() ?: return
//        val nextStep = navigationManager.getNextStep()
//
//        if (nextStep == null) {
//            updateState { it.copy(
//                isNavigating = false,
//                direction = "Kamu telah sampai di lokasi tujuanmu!"
//            )}
//            return
//        }
//
//        val currentDescription = fetchMarkerDescription(currentStep.node)
//
//        updateState { it.copy(
//            isNavigating = true,
//            currentLocation = "Lokasi saat ini: Marker ${currentStep.node}\n$currentDescription",
//            nextMarker = "Marker selanjutnya: ${nextStep.node} marker aruco saat ini ${currentArucoId}",
//            direction = "sudut tujuan: ${nextStep.angle} sudut saat ini ${currentAngle}",
//            distance = "Current step: ${currentStep} next step ${nextStep} "
//        )}
//    }

    private suspend fun fetchMarkerDescription(markerId: Int): String {
        return try {
            val response = RetrofitClient.apiService.getArucoMarker(markerId)
            if (response.isSuccessful) {
                response.body()?.description ?: "Tidak ada deskripsi"
            } else {
                "Error fetching deskripsi marker"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun updateState(update: (NavigationState) -> NavigationState) {
        _navigationState.value = update(_navigationState.value)
    }

    fun isNavigating(): Boolean = navigationState.value.isNavigating
}