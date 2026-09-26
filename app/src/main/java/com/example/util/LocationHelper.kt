package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onSuccess: (LatLng) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError("Location permission not granted.")
            return
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onSuccess(LatLng(location.latitude, location.longitude))
                    } else {
                        // Fallback to last known location
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                onSuccess(LatLng(lastLoc.latitude, lastLoc.longitude))
                            } else {
                                // Default fallback to SF Mission coordinates if emulator has no fix yet
                                onSuccess(LatLng(37.7648, -122.4215))
                            }
                        }.addOnFailureListener {
                            onSuccess(LatLng(37.7648, -122.4215))
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    onError(exception.message ?: "Failed to get live location.")
                }
        } catch (e: SecurityException) {
            onError("Security exception: ${e.message}")
        } catch (e: Exception) {
            onError("Error fetching location: ${e.message}")
        }
    }
}
