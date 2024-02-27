package com.example.jupfront.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("new token", token)
        saveTokenLocally(token)
    }

    override fun onCreate() {
        super.onCreate()
        //지정된 토픽을 구독
        FirebaseMessaging.getInstance().subscribeToTopic("testMessage")
    }

    private fun saveTokenLocally(token: String) {
        val shredPref = applicationContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        with(shredPref.edit()) {
            putString("firebase_token", token)
            apply()
        }
        val sharedPreferences = applicationContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val savedToken = sharedPreferences.getString("firebase_token", "")
        Log.d("SharedPreferences", "Saved token: $savedToken")
    }
}