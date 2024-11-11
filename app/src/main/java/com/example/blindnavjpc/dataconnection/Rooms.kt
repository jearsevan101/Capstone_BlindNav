package com.example.blindnavjpc.dataconnection

data class Rooms(
    val room_id: Int,
    val building_id: Int,
    val floor: Int,
    val room_name: String,
    val room_type: String,
    val room_number: String
//    val responsible_professors: null
)
