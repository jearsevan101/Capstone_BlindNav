package com.example.blindnavjpc.dataconnection
import com.example.blindnavjpc.helpers.TTSManager
import kotlin.math.abs

data class NavigationStep(
    val currentMarkerId: Int,
    val nextMarkerId: Int,
    val angle: Float,
    val currentMarkerDescription: String,
    val direction: String,
    val distance: Float
)

class NavigationManager {
    private var route: List<Graph.PathNode> = emptyList()
    private var currentStepIndex = 0
    private var currentDistance: Float = 0f
    private var currentAngle: Float = 0f

    fun setRoute(newRoute: List<Graph.PathNode>) {
        route = newRoute
        currentStepIndex = 0
        currentDistance = 0f
        currentAngle = 0f
    }

    fun getCurrentStep(): Graph.PathNode? {
        return route.getOrNull(currentStepIndex)
    }

    fun getNextStep(): Graph.PathNode? {
        return route.getOrNull(currentStepIndex + 1)
    }

    fun moveToNextStep() {
        if (currentStepIndex < route.size - 1) {
            currentStepIndex++
            currentDistance = 0f
            currentAngle = 0f
        }
    }

    fun updateCurrentPosition(distance: Float, angle: Float) {
        currentDistance = distance
        currentAngle = angle
    }

    fun getRemainingDistance(): Float {
        val nextStep = getNextStep() ?: return 0f
        return currentDistance
    }

    fun getRequiredTurn(targetAngle: Float): String {
//        val angleDiff = (targetAngle - currentAngle + 360) % 360
        val angleDiff = (targetAngle - currentAngle)
//        TTSManager.speak("Target sudut $targetAngle   sudut saat ini $currentAngle     perbandingan sudut  $angleDiff ")

        return when {
            -180 <= angleDiff && angleDiff < -135  -> "Putar arah ke Kiri"
            -135 <= angleDiff && angleDiff < -45  -> "Belok Kiri sejauh ${abs(angleDiff.toInt())} derajat"
            -45 <= angleDiff && angleDiff < 45  -> "Maju Terus Ke depan"
            45 <= angleDiff && angleDiff < 135   -> "Belok Kanan sejauh ${abs(angleDiff.toInt())} derajat"
//            -180 <= angleDiff && angleDiff < -135  -> "Putar arah ke Kiri"
//            -135 <= angleDiff &&angleDiff < -45  -> "Belok Kiri, arah saat ini $currentAngle derajat dengan target angle $targetAngle perbedaan sudutnya $angleDiff"
//            -45 <= angleDiff &&angleDiff < 45  -> "Maju Terus Ke depan, arah saat ini $currentAngle derajat dengan target angle $targetAngle perbedaan sudutnya $angleDiff"
//            45 <= angleDiff &&angleDiff < 135   -> "Belok Kanan, arah saat ini $currentAngle derajat dengan target angle $targetAngle perbedaan sudutnya $angleDiff"
            else   -> "Putar Arah ke kanan "
//            angleDiff <= 10 || angleDiff >= 350 -> "Maju Terus Kedepan"
//            angleDiff < 180 -> "Belok kanan ${angleDiff.toInt()} derajat"
//            else -> "Belok kiri ${(360 - angleDiff).toInt()} derajat"
        }
    }

    fun isCloseToMarker(): Boolean {
        return currentDistance <= 20 // 20cm threshold
    }

    fun getNavigationInstruction(targetAngle: Float): String {
        if (currentDistance == 0f) {
            return "Tolong perbaharui jarak dan sudut"
        }

        val turnInstruction = getRequiredTurn(targetAngle)
        return when {
            isCloseToMarker() -> "Anda dekat dengan marker. Mencari marker lainnya."
            else -> "Maju sejauh ${currentDistance.toInt()-75} centimeter kemudian $turnInstruction\n"
        }
    }

    fun isNavigationComplete(): Boolean {
        return currentStepIndex >= route.size - 1
    }

    fun getDirection(targetAngle: Float): String {
        // Normalize angles to be between 0 and 360
        val normalizedCurrentAngle = normalizeAngle(currentAngle)
        val normalizedTargetAngle = normalizeAngle(targetAngle)

        // If we don't have current angle information, use cardinal directions
        if (currentAngle == 0f) {
            return getCardinalDirection(normalizedTargetAngle)
        }

        // Calculate the shortest angular difference between current and target angles
        val angleDiff = calculateAngleDifference(normalizedCurrentAngle, normalizedTargetAngle)

        return when {
            angleDiff <= 10 -> "Ke Depan"
            angleDiff <= 45 -> "Sedikit Ke kanan"
            angleDiff <= 135 -> "Kanan"
            angleDiff <= 180 -> "Putar Balik"
            angleDiff <= 225 -> "Putar Balik Kiri"
            angleDiff <= 315 -> "Kiri"
            else -> "Sedikit ke kiri"
        }
    }

    private fun normalizeAngle(angle: Float): Float {
        var normalized = angle % 360
        if (normalized < 0) normalized += 360
        return normalized
    }

    private fun calculateAngleDifference(angle1: Float, angle2: Float): Float {
        val diff = abs((angle2 - angle1 + 180) % 360 - 180)
        return if (angle1 > angle2) -diff else diff
//        return diff
    }

    private fun getCardinalDirection(angle: Float): String {
        val normalizedAngle = normalizeAngle(angle)
        return when {
            normalizedAngle >= 337.5f || normalizedAngle < 22.5f -> "Ke depan"
            normalizedAngle < 67.5f -> "Ke depan kanan"
            normalizedAngle < 112.5f -> "Kanan"
            normalizedAngle < 157.5f -> "Belakang kanan"
            normalizedAngle < 202.5f -> "Belakang"
            normalizedAngle < 247.5f -> "Belakang kiri"
            normalizedAngle < 292.5f -> "Kiri"
            normalizedAngle < 337.5f -> "Ke depan kiri"
            else -> "Ke depan" // This case should never occur due to initial check
        }
    }
}