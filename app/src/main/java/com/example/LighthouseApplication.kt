package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class LighthouseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeFirebaseAppCheck()
    }

    private fun initializeFirebaseAppCheck() {
        try {
            FirebaseApp.initializeApp(this)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()

            if (BuildConfig.DEBUG) {
                // In debug builds, dynamically load DebugAppCheckProviderFactory so release builds have no static reference.
                // Never hardcode, print, or commit a debug token.
                try {
                    val debugFactoryClass = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                    val getInstanceMethod = debugFactoryClass.getMethod("getInstance")
                    val factory = getInstanceMethod.invoke(null) as com.google.firebase.appcheck.AppCheckProviderFactory
                    firebaseAppCheck.installAppCheckProviderFactory(factory)
                    Log.d("LighthouseApp", "Firebase App Check initialized with DebugAppCheckProviderFactory")
                } catch (_: Exception) {
                    Log.w("LighthouseApp", "Debug App Check provider unavailable")
                }
            } else {
                // In release builds, use Play Integrity
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d("LighthouseApp", "Firebase App Check initialized with PlayIntegrityAppCheckProviderFactory")
            }
        } catch (_: Exception) {
            // Guard against cases where google-services is not yet provisioned during demo or unit test runs
            Log.w("LighthouseApp", "Firebase App Check initialization deferred")
        }
    }
}
