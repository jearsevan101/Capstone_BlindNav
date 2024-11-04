package com.example.blindnavjpc.dataconnection

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("aruco-markers/{id}")
    suspend fun getArucoMarker(@Path("id") id: Int): Response<ApiResponse>

    @GET("aruco-markers")
    suspend fun getAllArucoMarkers(): Response<List<ApiResponse>>

    @GET("graph-connections")
    suspend fun getGraphConnections(): Response<List<GraphConnection>>

    @GET("rooms")
    suspend fun getRooms(): Response<List<Rooms>>
}