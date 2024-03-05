package com.example.jupfront.firebase

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class TokenSender {

    fun sendTokenToServer(context: Context, token: String?, accessToken: String?) {
        if (token != null && accessToken != null) {
            val client = OkHttpClient()
            Log.d("parsedAccessToken", accessToken)
            val json = "{\"token\":\"$token\"}"
            val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("http://192.168.219.100:8080/api/v1/notifications/test")
                .post(requestBody)
                .header("Authorization", "Bearer $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("TokenSender", "Failed to send token: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        Log.d("TokenSender", "Token sent successfully")
                    } else {
                        Log.e("TokenSender", "Failed to send token: ${response.code}")
                    }
                }
            })
        } else {
            Log.e("TokenSender", "Token or AccessToken is null")
        }
    }
}