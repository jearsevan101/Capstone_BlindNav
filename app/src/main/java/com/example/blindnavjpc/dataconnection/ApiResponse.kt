package com.example.blindnavjpc.dataconnection

data class ApiResponse(
    val marker_id: Int,
    val building_id: Int,
    val floor: Int,
    val point_name: String,
    val point_type: String,
    val west_room_id: Int?,
    val east_room_id: Int?,
    val north_room_id: Int?,
    val south_room_id: Int?,
    val description: String?
)
