package com.example.blindnavjpc.dataconnection
import android.content.Context
import android.content.ContextWrapper
import com.example.blindnavjpc.CameraActivity


class ContextExtensions {
    fun Context.findViewTreeCameraActivity(): CameraActivity? {
        var currentContext: Context? = this
        while (currentContext != null) {
            if (currentContext is CameraActivity) {
                return currentContext
            }
            currentContext = (currentContext as? ContextWrapper)?.baseContext
        }
        return null
    }
}