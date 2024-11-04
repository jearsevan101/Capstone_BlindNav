package com.example.blindnavjpc.dataconnection
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MarkerViewModel: ViewModel() {
    private val _markerId = MutableLiveData<Int>()
    val markerId: LiveData<Int> = _markerId

    private val _angle = MutableLiveData<Float>()
    val angle: LiveData<Float> = _angle

    fun updateMarkerId(id: Int) {
        _markerId.value = id
    }

    fun updateAngle(newAngle: Float) {
        _angle.value = newAngle
    }
}