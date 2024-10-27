package com.example.blindnavjpc.dataconnection

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
        val angleDiff = (targetAngle - currentAngle + 360) % 360
        return when {
            angleDiff <= 10 || angleDiff >= 350 -> "Keep going straight"
            angleDiff < 180 -> "Turn right ${angleDiff.toInt()} degrees"
            else -> "Turn left ${(360 - angleDiff).toInt()} degrees"
        }
    }

    fun isCloseToMarker(): Boolean {
        return currentDistance <= 50 // 50cm threshold
    }

    fun getNavigationInstruction(targetAngle: Float): String {
        if (currentDistance == 0f) {
            return "Please update current distance and angle"
        }

        val turnInstruction = getRequiredTurn(targetAngle)
        return when {
            isCloseToMarker() -> "You are close to the marker. Look for the next marker."
            else -> "$turnInstruction\nContinue for ${currentDistance.toInt()} cm"
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
            angleDiff <= 10 -> "Forward"
            angleDiff <= 45 -> "Slight Right"
            angleDiff <= 135 -> "Right"
            angleDiff <= 180 -> "Turn Around"
            angleDiff <= 225 -> "Turn Around Left"
            angleDiff <= 315 -> "Left"
            else -> "Slight Left"
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
    }

    private fun getCardinalDirection(angle: Float): String {
        val normalizedAngle = normalizeAngle(angle)
        return when {
            normalizedAngle >= 337.5f || normalizedAngle < 22.5f -> "Forward"
            normalizedAngle < 67.5f -> "Forward Right"
            normalizedAngle < 112.5f -> "Right"
            normalizedAngle < 157.5f -> "Backward Right"
            normalizedAngle < 202.5f -> "Backward"
            normalizedAngle < 247.5f -> "Backward Left"
            normalizedAngle < 292.5f -> "Left"
            normalizedAngle < 337.5f -> "Forward Left"
            else -> "Forward" // This case should never occur due to initial check
        }
    }
}