package com.example.blindnavjpc.dataconnection

data class GraphConnection(
    val connection_id: Int,
    val from_marker_id: Int,
    val to_marker_id: Int,
    val distance: Float,
    val angle: Float
)

data class GraphConnectionResponse(
    val data: List<GraphConnection>
)