package com.example.blindnavjpc

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Dictionary
import org.opencv.objdetect.Objdetect
import org.opencv.objdetect.DetectorParameters
import kotlin.math.atan2
import org.opencv.calib3d.Calib3d
import java.util.ArrayList



class CameraActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private var PERMISSION_CAMERA = Manifest.permission.CAMERA
    private var REQUEST_CODE = 101
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var mIntermediateMat: Mat
    private lateinit var arucoDetector: ArucoDetector
    private lateinit var dictionary: Dictionary
    private lateinit var detectorParams: DetectorParameters

    private var onPositionUpdate: ((Float, Float) -> Unit)? = null
    private var onIdUpdate:((Int)->Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.open_camera)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)

        cameraBridgeViewBase = findViewById(R.id.cameraview)
        cameraBridgeViewBase.setCvCameraViewListener(this)
        cameraBridgeViewBase.setCameraPermissionGranted()
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY)
        cameraBridgeViewBase.visibility = SurfaceView.VISIBLE

        if (OpenCVLoader.initDebug()) {
            Log.d("OPENCV:APP", "Successful load of OpenCV")
            cameraBridgeViewBase.enableView()
            Toast.makeText(this, "OpenCV Loaded!", Toast.LENGTH_LONG).show()
            initAruco()
        }
    }

    private fun initAruco() {
        dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_5X5_250)
        detectorParams = DetectorParameters()
        arucoDetector = ArucoDetector(dictionary, detectorParams)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            getPermission(this, PERMISSION_CAMERA, REQUEST_CODE)
        }
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return listOf(cameraBridgeViewBase)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mIntermediateMat = Mat()
    }

    override fun onCameraViewStopped() {
        mIntermediateMat.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat? {
        inputFrame?.let {
            val grayFrame = it.gray()
            val outputFrame = it.rgba()
            detectArucoMarkers(grayFrame, outputFrame)
            return outputFrame
        }
        return null
    }
    fun setOnPositionUpdateCallback(callback: (Float, Float) -> Unit) {
        onPositionUpdate = callback
    }
    fun setOnIDUpdateCallback(callback: (Int) -> Unit) {
        onIdUpdate = callback
    }

    private fun detectArucoMarkers(grayFrame: Mat, outputFrame: Mat) {
        // Implementation for detecting ArUco markers (as provided in your original code)


        // Create empty lists for storing detected marker corners and IDs
        val markerCorners: MutableList<Mat> = ArrayList() // Correct type: MutableList<Mat>
        val markerIds = Mat()
        val rejectedCandidates: MutableList<Mat> = ArrayList() // Correct type: MutableList<Mat>

        val MARKER_SIZE = 0.05 //5cm
        // Detect markers in the gray frame using the new ArucoDetector class
        arucoDetector.detectMarkers(grayFrame, markerCorners, markerIds, rejectedCandidates)

        // If markers are detected, draw them on the output frame
        if (!markerIds.empty()) {
            Log.d("Aruco", "Detected markers: " + markerIds.dump())

            // Rotation and translation vectors for pose estimation
            val rvec = Mat()
            val tvec = Mat()

            // Create camera matrix (assuming 640x480 resolution - adjust to your camera)
            val cameraMatrix = Mat(3, 3, CvType.CV_64F).apply {
                put(0, 0,
                    grayFrame.width().toDouble(), 0.0, grayFrame.width().toDouble() / 2,
                    0.0, grayFrame.width().toDouble(), grayFrame.height().toDouble() / 2,
                    0.0, 0.0, 1.0
                )
            }

            // Create distortion coefficients as MatOfDouble
            val distCoeffs = MatOfDouble()
            distCoeffs.fromArray(0.0, 0.0, 0.0, 0.0, 0.0)


            // Draw detected markers on the output frame
            for (i in markerCorners.indices) {
                // Convert Mat to MatOfPoint for polylines
                val corners = markerCorners[i]
                val points = MatOfPoint()
                val cornersData = FloatArray(8) // 4 points × 2 coordinates
                corners.get(0, 0, cornersData)

                val pointList = mutableListOf<Point>()
                for (j in cornersData.indices step 2) {
                    pointList.add(Point(cornersData[j].toDouble(), cornersData[j + 1].toDouble()))
                }
                points.fromList(pointList)

                // Marker borders
                Imgproc.polylines(
                    outputFrame,
                    listOf(points),
                    true,
                    Scalar(0.0, 255.0, 0.0),
                    4
                )

                val markerId = markerIds.get(i, 0)[0].toInt() // Get the ID of the marker
                val topLeftCorner = Point(cornersData[0].toDouble(), cornersData[1].toDouble()) // First point (x, y)

                //Update ID
                onIdUpdate?.invoke(markerId)

                // Draw the text "ID: x" on the output frame near the top-left corner of the marker
                Imgproc.putText(
                    outputFrame,
                    "ID: $markerId",
                    topLeftCorner,
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0,
                    Scalar(255.0, 0.0, 0.0),
                    4
                )

                try {
                    // Convert to object points and image points for PnP
                    val objectPoints = MatOfPoint3f()
                    val imagePoints = MatOfPoint2f()

                    // Define 3D coordinates of marker corners
                    objectPoints.fromArray(
                        Point3((-MARKER_SIZE/2), (MARKER_SIZE/2), 0.0),
                        Point3((MARKER_SIZE/2), (MARKER_SIZE/2), 0.0),
                        Point3((MARKER_SIZE/2), (-MARKER_SIZE/2), 0.0),
                        Point3((-MARKER_SIZE/2), (-MARKER_SIZE/2), 0.0)
                    )

                    // Convert the same pointList to MatOfPoint2f
                    imagePoints.fromList(pointList)

                    // Solve PnP
                    Calib3d.solvePnP(
                        objectPoints,
                        imagePoints,
                        cameraMatrix,
                        distCoeffs,
                        rvec,
                        tvec
                    )

                    // Calculate distance
                    val x = tvec.get(0, 0)[0]
                    val y = tvec.get(1, 0)[0]
                    val z = tvec.get(2, 0)[0]

                    val distance = (Math.sqrt(x*x + y*y + z*z)) - 0.1 //0.1 for accuracy

                    // Draw distance text ON CENTER
                    val center = Point(
                        (pointList[0].x + pointList[2].x) / 2,
                        (pointList[0].y + pointList[2].y) / 2
                    )

                    // Calculate the bottom-left corner relative to the detected marker corners
                    val bottomLeftMarkerCorner = Point(pointList[3].x, pointList[3].y + 20)  // Adjust y-offset as needed

                    Imgproc.putText(
                        outputFrame,
                        String.format("Distance: %.2f m", distance),
                        center,
                        Imgproc.FONT_HERSHEY_SIMPLEX,
                        0.7,
                        Scalar(0.0, 255.0, 0.0),
                        2
                    )


                    val textDistance = String.format("%.2f m", distance)
                    Log.d("ArucoDistance", "DISTANCE: $textDistance")

                    //ORIENTATION
                    // Calculate yaw angle in degrees and normalize
                    val rotationMatrix = Mat(3, 3, CvType.CV_64F)
                    Calib3d.Rodrigues(rvec, rotationMatrix)
                    val r11 = rotationMatrix.get(0, 0)[0]
                    val r21 = rotationMatrix.get(1, 0)[0]

                    // Calculate yaw angle in degrees, invert the angle for correct orientation
                    val yawRad = atan2(-r21, r11) // Inverted r21 to correct mirror effect
                    val yawDeg = Math.toDegrees(yawRad)

                    // Adjust yaw to be between 0° and 360°
                    val adjustedYaw = (yawDeg + 360 -90) % 360

                    // Determine cardinal direction based on yaw angle
                    val direction = when {
                        adjustedYaw < 45 || adjustedYaw >= 315 -> "North"
                        adjustedYaw >= 45 && adjustedYaw < 135 -> "East"
                        adjustedYaw >= 135 && adjustedYaw < 225 -> "South"
                        adjustedYaw >= 225 && adjustedYaw < 315 -> "West"
                        else -> "Unknown"
                    }

                    // Update the distance and angle
                    onPositionUpdate?.invoke(distance.toFloat(), adjustedYaw.toFloat())



                    // Draw 3D XYZ axes to indicate orientation
                    val axisLength = 0.03  // Axis length in meters
                    val axisPoints3D = MatOfPoint3f(
                        Point3(0.0, 0.0, 0.0),         // Origin (marker center)
                        Point3(axisLength, 0.0, 0.0),  // X-axis
                        Point3(0.0, axisLength, 0.0),  // Y-axis
                        Point3(0.0, 0.0, axisLength)   // Z-axis
                    )

                    val projectedAxisPoints = MatOfPoint2f()
                    Calib3d.projectPoints(
                        axisPoints3D,
                        rvec,
                        tvec,
                        cameraMatrix,
                        distCoeffs,
                        projectedAxisPoints
                    )

                    // Display direction text near marker
                    Imgproc.putText(
                        outputFrame,
                        "Dir: $direction",
                        bottomLeftMarkerCorner,
                        Imgproc.FONT_HERSHEY_SIMPLEX,
                        0.7,
                        Scalar(0.0, 0.0, 255.0),
                        2
                    )

                    // Display adjusted yaw angle
                    Imgproc.putText(
                        outputFrame,
                        "Angle: %.2f°".format(adjustedYaw),
                        Point(bottomLeftMarkerCorner.x, bottomLeftMarkerCorner.y + 30),
                        Imgproc.FONT_HERSHEY_SIMPLEX,
                        0.7,
                        Scalar(0.0, 255.0, 0.0),
                        2
                    )

                    Log.d("ArucoOrientation", "Direction: $direction, Angle: $adjustedYaw")

                    // Draw axes on the frame
                    val pts = projectedAxisPoints.toArray()
                    Imgproc.line(outputFrame, pts[0], pts[1], Scalar(0.0, 0.0, 255.0), 3)  // X-axis (red)
                    Imgproc.line(outputFrame, pts[0], pts[2], Scalar(0.0, 255.0, 0.0), 3)  // Y-axis (green)
//                    Imgproc.line(outputFrame, pts[0], pts[3], Scalar(255.0, 0.0, 0.0), 3)  // Z-axis (blue)



                } catch (e: Exception) {
                    Log.e("ArucoDistanceError", "Error during ArUco marker detection: ${e.message}")
                }


            }


            rvec.release()
            tvec.release()
            cameraMatrix.release()
            distCoeffs.release()
        }

    }

    override fun onResume() {
        super.onResume()
        cameraBridgeViewBase.enableView()
    }

    override fun onDestroy() {
        cameraBridgeViewBase.disableView()
        super.onDestroy()
    }
}

// Permission handling function (can be placed in a separate utility file if needed)
fun getPermission(con: Context, permissionCamera: String, reqCode: Int) {

    if (ActivityCompat.checkSelfPermission(con, permissionCamera) ==
        PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(con, "Camera Permission Granted", Toast.LENGTH_LONG).show()
    } else if (ActivityCompat.shouldShowRequestPermissionRationale(
            con as Activity, permissionCamera
        )
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(con)
        val perm = arrayOf(permissionCamera)

        builder.setMessage("This application requires Camera")
            .setTitle("PERMISSION REQUIRED")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                ActivityCompat.requestPermissions(con, perm, reqCode)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    } else {
        val perm = arrayOf(permissionCamera)
        ActivityCompat.requestPermissions(con, perm, reqCode)
    }
}
