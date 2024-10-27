package com.example.blindnavjpc.dataconnection

class Graph {
    private val adjacencyList = mutableMapOf<Int, MutableList<Edge>>()

    data class Edge(
        val toNode: Int,
        val distance: Float,
        val angle: Float
    )

    fun addEdge(fromNode: Int, toNode: Int, distance: Float, angle: Float) {
        // Add forward edge
        adjacencyList.getOrPut(fromNode) { mutableListOf() }
            .add(Edge(toNode, distance, angle))

        // Add reverse edge with adjusted angle
        val reverseAngle = (angle + 180) % 360
        adjacencyList.getOrPut(toNode) { mutableListOf() }
            .add(Edge(fromNode, distance, reverseAngle))
    }

    fun findShortestPath(start: Int, end: Int): List<PathNode>? {
        val distances = mutableMapOf<Int, Float>()
        val previousNodes = mutableMapOf<Int, Int>()
        val previousAngles = mutableMapOf<Int, Float>()
        val unvisited = mutableSetOf<Int>()

        // Initialize distances
        adjacencyList.keys.forEach { node ->
            distances[node] = Float.POSITIVE_INFINITY
            unvisited.add(node)
        }
        distances[start] = 0f

        while (unvisited.isNotEmpty()) {
            val current = unvisited.minByOrNull { distances[it] ?: Float.POSITIVE_INFINITY }
                ?: break

            if (current == end) break

            unvisited.remove(current)

            adjacencyList[current]?.forEach { edge ->
                if (unvisited.contains(edge.toNode)) {
                    val newDistance = (distances[current] ?: 0f) + edge.distance
                    if (newDistance < (distances[edge.toNode] ?: Float.POSITIVE_INFINITY)) {
                        distances[edge.toNode] = newDistance
                        previousNodes[edge.toNode] = current
                        previousAngles[edge.toNode] = edge.angle
                    }
                }
            }
        }

        // Reconstruct path
        if (previousNodes[end] == null) return null

        val path = mutableListOf<PathNode>()
        var current = end
        while (current != start) {
            val previous = previousNodes[current] ?: break
            path.add(0, PathNode(current, previousAngles[current] ?: 0f))
            current = previous
        }
        path.add(0, PathNode(start, 0f))

        return path
    }

    data class PathNode(val node: Int, val angle: Float)
}