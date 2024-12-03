package edu.uark.joshuaj.skillscape

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase with Application Context
        FirebaseApp.initializeApp(this)
    }
}
